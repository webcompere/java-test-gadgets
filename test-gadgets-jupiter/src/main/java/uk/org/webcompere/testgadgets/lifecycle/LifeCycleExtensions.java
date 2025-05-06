package uk.org.webcompere.testgadgets.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Plugs in test gadgets lifecycle behaviour
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({BeforeEachNestedTestInstancePostProcessor.class})
public @interface LifeCycleExtensions {}
