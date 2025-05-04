package uk.org.webcompere.testgadgets.testdataloader;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An instance of this will provide test data objects from the file system
 */
public class TestDataLoader {
    private Path root = Paths.get("src", "test", "resources");
    private String defaultExtension = ".json";
    private Immutable immutableMode = Immutable.MUTABLE;

    /**
     * Collection of cached objects
     */
    private Map<Path, Object> cache = new ConcurrentHashMap<>();

    private static Map<String, ObjectLoader> defaultLoaders() {
        return new HashMap<>(Map.of(
            ".txt", new TextLoader(),
            ".json", new JsonLoader()));
    }

    private Map<String, ObjectLoader> loaders = TestDataLoader.defaultLoaders();

    /**
     * Move the root path deeper in the hierarchy
     * @param subdirectory the subdirectory to move to
     */
    public void addPath(Path subdirectory) {
        root = root.resolve(subdirectory);
    }

    /**
     * Load a file into an object
     * @param pathToFile the path to the file relative to the paths in the engine
     * @param type the type to hydrate
     * @param useCache whether to use the cache at all
     * @return the object required
     * @param <T> the type of object to load
     * @throws IOException on any error
     */
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @SuppressWarnings("unchecked")
    public <T> T load(Path pathToFile, Type type, boolean useCache) throws IOException {
        if (useCache) {
            try {
                return (T) cache.computeIfAbsent(pathToFile, (path) -> {
                    try {
                        return load(pathToFile, type, false);
                    } catch (IOException e) {
                        throw new RuntimeException("Error loading " + pathToFile + " " + e.getMessage(), e);
                    }
                });
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        Path resolved = root.resolve(pathToFile);

        var fileExtension = getExtension(resolved);
        String extensionToUse = fileExtension.orElse(defaultExtension);
        Path pathToUse = fileExtension.isPresent() ? resolved :
            resolved.getParent().resolve(pathToFile.getFileName() + extensionToUse);

        if (!loaders.containsKey(extensionToUse.toLowerCase(Locale.getDefault()))) {
            throw new IOException("No loader present for extension " + extensionToUse);
        }

        return (T) loaders.get(extensionToUse.toLowerCase(Locale.getDefault())).load(pathToUse, type);
    }

    /**
     * Set a default file extension for when one's not provided
     * @param defaultExtension the default extension to use, including the `.`
     */
    public void setDefaultExtension(String defaultExtension) {
        this.defaultExtension = defaultExtension;
    }

    /**
     * What is the default immutability of this loader
     * @return the mode, which will be guaranteed as {@link Immutable#IMMUTABLE} or {@link Immutable#MUTABLE}
     */
    public Immutable getImmutableMode() {
        if (immutableMode == Immutable.DEFAULT) {
            return Immutable.MUTABLE;
        }
        return immutableMode;
    }

    /**
     * Set the immutable mode
     * @param immutableMode the new mode
     */
    public void setImmutableMode(Immutable immutableMode) {
        this.immutableMode = immutableMode;
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private static Optional<String> getExtension(Path path) {
        String filename = path.getFileName().toString();
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            return Optional.empty();
        }
        return Optional.of(filename.substring(index));
    }
}
