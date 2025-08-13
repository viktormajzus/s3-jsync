package s3jsync;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        CLI cli = null;
        try {
            cli = new CLI();
        }
        catch (Throwable e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        cli.run(args);
    }
}