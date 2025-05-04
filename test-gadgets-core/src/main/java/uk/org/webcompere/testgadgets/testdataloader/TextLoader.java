package uk.org.webcompere.testgadgets.testdataloader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Text loader - can only load into Strings and String arrays
 */
public class TextLoader implements ObjectLoader {
    @Override
    public Object load(Path source, Type targetType) throws IOException {
        if (targetType.equals(String.class)) {
            try (Stream<String> stream = Files.lines(source)) {
                return stream.collect(Collectors.joining("\n"));
            }
        }

        if (targetType.equals(String[].class)) {
            try (Stream<String> stream = Files.lines(source)) {
                return stream.toArray(String[]::new);
            }
        }

        throw new IOException("Cannot load text file to " + targetType.getTypeName());
    }
}
