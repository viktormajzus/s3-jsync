package s3jsync;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListBucketsIterable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.RateLimiter;

public class S3Manager {
    private final S3Client client;
    ConfigManager configManager;

    public S3Manager(String accessKey, String secretKey, String region) throws IOException {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(region))
                .build();
        configManager = new ConfigManager();
    }

    public void listBuckets() {
        ListBucketsIterable response = client.listBucketsPaginator();
        System.out.println("My Buckets:");
        response.buckets().forEach(bucket ->
                System.out.println(" - " + bucket.name()));
    }

    public void listObjects(String bucketName) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();
        ListObjectsV2Response response = client.listObjectsV2(request);

        List<S3Object> objects = response.contents();

        System.out.println("Objects in bucket " + bucketName + ":");
        System.out.printf("  %-50s %12s\n", "Key", "Size");
        System.out.println("  " + "-".repeat(65));

        objects.forEach(object ->
                System.out.printf(" - %-50s %12s\n", object.key(), formatSize(object.size())));
    }

    public void uploadDirectory(Path srcDirectory, String dstBucket) throws IOException, InterruptedException {
        Map<String, Path> files = FileWalker.getAllFiles(srcDirectory);

        Map<String, Instant> remoteTimestamps = new HashMap<>();
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(dstBucket)
                .build();
        ListObjectsV2Response response = client.listObjectsV2(request);
        for (S3Object obj : response.contents()) {
            remoteTimestamps.put(obj.key(), obj.lastModified());
        } // Move to method later

        ExecutorService executor = Executors.newFixedThreadPool(configManager.getThreadCount()); // 8 Threads

        List<Future<?>> futures = new ArrayList<>();

        AtomicLong totalBytesUploaded = new AtomicLong(0);
        long startTime = System.nanoTime();
        long totalBytes = files.values().stream()
                .mapToLong(p -> p.toFile().length())
                .sum();

        RateLimiter rateLimiter = RateLimiter.create(configManager.getRateLimit() * 1024 * 1024); // 50 MB/s

        for(Map.Entry<String, Path> entry : files.entrySet()) {
            String key = entry.getKey();
            File file = entry.getValue().toFile();
            long size = file.length();

            Runnable task = () -> {
                Instant localModified = Instant.ofEpochMilli(file.lastModified());

                // For later
                if (remoteTimestamps.containsKey(key)) {
                    Instant remoteModified = remoteTimestamps.get(key);
                    if (remoteModified.isAfter(localModified)) {
                        long count = totalBytesUploaded.addAndGet(size);
                        System.out.printf("[%.1f%%] Skipped (remote is newer): %s\n",
                                (count * 100.0) / totalBytes, key);
                        return;
                    }
                }

                rateLimiter.acquire((int)size);

                boolean success = false;
                for (int attempt = 0; attempt < 3 && !success; attempt++) {
                    if (size > 100 * 1024 * 1024) {
                        success = UploadMultipart(dstBucket, key, file);
                    } else {
                        success = UploadWhole(dstBucket, key, file);
                    }

                    if (!success)
                        System.err.printf("Retrying upload for %s (attempt %d)\n", key, attempt + 2);
                }
                if(success) {
                    long count = totalBytesUploaded.addAndGet(size);
                    System.out.printf("[%.1f%%] Uploaded: %s\n",
                            (count * 100.0) / totalBytes, key);

                }
                else
                    System.err.printf("Failed to upload %s after 3 attempts.\n", key);
            };

            futures.add(executor.submit(task));
        }

        for(Future<?> future : futures) {
            try{
                future.get();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        long endTime = System.nanoTime();
        long durationNs = endTime - startTime;
        long seconds = durationNs / 1_000_000_000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        System.out.printf(
                "Uploaded %s in %02d:%02d:%02d\n",
                formatSize(totalBytesUploaded.get()),
                hours, minutes, seconds
        );

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
    }

    public void downloadDirectory(String srcBucket, Path dstDirectory) {
        // TODO: Implement
    }

    private boolean UploadWhole(String dstBucket, String key, File file) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(dstBucket)
                    .key(key)
                    .build();

            client.putObject(request, RequestBody.fromFile(file));
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    private boolean UploadMultipart(String dstBucket, String key, File file) {
        final long partSize = (long)configManager.getPartSize() * 1024 * 1024; // 16 MB

        CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                .bucket(dstBucket)
                .key(key)
                .build();
        CreateMultipartUploadResponse createResponse = client.createMultipartUpload(createRequest);
        String uploadId = createResponse.uploadId();

        List<CompletedPart> completedParts = new ArrayList<>();
        int partNumber = 1;
        long position = 0;

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            while (position < file.length()) {
                long bytesToRead = Math.min(partSize, file.length() - position);
                byte[] buffer = new byte[(int) bytesToRead];
                raf.seek(position);
                int bytesRead = raf.read(buffer);
                if (bytesRead == -1) break;

                UploadPartRequest partRequest = UploadPartRequest.builder()
                        .bucket(dstBucket)
                        .key(key)
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .contentLength((long) bytesRead)
                        .build();

                UploadPartResponse partResponse = client.uploadPart(partRequest, RequestBody.fromBytes(buffer));

                completedParts.add(CompletedPart.builder()
                        .partNumber(partNumber)
                        .eTag(partResponse.eTag())
                        .build());

                position += bytesRead;
                partNumber++;
            }

            CompletedMultipartUpload completedUpload = CompletedMultipartUpload.builder()
                    .parts(completedParts)
                    .build();

            CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(dstBucket)
                    .key(key)
                    .uploadId(uploadId)
                    .multipartUpload(completedUpload)
                    .build();

            client.completeMultipartUpload(completeRequest);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                    .bucket(dstBucket)
                    .key(key)
                    .uploadId(uploadId)
                    .build());
            return false;
        }
    }


    private String formatSize(long sizeBytes) {
        if(sizeBytes >= 1024 * 1024 * 1024) {
            double sizeGB = sizeBytes / (1024.0 * 1024.0 * 1024.0);
            return String.format("%.1f GB", sizeGB);
        }
        else if (sizeBytes >= 1024 * 1024) {
            double sizeMB = sizeBytes / (1024.0 * 1024.0);
            return String.format("%.1f MB", sizeMB);
        } else if (sizeBytes >= 1024) {
            double sizeKB = sizeBytes / 1024.0;
            return String.format("%.1f KB", sizeKB);
        } else {
            return sizeBytes + " B";
        }
    }
}
