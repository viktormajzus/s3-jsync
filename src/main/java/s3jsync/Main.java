package s3jsync;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        ConfigManager configManager = new ConfigManager();
        if(!configManager.load()) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please enter your access key:");
            String accessKey = scanner.nextLine();
            System.out.println("Please enter your secret key:");
            String secretKey = scanner.nextLine();
            System.out.println("Please enter your region:");
            String region = scanner.nextLine();

            configManager.save(accessKey, secretKey, region);
        }

        S3Manager manager = new S3Manager(configManager.getAccessKey(),
                configManager.getSecretKey(), configManager.getRegion());
        manager.listBuckets();
        manager.listObjects("mywebsite-8a7sd82bn");
    }
}