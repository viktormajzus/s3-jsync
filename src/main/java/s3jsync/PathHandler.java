package s3jsync;

import java.io.IOException;
import java.nio.file.*;

public class PathHandler {
    private final Path baseDirectory;

    /** Constructs path to base directory for config
     * Note that this should be merged into ConfigManager later, as I've deprecated CredentialsManager
     *
     * @param appName Name of the folder you want to put in base directory
     */
    public PathHandler(String appName) {
        this.baseDirectory = resolveConfigPath(appName);
    }

    /** Resolves path to base directory/file (or folder) name
     * Note that the base directory is %APPDATA% for windows and home directory for linux. This is to be changed, as its not an appropriate location to store the config in
     *
     * @param appName File/folder
     * @return Path object to file/folder
     */
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

    /** Ensures that a base directory exists, if not, creates one
     *
     * @throws IOException Throws an exception if it failed to create a directory
     */
    public void ensureBaseDirExists() throws IOException {
        if(!Files.exists(baseDirectory)) {
            Files.createDirectories(baseDirectory);
        }
    }

    /** Gets a path to a specified file (relative to base directory)
     *
     * @param fileName Name/path to file in base directory
     * @return Path object to file
     */
    public Path getPathTo(String fileName) {
        return baseDirectory.resolve(fileName);
    }

    /** Gets the base directory
     *
     * @deprecated Until future use
     * @return Path object to base directory
     */
    public Path getBaseDirectory() {
        return baseDirectory;
    }
}
