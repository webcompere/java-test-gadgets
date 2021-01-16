package uk.org.webcompere.testgadgets.order;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a test method (also annotated with {@link org.junit.Test} to show
 * which method(s) it needs to run after, whose failure will prevent this test from running.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DependOnPassing {
    /**
     * Provide one or more method names of methods to depend on.
     */
    String[] value();
}
