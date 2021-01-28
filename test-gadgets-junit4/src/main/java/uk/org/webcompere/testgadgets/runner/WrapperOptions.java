package uk.org.webcompere.testgadgets.runner;

import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Options for the Test Wrapper
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface WrapperOptions {
    /**
     * The inner runner to use
     */
    Class<? extends Runner> runnerClass() default BlockJUnit4ClassRunner.class;
}
