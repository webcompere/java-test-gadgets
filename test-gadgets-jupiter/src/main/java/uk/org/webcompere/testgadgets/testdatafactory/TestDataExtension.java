package uk.org.webcompere.testgadgets.testdatafactory;

import static uk.org.webcompere.testgadgets.testdatafactory.TestDataLoaderAnnotations.pathFrom;

import java.lang.reflect.ParameterizedType;
import java.util.Locale;
import java.util.function.Supplier;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
        // customise OUR loader with the class header
        if (context.getRequiredTestClass().isAnnotationPresent(TestDataFactory.class)) {
            customiseTestDataLoader(context.getRequiredTestClass().getAnnotation(TestDataFactory.class));
        }

        TestDataLoaderAnnotations.getLoaderFromTestClassOrObject(context.getRequiredTestClass(), null)
                .ifPresent(loader -> testDataLoader = loader);

        TestDataLoaderAnnotations.bindAnnotatedStaticFields(testDataLoader, context.getRequiredTestClass());
    }

    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    private void customiseTestDataLoader(TestDataFactory annotation) {
        if (annotation.root().length > 0) {
            testDataLoader.setRoot(pathFrom(annotation.root()));
        }
        if (annotation.path().length > 0) {
            testDataLoader.addPath(pathFrom(annotation.path()));
        }
        testDataLoader.setImmutableMode(annotation.immutable());
        for (FileTypeLoader loader : annotation.loaders()) {
            try {
                testDataLoader.addLoader(
                        loader.extension().toLowerCase(Locale.getDefault()),
                        loader.loadedBy().getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                throw new RuntimeException(
                        "Cannot instantiate test data loader for " + loader.extension() + " -> "
                                + loader.loadedBy().getCanonicalName(),
                        e);
            }
        }
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
