package s3jsync;

import java.io.IOException;
import java.nio.file.*;

public class PathHandler {
    private final Path baseDirectory;

    public PathHandler(String appName) {
        this.baseDirectory = resolveConfigPath(appName);
    }

    private Path resolveConfigPath(String appName) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            return Paths.get(localAppData, appName);
        }
        else if (os.contains("mac")) {
            String home = System.getProperty("user.home");
            return Paths.get(home, appName);
        }
        else {
            String home = System.getProperty("user.home");
            return Paths.get(home, appName);
        }
    }

    public void ensureBaseDirExists() throws IOException {
        if(!Files.exists(baseDirectory)) {
            Files.createDirectories(baseDirectory);
        }
    }

    public Path getPathTo(String fileName) {
        return baseDirectory.resolve(fileName);
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }
}
