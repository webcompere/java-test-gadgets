package uk.org.webcompere.testgadgets.testdatafactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Can be used as a synonym for <code>@ExtendWith(TestDataExtension.class)</code> allowing the
 * configuration of the Test Data Loader to be set.
 */
@ExtendWith(TestDataExtension.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestDataFactory {

    /**
     * The root directory - defaults to `src`, `test`, `resources` - provide the path as separate
     * values or values separated by slashes
     * @return the root directory configuration
     */
    String[] root() default {};

    /**
     * The directoris beneath the root - usually we only customise one of these
     * @return the subdirectories
     */
    String[] path() default {};

    /**
     * The immutability mode of the test data loader - set to {@link Immutable#IMMUTABLE} to get
     * caching
     * @return the immutability mode
     */
    Immutable immutable() default Immutable.DEFAULT;

    /**
     * Specific loaders to use to load items - each is an annotation describing how a file is loaded
     * @return the loaders
     */
    FileTypeLoader[] loaders() default {};
}
