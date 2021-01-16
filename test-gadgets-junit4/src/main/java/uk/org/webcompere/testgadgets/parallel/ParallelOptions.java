package uk.org.webcompere.testgadgets.parallel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Options for parallel running
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ParallelOptions {
    int DEFAULT_POOL_SIZE = 4;

    /**
     * Sets the number of threads available to run child tests. Set higher if there
     * are more children and they are ALL to run in parallel. Set lower to deliberately throttle.
     * Setting to <code>1</code> is the equivalent of just using {@link org.junit.experimental.runners.Enclosed}
     * directly.
     * @return number of threads for child tests
     */
    int poolSize() default DEFAULT_POOL_SIZE;
}
