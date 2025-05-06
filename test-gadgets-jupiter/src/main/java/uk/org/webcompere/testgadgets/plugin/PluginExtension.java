package uk.org.webcompere.testgadgets.plugin;

import org.junit.jupiter.api.extension.*;
import uk.org.webcompere.testgadgets.TestResource;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.function.Predicate.not;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;
import static org.junit.platform.commons.util.ReflectionUtils.tryToReadFieldValue;

/**
 * Use with {@link org.junit.jupiter.api.extension.ExtendWith} to add automatic processing of
 * {@link TestResource} objects provided by System Stubs.
 * Parameters to functions will be injected as live test resources, and fields marked as
 * {@link Plugin} will be active during the test and cleaned up automatically after.
 * @since 1.0.0
 */
public class PluginExtension implements TestInstancePostProcessor,
    TestInstancePreDestroyCallback, ParameterResolver, AfterEachCallback,
    BeforeAllCallback, AfterAllCallback {

    private LinkedList<TestResource> activeResources = new LinkedList<>();

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws Exception {
        setupFields(testInstance.getClass(), testInstance, not(PluginExtension::isStaticField));
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext extensionContext) throws Exception {
        Object testInstance = extensionContext.getTestInstance().get();

        cleanupFields(testInstance.getClass(), testInstance, not(PluginExtension::isStaticField));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return TestResource.class.isAssignableFrom(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        try {
            // create using default constructor, turn it on and remember it for cleanup
            TestResource resource = (TestResource)parameterContext.getParameter().getType().newInstance();
            resource.setup();

            activeResources.addFirst(resource);
            return resource;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ParameterResolutionException("Failure to call default constructor of TestResource of type " +
                parameterContext.getParameter().getType().getCanonicalName() +
                ". The type should have a public default constructor.", e);
        } catch (Exception e) {
            throw new ParameterResolutionException("Cannot start test resource: " + e.getMessage(), e);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        executeCleanup(activeResources);
        activeResources.clear();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        cleanupFields(context.getRequiredTestClass(), null, PluginExtension::isStaticField);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        setupFields(context.getRequiredTestClass(), null, PluginExtension::isStaticField);
    }

    private void setup(Field field, Object testInstance) throws Exception {
        if (!TestResource.class.isAssignableFrom(field.getType())) {
            throw new IllegalArgumentException("Cannot use @SystemStub with non TestResource object");
        }
        makeAccessible(field);
        getInstantiatedTestResource(field, testInstance)
            .setup();
    }

    private TestResource getInstantiatedTestResource(Field field, Object testInstance) {
        return tryToReadFieldValue(field, testInstance)
            .toOptional()
            .map(val -> (TestResource)val)
            .orElseGet(() -> assignNewInstanceToField(field, testInstance));
    }

    private TestResource assignNewInstanceToField(Field field, Object testInstance) {
        try {
            TestResource resource = (TestResource)field.getType().newInstance();
            field.set(testInstance, resource);
            return resource;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private void setupFields(Class<?> clazz, Object testInstance, Predicate<Field> predicate) throws Exception {
        for (Field field : findAnnotatedFields(clazz, Plugin.class, predicate)) {
            setup(field, testInstance);
        }
    }

    private void cleanupFields(Class<?> clazz, Object testInstance, Predicate<Field> predicate) throws Exception {
        LinkedList<TestResource> active = new LinkedList<>();
        findAnnotatedFields(clazz, Plugin.class, predicate)
            .stream()
            .map(field -> tryToReadFieldValue(field, testInstance).toOptional())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(item -> (TestResource)item)
            .forEach(active::addFirst);

        executeCleanup(active);
    }

    private static boolean isStaticField(Field f) {
        return isStatic(f.getModifiers());
    }

    /**
     * Clean up all of the resources provided, tolerating exceptions in any of them and throwing
     * at the end if necessary
     * @param resourcesSetUp the list of resources in the order to clean them up
     * @throws Exception on the first teardown error
     */
    private static void executeCleanup(List<TestResource> resourcesSetUp) throws Exception {
        Exception firstExceptionThrownOnTidyUp = null;
        for (TestResource resource : resourcesSetUp) {
            try {
                resource.teardown();
            } catch (Exception e) {
                firstExceptionThrownOnTidyUp = firstExceptionThrownOnTidyUp == null ? e : firstExceptionThrownOnTidyUp;
            }
        }
        if (firstExceptionThrownOnTidyUp != null) {
            throw firstExceptionThrownOnTidyUp;
        }
    }
}
