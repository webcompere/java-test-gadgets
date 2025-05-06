package uk.org.webcompere.testgadgets.testdatafactory;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.function.Predicate.not;

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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Apply test data loader annotations outcomes
 */
public class TestDataLoaderAnnotations {

    /**
     * Apply the right test data to the fields
     * @param loaderInstance the loader to use
     * @param testObject a test object to write to
     */
    public static void bindAnnotatedFields(TestDataLoader loaderInstance, Object testObject) throws Exception {
        setupFields(loaderInstance, testObject.getClass(), testObject, not(TestDataLoaderAnnotations::isStaticField));
    }

    /**
     * Using this loader and this annotation, load the correct value
     * @param loaderInstance the loader which will populate the value
     * @param name the name of the field/parameter
     * @param type the type of the field
     * @param testDataAnnotation the annotation on the field/parameter
     * @return an object that, hopefully, meets the spec
     */
    public static Object load(TestDataLoader loaderInstance, String name, Type type, TestData testDataAnnotation)
            throws Exception {
        Path path = testDataAnnotation.value().length > 0 ? pathFrom(testDataAnnotation) : Paths.get(name);

        boolean cache = testDataAnnotation.immutable() == Immutable.IMMUTABLE;
        if (testDataAnnotation.immutable() == Immutable.DEFAULT) {
            cache = loaderInstance.getImmutableMode() == Immutable.IMMUTABLE;
        }

        return loaderInstance.load(path, type, cache);
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
        Predicate<Field> annotated = field -> field.isAnnotationPresent(TestData.class);
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
        return Arrays.stream(testDataAnnotation.value())
                .flatMap(slug -> Arrays.stream(slug.split("[/\\\\]+")))
                .filter(not(String::isBlank))
                .map(Paths::get)
                .reduce((prev, next) -> prev.resolve(next))
                .orElseThrow();
    }
}
