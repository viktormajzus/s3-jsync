package s3jsync;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfigManager {
    private int threadCount;
    private int rateLimit;
    private int partSize;
    Path configPath;

    public ConfigManager() throws IOException {
        PathHandler pathHandler = new PathHandler("s3-jsync");
        pathHandler.ensureBaseDirExists();

        configPath = pathHandler.getPathTo("config.cfg");

        if(!Files.exists(configPath)) createConfig(configPath);

        load();
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getRateLimit() {
        return rateLimit;
    }

    public int getPartSize() {
        return partSize;
    }

    private void load() throws IOException {
        List<String> lines = Files.readAllLines(configPath);
        if(lines.size() < 3) throw new IOException();

        String threadCountStrings[] = lines.get(0).split("=");
        threadCount = Integer.parseInt(threadCountStrings[1]);

        String rateLimitStrings[] = lines.get(1).split("=");
        rateLimit = Integer.parseInt(rateLimitStrings[1]);

        String partSizeStrings[] = lines.get(2).split("=");
        partSize = Integer.parseInt(partSizeStrings[1]);
    }

    private void createConfig(Path configPath) throws IOException {
        if(Files.exists(configPath)) return;

        Files.createDirectories(configPath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {

            int defaultThreadCount = 8;
            int defaultRateLimit = 50;
            int defaultPartSize = 16;

            writer.write("ThreadCount=" + defaultThreadCount + "\n");
            writer.write("RateLimit=" + defaultRateLimit + "\n");
            writer.write("PartSize=" + defaultPartSize + "\n");
        }
    }
}
