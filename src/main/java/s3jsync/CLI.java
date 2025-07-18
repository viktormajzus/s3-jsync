package s3jsync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class CLI {
    private final S3Manager manager;
    private final CredentialsManager credentialsManager;

    public CLI() throws IOException {
        credentialsManager = new CredentialsManager();
        if(!credentialsManager.load()) {
            configure();
        }

        manager = new S3Manager(credentialsManager);
    }

    public void run(String[] args) throws IOException, InterruptedException {
        if(args.length==0) {
            printUsage();
            return;
        }

        switch(args[0]) {
            case "upload":
                upload(args);
                break;
            case "download":
                download(args);
                break;
            case "list":
                list(args);
                break;
            case "configure":
                configure(args);
                break;
            case "help":
                printUsage();
                break;
            case "--help":
                printUsage();
                break;
            default:
                System.err.println("Unknown command: "+args[0]);
                printUsage();
                break;
        }
    }

    private void printUsage() {
        System.out.println("Usage:");
        System.out.println("Upload directory to bucket:\n  s3-jsync upload <localDir> <bucket>");
        System.out.println("Download bucket contents to directory:\n  s3-jsync download <bucket> <localDir>");
        System.out.println("List buckets:\n  s3-jsync list -b");
        System.out.println("List bucket contents:\n  s3-jsync list -o <bucket>");
        System.out.println("Configure AWS credentials:\n  s3-jsync configure");
    }

    private void configure(String[] args) throws IOException {
        if(args.length != 1) {
            System.err.println("Invalid number of arguments for configure.");
            System.out.println("Usage: s3-jsync configure");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter your access key:");
        String accessKey = scanner.nextLine();
        System.out.println("Please enter your secret key:");
        String secretKey = scanner.nextLine();
        System.out.println("Please enter your region:");
        String region = scanner.nextLine();

        credentialsManager.save(accessKey, secretKey, region);
    }

    private void configure() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter your access key:");
        String accessKey = scanner.nextLine();
        System.out.println("Please enter your secret key:");
        String secretKey = scanner.nextLine();
        System.out.println("Please enter your region:");
        String region = scanner.nextLine();

        credentialsManager.save(accessKey, secretKey, region);
    }

    private void list(String[] args) {
        if(args.length != 2 && args.length != 3) {
            System.err.println("Invalid number of arguments for list.");
            System.out.println("Usage: s3-jsync list -b");
            System.out.println("Usage: s3-jsync list -o <bucket>");
            return;
        }

        if(args.length == 2) {
            if(!args[1].equals("-b")) {
                System.err.println("Invalid argument for list.");
                System.out.println("Usage: s3-jsync list -b");
                System.out.println("Usage: s3-jsync list -o <bucket>");
                return;
            }

            manager.listBuckets();
            return;
        }

        if(!args[1].equals("-o")) {
            System.err.println("Invalid argument for list.");
            System.out.println("Usage: s3-jsync list -b");
            System.out.println("Usage: s3-jsync list -o <bucket>");
            return;
        }
        if(!manager.bucketExists(args[2])) {
            System.err.println("Bucket does not exist.");
            return;
        }

        manager.listObjects(args[2]);
    }

    private void upload(String[] args) {
        if (args.length != 3) {
            System.err.println("Invalid number of arguments for upload.");
            System.out.println("Usage: s3sync upload <localDir> <bucket>");
            return;
        }

        Path srcDirectory = Path.of(args[1]);
        String dstBucket = args[2];

        if (!Files.exists(srcDirectory) || !Files.isDirectory(srcDirectory)) {
            System.err.println("Provided local directory does not exist or is not a directory: " + srcDirectory);
            return;
        }

        try {
            manager.uploadDirectory(srcDirectory, dstBucket);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during upload: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void download(String[] args) {
        if(args.length != 3) {
            System.err.println("Invalid number of arguments for download.");
            System.out.println("Usage: s3sync download <bucket> <localDir>");
            return;
        }

        Path dstDirectory = Path.of(args[2]);
        String srcBucket = args[1];

        if(!manager.bucketExists(srcBucket)) {
            System.err.println("Bucket does not exist: " + srcBucket);
            return;
        }

        try {
            manager.downloadDirectory(srcBucket, dstDirectory);
        } catch (InterruptedException e) {
            System.err.println("Error during download: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
