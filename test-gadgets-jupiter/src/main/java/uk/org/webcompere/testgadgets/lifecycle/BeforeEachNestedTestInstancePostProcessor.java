package uk.org.webcompere.testgadgets.lifecycle;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedMethods;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.TOP_DOWN;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * Allows a custom method - {@link BeforeEachNested} that's run before each instance of a nested class, to tidy
 * up before a test sub-suite.
 */
public class BeforeEachNestedTestInstancePostProcessor implements TestInstancePostProcessor {
    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        if (context.getTestClass()
                .filter(clazz -> isAnnotated(clazz, Nested.class))
                .isPresent()) {
            context.getParent().ifPresent(this::executeAllBeforeEachNestedMethods);
        }
    }

    private void executeAllBeforeEachNestedMethods(ExtensionContext parent) {
        parent.getTestClass().ifPresent(this::executeAllBeforeEachNestedMethods);
    }

    private void executeAllBeforeEachNestedMethods(Class<?> clazz) {
        findAnnotatedMethods(clazz, BeforeEachNested.class, TOP_DOWN).forEach(this::invokeBeforeEachNested);
    }

    private void invokeBeforeEachNested(Method method) {
        try {
            method.setAccessible(true);
            method.invoke(null);
        } catch (Exception e) {
            fail("Cannot invoke: " + method.getName() + " in "
                    + method.getDeclaringClass().getCanonicalName() + " " + e.getMessage());
        }
    }
}
