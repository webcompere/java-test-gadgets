package uk.org.webcompere.testgadgets.testdatafactory;

import java.lang.reflect.ParameterizedType;
import java.util.function.Supplier;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Adds support for test data loading to a JUnit 5 test class. Static fields will be populated before all
 * tests. Non static files are populated before each test instance.
 */
public class TestDataExtension implements BeforeEachCallback, BeforeAllCallback, ParameterResolver {
    private TestDataLoader testDataLoader = new TestDataLoader();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        TestDataLoaderAnnotations.getLoaderFromTestClassOrObject(context.getRequiredTestClass(), null)
                .ifPresent(loader -> testDataLoader = loader);

        TestDataLoaderAnnotations.bindAnnotatedStaticFields(testDataLoader, context.getRequiredTestClass());
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        TestDataLoaderAnnotations.getLoaderFromTestClassOrObject(
                        context.getRequiredTestClass(), context.getRequiredTestInstance())
                .ifPresent(loader -> testDataLoader = loader);

        TestDataLoaderAnnotations.bindAnnotatedFields(testDataLoader, context.getRequiredTestInstance());
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.isAnnotated(TestData.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        TestData data = parameterContext.findAnnotation(TestData.class).orElseThrow();
        try {
            if (parameterContext.getParameter().getType().equals(Supplier.class)) {
                return (Supplier<?>) () -> {
                    try {
                        return TestDataLoaderAnnotations.load(
                                testDataLoader,
                                parameterContext.getParameter().getName(),
                                ((ParameterizedType)
                                                parameterContext.getParameter().getParameterizedType())
                                        .getActualTypeArguments()[0],
                                data);
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot load " + e.getMessage(), e);
                    }
                };
            }
            return TestDataLoaderAnnotations.load(
                    testDataLoader,
                    parameterContext.getParameter().getName(),
                    parameterContext.getParameter().getParameterizedType(),
                    data);
        } catch (Exception e) {
            throw new ParameterResolutionException("Cannot resolve parameter to file", e);
        }
    }
}
