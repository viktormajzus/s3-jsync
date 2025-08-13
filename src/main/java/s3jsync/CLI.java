package s3jsync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CLI {
    private final S3Manager manager;

    /** Constructs the CLI object, ensures validity of credentials
     *
     * @throws Throwable Throws an exception if credentials are invalid or environment variables are not set
     */
    public CLI() throws Throwable {
        EnvironmentCredentials creds;
        try {
            creds = new EnvironmentCredentials();
        }
        catch (Throwable e) {
            if(e instanceof NullPointerException)
                throw new Exception("Environment variables not set. Please see documentation for details.");

            throw e;
        }
        if(!creds.getValidity())
            throw new Exception("Invalid credentials");

        manager = new S3Manager(creds);
    }

    /** Runs the CLI
     *
     * @param args Arguments
     */
    public void run(String[] args) {
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
            case "help", "--help":
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
        System.out.println("Configure AWS credentials [DEPRECATED]:\n  s3-jsync configure");
    }

    /** Deprecated. Will be removed soon
     *
     *
     * @param args Arguments
     */
    private void configure(String[] args) {
        if(args.length != 1) {
            System.err.println("Invalid number of arguments for configure.");
            System.out.println("Usage: s3-jsync configure");
            return;
        }

        System.out.println("Configure AWS credentials [DEPRECATED]");

//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Please enter your access key:");
//        String accessKey = scanner.nextLine();
//        System.out.println("Please enter your secret key:");
//        String secretKey = scanner.nextLine();
//        System.out.println("Please enter your region:");
//        String region = scanner.nextLine();
//
//        creds.save(
//                accessKey.strip(),
//                secretKey.strip(),
//                region.strip());
    }

    /**
     *
     * @deprecated Deprecated due to new credentials system
     */
    private void configure() {
        System.out.println("Configure AWS credentials [DEPRECATED]");

//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Please enter your access key:");
//        String accessKey = scanner.nextLine();
//        System.out.println("Please enter your secret key:");
//        String secretKey = scanner.nextLine();
//        System.out.println("Please enter your region:");
//        String region = scanner.nextLine();
//
//        creds.save(
//                accessKey.strip(),
//                secretKey.strip(),
//                region.strip());
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
