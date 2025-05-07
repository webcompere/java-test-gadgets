package uk.org.webcompere.testgadgets.testdatafactory;

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
     * Set the root of the test data loadwr
     * @param root the new root
     * @return this for fluent use
     */
    public TestDataLoader setRoot(Path root) {
        this.root = root;
        return this;
    }

    /**
     * Move the root path deeper in the hierarchy
     * @param subdirectory the subdirectory to move to
     * @return this for fluent calling
     */
    public TestDataLoader addPath(Path subdirectory) {
        root = root.resolve(subdirectory);
        return this;
    }

    /**
     * Add an object loader to the configuration - or replace one
     * @param extension the file extension, including the .
     * @param objectLoader the loader which can load files of this extension
     * @return this for fluent calling
     */
    public TestDataLoader addLoader(String extension, ObjectLoader objectLoader) {
        loaders.put(extension.toLowerCase(Locale.getDefault()), objectLoader);
        return this;
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
    public <T> T load(Path pathToFile, Type type, boolean useCache) throws IOException {
        return load(pathToFile, type, useCache, null);
    }

    /**
     * Load a file into an object
     * @param pathToFile the path to the file relative to the paths in the engine
     * @param type the type to hydrate
     * @param useCache whether to use the cache at all
     * @param overrideExtension can be null or blank, but if present, it's the file extension that defines which
     *                     loader to use, in place of the native file extension
     * @return the object required
     * @param <T> the type of object to load
     * @throws IOException on any error
     */
    @SuppressWarnings("unchecked")
    public <T> T load(Path pathToFile, Type type, boolean useCache, String overrideExtension) throws IOException {
        if (isImmutable(type) || useCache) {
            try {
                return (T) cache.computeIfAbsent(pathToFile, (path) -> {
                    try {
                        return loadWithLoaders(pathToFile, type, overrideExtension);
                    } catch (IOException e) {
                        throw new RuntimeException("Error loading " + pathToFile + " " + e.getMessage(), e);
                    }
                });
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        return loadWithLoaders(pathToFile, type, overrideExtension);
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private <T> T loadWithLoaders(Path pathToFile, Type type, String overrideExtension) throws IOException {
        Path resolved = root.resolve(pathToFile);

        var fileExtension = getExtension(resolved);
        String extensionToUse = fileExtension.orElse(defaultExtension);
        Path pathToUse = fileExtension.isPresent()
                ? resolved
                : resolved.getParent().resolve(pathToFile.getFileName() + extensionToUse);

        if (overrideExtension != null && !overrideExtension.isBlank()) {
            extensionToUse = overrideExtension;
        }

        if (!loaders.containsKey(extensionToUse.toLowerCase(Locale.getDefault()))) {
            throw new IOException("No loader present for extension " + extensionToUse);
        }

        return (T) loaders.get(extensionToUse.toLowerCase(Locale.getDefault())).load(pathToUse, type);
    }

    /**
     * Set a default file extension for when one's not provided
     * @param defaultExtension the default extension to use, including the `.`
     * @return this for fluent use
     */
    public TestDataLoader setDefaultExtension(String defaultExtension) {
        this.defaultExtension = defaultExtension;
        return this;
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
     * @return this for fluent use
     */
    public TestDataLoader setImmutableMode(Immutable immutableMode) {
        this.immutableMode = immutableMode;
        return this;
    }

    private static boolean isImmutable(Type type) {
        if (type.equals(String.class)) {
            return true;
        }

        return type instanceof Class && isRecord((Class<?>) type);
    }

    private static boolean isRecord(Class<?> clazz) {
        return clazz.getSuperclass() != null && clazz.getSuperclass().getName().equals("java.lang.Record");
    }

    private static Optional<String> getExtension(Path path) {
        return Optional.ofNullable(path.getFileName())
                .map(Path::toString)
                .filter(filename -> filename.lastIndexOf(".") != -1)
                .map(filename -> filename.substring(filename.lastIndexOf(".")));
    }
}
