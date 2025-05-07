package uk.org.webcompere.testgadgets.testdatafactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tag an interface as a container of test data
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestDataCollection {
    /**
     * The path to the directory relative to the path of any parents. Can be a single string
     * with `/` or `\` in it, or can be an array of the path to the filename. If no filename provided
     * then it's assumed that the field name matches the filename, using the default file extension
     */
    String[] value() default {};
}
