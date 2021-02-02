package uk.org.webcompere.testgadgets.lifecycle;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Plugs in test gadgets lifecycle behaviour
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({BeforeEachNestedTestInstancePostProcessor.class})
public @interface LifeCycleExtensions {
}
