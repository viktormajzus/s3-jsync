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

    /** Constructs a config if it doesn't exist, loads one otherwise
     *
     * @throws IOException Throws an exception if there was a problem writing to file
     */
    public ConfigManager() throws IOException {
        PathHandler pathHandler = new PathHandler("s3-jsync");
        pathHandler.ensureBaseDirExists();

        configPath = pathHandler.getPathTo("config.cfg");

        if(!Files.exists(configPath)) createConfig(configPath);

        load();
    }

    /** Getter for number of threads to use on download/upload
     *
     * @return Number of threads
     */
    public int getThreadCount() {
        return threadCount;
    }

    /** Getter for rate limit
     *
     * @return Returns rate limit in MB
     */
    public int getRateLimit() {
        return rateLimit;
    }

    /** Getter for part size
     *
     * @return Returns the size of parts to upload in UploadMultipart (default is 16MB, can be changed in config)
     */
    public int getPartSize() {
        return partSize;
    }

    /** Loads config settings from file
     *
     * @throws IOException Throws an exception if it failed to read config file
     */
    private void load() throws IOException {
        List<String> lines = Files.readAllLines(configPath);
        if(lines.size() < 3) throw new IOException();

        String[] threadCountStrings = lines.get(0).split("=");
        threadCount = Integer.parseInt(threadCountStrings[1]);

        String[] rateLimitStrings = lines.get(1).split("=");
        rateLimit = Integer.parseInt(rateLimitStrings[1]);

        String[] partSizeStrings = lines.get(2).split("=");
        partSize = Integer.parseInt(partSizeStrings[1]);
    }

    /** Creates config file with default settings
     *
     * @param configPath Path to config
     * @throws IOException Throws an exception if it failed to create file or write to it
     */
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
