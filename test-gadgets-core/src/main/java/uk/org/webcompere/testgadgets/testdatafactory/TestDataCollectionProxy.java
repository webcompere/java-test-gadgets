package uk.org.webcompere.testgadgets.testdatafactory;

import static uk.org.webcompere.testgadgets.testdatafactory.TestDataLoaderAnnotations.pathFrom;
import static uk.org.webcompere.testgadgets.testdatafactory.TestDataLoaderAnnotations.shouldCache;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Act as an interface tagged by <code>@TestDataCollection</code>
 */
public class TestDataCollectionProxy implements InvocationHandler {
    private Path parentPath;
    private TestDataLoader loader;

    private TestDataCollectionProxy(
            TestData fieldAnnotation, TestDataCollection classAnnotation, Path parentPath, TestDataLoader loader) {
        this.parentPath = Stream.of(
                        Optional.ofNullable(parentPath),
                        toOptionalPath(fieldAnnotation.value()),
                        toOptionalPath(classAnnotation.value()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Path::resolve)
                .orElse(null);

        this.loader = loader;
    }

    private static Optional<Path> toOptionalPath(String[] path) {
        return Optional.of(path).filter(p -> p.length > 0).map(TestDataLoaderAnnotations::pathFrom);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(TestData.class)) {
            // this either returns a proxy or a value
            TestData annotation = method.getDeclaredAnnotation(TestData.class);
            var newProxy = proxyFor(method.getReturnType(), annotation, parentPath, loader);
            if (newProxy.isPresent()) {
                return newProxy.get();
            }

            Path path = annotation.value().length > 0 ? pathFrom(annotation) : Paths.get(method.getName());
            if (parentPath != null) {
                path = parentPath.resolve(path);
            }

            return loader.load(path, method.getReturnType(), shouldCache(loader, annotation), annotation.as());
        }

        // other methods all return null
        return null;
    }

    /**
     * Check the type of the field and determine whether it's eligible for a Proxy (in that it would have a
     * {@link TestDataCollection} annotation on it, and be an interface
     * @param clazz the field type which should be an interface type with the {@link TestDataCollection} annotation on it
     *              or <code>null</code> will be returned
     * @param fieldAnnotation the {@link TestData} annotation on the field itself - to get parent paths
     * @param parentPath any cumulative parent path - can be null
     * @param loader the test data loader that the proxy would use
     * @return {@link Optional#empty()} if no proxy, or a proxy to use of the field value if it's possible to proxy it
     * @param <T> the type of the field
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> proxyFor(
            Class<T> clazz, TestData fieldAnnotation, Path parentPath, TestDataLoader loader) {
        if (!clazz.isInterface() || !clazz.isAnnotationPresent(TestDataCollection.class)) {
            return Optional.empty();
        }
        return Optional.of((T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[] {clazz},
                new TestDataCollectionProxy(
                        fieldAnnotation, clazz.getAnnotation(TestDataCollection.class), parentPath, loader)));
    }
}
