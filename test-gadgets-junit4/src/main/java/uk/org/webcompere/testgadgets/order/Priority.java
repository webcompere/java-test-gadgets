package uk.org.webcompere.testgadgets.order;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply a sort priority to the test methods
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Priority {
    /**
     * The lower the number, the earlier this test will run. Unannotated tests will run last.
     */
    int value();
}
