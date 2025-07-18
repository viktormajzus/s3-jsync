package s3jsync;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    public Boolean load() throws IOException {
        if(!Files.exists(configPath)) return false;

        List<String> lines = Files.readAllLines(getConfigPath());
        if(lines.size() < 4 || !lines.get(0).equals("valid")) return false;

        accessKey = lines.get(1).trim();
        secretKey = lines.get(2).trim();
        region = lines.get(3).trim();

        return true;
    }

    public void save(String accessKey, String secretKey, String region) throws IOException {
        Files.createDirectories(configPath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
            writer.write("valid\n");
            writer.write(accessKey + "\n");
            writer.write(secretKey + "\n");
            writer.write(region + "\n");
        }
    }

    public Path getConfigPath() {
        return configPath;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getRegion() {
        return region;
    }
}
