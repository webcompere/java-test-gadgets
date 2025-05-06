package uk.org.webcompere.testgadgets.testdatafactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface TestData {
    /**
     * The path to the filename from the root of the test data loader. Can be a single string
     * with `/` or `\` in it, or can be an array of the path to the filename. If no filename provided
     * then it's assumed that the field name matches the filename, using the default file extension
     */
    String[] value() default {};

    /**
     * Is this test data to be treated as immutable and thus cached and served from the cache.
     */
    Immutable immutable() default Immutable.DEFAULT;
}
