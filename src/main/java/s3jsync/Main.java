package s3jsync;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        CLI cli = new CLI();

        cli.run(args);
    }
}