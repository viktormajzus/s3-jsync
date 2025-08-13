package s3jsync;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileWalker {
    /** Creates a map to every file's path in a root directory
     *
     * @param rootDir Root directory to make a map from
     * @return Map<String, Path>, where String represents the Key (which is a relative path from Root Directory), and Path represents the full Path of a file
     * @throws IOException Throws an exception if it failed to walk directory
     */
    public static Map<String, Path> getAllFiles(Path rootDir) throws IOException {
        try (Stream<Path> stream = Files.walk(rootDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toMap(
                            path -> rootDir.relativize(path).toString().replace("\\", "/"), // S3-style key
                            path -> path
                    ));
        }
    }
}
