package s3jsync;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileWalker {
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
