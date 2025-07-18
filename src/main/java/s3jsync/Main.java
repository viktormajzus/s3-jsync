package s3jsync;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        CredentialsManager credentialsManager = new CredentialsManager();
        if(!credentialsManager.load()) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please enter your access key:");
            String accessKey = scanner.nextLine();
            System.out.println("Please enter your secret key:");
            String secretKey = scanner.nextLine();
            System.out.println("Please enter your region:");
            String region = scanner.nextLine();

            credentialsManager.save(accessKey, secretKey, region);
        }

        S3Manager manager = new S3Manager(credentialsManager.getAccessKey(),
                credentialsManager.getSecretKey(), credentialsManager.getRegion());
        manager.listBuckets();
        manager.listObjects("mywebsite-8a7sd82bn");
        manager.uploadDirectory(Paths.get("C:\\Users\\v.majzus\\Desktop\\TestWrite"), "mywebsite-8a7sd82bn");
    }
}