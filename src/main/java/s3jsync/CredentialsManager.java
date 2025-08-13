package s3jsync;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// --------------------
//   DEPRECATED CLASS
// --------------------

/**
 * @deprecated Deprecated due to switch to environment variables
 */
public class CredentialsManager {
    private final Path configPath;
    private String accessKey;
    private String secretKey;
    private String region;

    public CredentialsManager() throws IOException {
        PathHandler pathHandler = new PathHandler("s3-jsync");
        pathHandler.ensureBaseDirExists();

        configPath = pathHandler.getPathTo("credentials.cfg");
    }

    /** Loads credentials file
     *
     * @deprecated Not used anymore
     * @return True if it was able to load credentials, false otherwise
     * @throws IOException Throws an exception if it failed to read file
     */
    public Boolean load() throws IOException {
        if(!Files.exists(configPath)) return false;

        List<String> lines = Files.readAllLines(getConfigPath());
        if(lines.size() < 4 || !lines.get(0).equals("valid")) return false;

        accessKey = lines.get(1).trim();
        secretKey = lines.get(2).trim();
        region = lines.get(3).trim();

        return true;
    }

    /** Saves credentials to file
     *
     * @deprecated Due to switch to environment variables
     * @param accessKey Access Key ID
     * @param secretKey Secret Key
     * @param region Region
     * @throws IOException Throws an exception if it failed to write to file
     */
    public void save(String accessKey, String secretKey, String region) throws IOException {
        Files.createDirectories(configPath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
            writer.write("valid\n");
            writer.write(accessKey + "\n");
            writer.write(secretKey + "\n");
            writer.write(region + "\n");
        }
    }

    /** Getter for Config Path
     *
     * @return Path object to config path
     */
    public Path getConfigPath() {
        return configPath;
    }

    /** Getter for access key
     *
     * @return Access Key ID string
     */
    public String getAccessKey() {
        return accessKey;
    }

    /** Getter for secret key
     *
     * @return Secret Key string
     */
    public String getSecretKey() {
        return secretKey;
    }

    /** Getter for region
     *
     * @return Region string
     */
    public String getRegion() {
        return region;
    }
}
