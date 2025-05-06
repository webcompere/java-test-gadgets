package uk.org.webcompere.testgadgets.testdatafactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;

/**
 * Can load from a specific file format into a given type
 */
public interface ObjectLoader {

    /**
     * Load from the source and produce an object of the target type
     * @param source the source file
     * @param targetType the type to create - the loader may or may not support this type, throwing
     *                   if it doesn't
     * @return an object
     * @throws IOException on error
     */
    Object load(Path source, Type targetType) throws IOException;
}
