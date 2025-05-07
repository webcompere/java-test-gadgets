package uk.org.webcompere.testgadgets.testdatafactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Default a loader by extension and Object Loader type
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FileTypeLoader {
    /**
     * The file extension including the .
     * @return lowercase file extension - e.g. <code>.json</code>
     */
    String extension();

    /**
     * A class, with a default constructor, which can load this file extension. We can subclass
     * {@link JsonLoader} or make our own
     * @return a loader type to use. Must have default constructor
     */
    Class<? extends ObjectLoader> loadedBy();
}
