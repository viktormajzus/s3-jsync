package s3jsync;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        CLI cli = new CLI();

        cli.run(args);
    }
}