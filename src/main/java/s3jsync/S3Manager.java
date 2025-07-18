package s3jsync;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListBucketsIterable;

import java.nio.file.Path;
import java.util.List;

public class S3Manager {
    private final S3Client client;

    public S3Manager(String accessKey, String secretKey, String region) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(region))
                .build();
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

        objects.forEach(object -> {
            System.out.printf(" - %-50s %12s\n", object.key(), formatSize(object.size()));
        });
    }

    public void uploadDirectory(Path localDir, String bucketName) {
        // TODO: Implement
    }

    public void downloadDirectory(String bucketName, Path localDir) {
        // TODO: Implement
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
