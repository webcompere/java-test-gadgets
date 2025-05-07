package uk.org.webcompere.testgadgets.testdatafactory;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.function.Predicate.not;
import static uk.org.webcompere.testgadgets.testdatafactory.TestDataCollectionProxy.proxyFor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Apply test data loader annotations outcomes
 */
public class TestDataLoaderAnnotations {

    /**
     * Apply the right test data to the fields
     *
     * @param loaderInstance the loader to use
     * @param testObject     a test object to write to
     */
    public static void bindAnnotatedFields(TestDataLoader loaderInstance, Object testObject) throws Exception {
        setupFields(loaderInstance, testObject.getClass(), testObject, not(TestDataLoaderAnnotations::isStaticField));
        setLoaderFields(
                loaderInstance, testObject.getClass(), testObject, not(TestDataLoaderAnnotations::isStaticField));
    }

    /**
     * Apply the right test data to the static fields
     *
     * @param loaderInstance the loader to use
     * @param testClass      the class to write to
     */
    public static void bindAnnotatedStaticFields(TestDataLoader loaderInstance, Class<?> testClass) throws Exception {
        setupFields(loaderInstance, testClass, testClass, TestDataLoaderAnnotations::isStaticField);
        setLoaderFields(loaderInstance, testClass, testClass, TestDataLoaderAnnotations::isStaticField);
    }

    /**
     * Find a test loader in the test class, or the test object or find none
     *
     * @param testClass  the type of the test class
     * @param testObject the test object
     * @return any non-null test loader provided by an {@link Loader} annotated field
     */
    public static Optional<TestDataLoader> getLoaderFromTestClassOrObject(Class<?> testClass, Object testObject) {
        return Stream.concat(
                        Optional.ofNullable(testClass).stream()
                                .map(tc -> findFirstNonNullLoader(tc, tc, TestDataLoaderAnnotations::isStaticField)),
                        Optional.ofNullable(testObject).stream()
                                .map(to -> findFirstNonNullLoader(
                                        to.getClass(), to, not(TestDataLoaderAnnotations::isStaticField))))
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(Function.identity());
    }

    private static Optional<TestDataLoader> findFirstNonNullLoader(
            Class<?> testClass, Object testInstance, Predicate<Field> predicate) {
        return findAnnotatedTestDataFields(testClass, Loader.class, predicate).stream()
                .map(field -> {
                    try {
                        makeAccessible(field);
                        return field.get(testInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Could not read field with test loader in", e);
                    }
                })
                .filter(Objects::nonNull)
                .filter(obj -> obj instanceof TestDataLoader)
                .map(obj -> (TestDataLoader) obj)
                .findFirst();
    }

    private static void setLoaderFields(
            TestDataLoader loaderInstance, Class<?> testClass, Object testInstance, Predicate<Field> predicate) {
        findAnnotatedTestDataFields(testClass, Loader.class, predicate.and(field -> isNull(field, testInstance)))
                .forEach(field -> {
                    try {
                        field.set(testInstance, loaderInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Cannot set loader field", e);
                    }
                });
    }

    private static boolean isNull(Field field, Object instance) {
        try {
            makeAccessible(field);
            return field.get(instance) == null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Using this loader and this annotation, load the correct value
     *
     * @param loaderInstance     the loader which will populate the value
     * @param name               the name of the field/parameter
     * @param type               the type of the field
     * @param testDataAnnotation the annotation on the field/parameter
     * @return an object that, hopefully, meets the spec
     */
    public static Object load(TestDataLoader loaderInstance, String name, Type type, TestData testDataAnnotation)
            throws Exception {
        if (type instanceof Class) {
            var proxy = proxyFor((Class<?>) type, testDataAnnotation, null, loaderInstance);
            if (proxy.isPresent()) {
                return proxy.get();
            }
        }

        Path path = testDataAnnotation.value().length > 0 ? pathFrom(testDataAnnotation) : Paths.get(name);

        boolean cache = shouldCache(loaderInstance, testDataAnnotation);

        return loaderInstance.load(path, type, cache, testDataAnnotation.as());
    }

    public static boolean shouldCache(TestDataLoader loaderInstance, TestData testDataAnnotation) {
        boolean cache = testDataAnnotation.immutable() == Immutable.IMMUTABLE;
        if (testDataAnnotation.immutable() == Immutable.DEFAULT) {
            cache = loaderInstance.getImmutableMode() == Immutable.IMMUTABLE;
        }
        return cache;
    }

    private static void setup(TestDataLoader loaderInstance, Field field, Object testInstance) throws Exception {
        makeAccessible(field);

        TestData testDataAnnotation = Objects.requireNonNull(field.getAnnotation(TestData.class));

        Object value;
        if (field.getType().equals(Supplier.class)) {
            value = (Supplier<?>) (() -> {
                try {
                    return load(
                            loaderInstance,
                            field.getName(),
                            ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0],
                            testDataAnnotation);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot load " + e.getMessage(), e);
                }
            });
        } else {
            value = load(loaderInstance, field.getName(), field.getType(), testDataAnnotation);
        }

        field.set(testInstance, value);
    }

    private static void setupFields(
            TestDataLoader loaderInstance, Class<?> clazz, Object testInstance, Predicate<Field> predicate)
            throws Exception {
        for (Field field : findTestDataFields(clazz, predicate)) {
            setup(loaderInstance, field, testInstance);
        }
    }

    private static List<Field> findTestDataFields(Class<?> clazz, Predicate<Field> predicate) {
        return findAnnotatedTestDataFields(clazz, TestData.class, predicate);
    }

    private static List<Field> findAnnotatedTestDataFields(
            Class<?> clazz, Class<? extends Annotation> annotation, Predicate<Field> predicate) {
        Predicate<Field> annotated = field -> field.isAnnotationPresent(annotation);
        return getAllFields(clazz, annotated.and(predicate));
    }

    @SuppressWarnings("deprecation") // "AccessibleObject.isAccessible()" is deprecated in Java 9
    private static <T extends AccessibleObject> T makeAccessible(T object) {
        if (!object.isAccessible()) {
            object.setAccessible(true);
        }
        return object;
    }

    private static boolean isStaticField(Field f) {
        return isStatic(f.getModifiers());
    }

    private static List<Field> getAllFields(Class<?> type, Predicate<Field> predicate) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                fields.add(field);
            }
        }
        return fields.stream().filter(predicate).collect(Collectors.toList());
    }

    static Path pathFrom(TestData testDataAnnotation) {
        return pathFrom(testDataAnnotation.value());
    }

    /**
     * Find a path from the slugs provided in an array of path pieces - treating slashes as delimited
     * @param slugs the slugs
     * @return a path or exception if nothing is present
     */
    public static Path pathFrom(String[] slugs) {
        return Arrays.stream(slugs)
                .flatMap(slug -> Arrays.stream(slug.split("[/\\\\]+")))
                .filter(not(String::isBlank))
                .map(Paths::get)
                .reduce((prev, next) -> prev.resolve(next))
                .orElseThrow();
    }
}
