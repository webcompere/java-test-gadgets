package uk.org.webcompere.testgadgets.category;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.Assume.assumeTrue;

/**
 * When used as a JUnit `@Rule` field, this wires in selective running of tests at method level.
 * The class also provides the ability for the category rules to be applied by other means.<br>
 * While the logic for including/excluding is inside {@link CategorySelection}, an instance of {@link CategoryRule}
 * loads the environment variables <code>INCLUDE_TEST_CATEGORIES</code> and <code>EXCLUDE_TEST_CATEGORIES</code>
 * and uses their values to create a {@link CategorySelection}. The environment variables are comma/separated lists
 * of category names.<br>
 * {@link CategoryRule} contains the methods that can convert from a {@link FrameworkMethod} or
 * {@link Category} annotation to a <code>boolean</code> via {@link CategoryRule#isPermitted}.
 */
public class CategoryRule implements MethodRule {
    public static final String ENVIRONMENT_VARIABLE_INCLUDE = "INCLUDE_TEST_CATEGORIES";
    public static final String ENVIRONMENT_VARIABLE_EXCLUDE = "EXCLUDE_TEST_CATEGORIES";

    private CategorySelection categorySelection = readCategoriesFromEnvironment();

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                assumeTrue("Method " + method.getName() + " excluded by category", isPermitted(method));
                base.evaluate();
            }
        };
    }

    /**
     * Is a given framework method allowed?
     * @param method true if it's permitted
     * @return false if not permitted
     */
    public boolean isPermitted(FrameworkMethod method) {
        return isPermitted(method.getAnnotation(Category.class));
    }

    /**
     * Is a given category annotation allowed?
     * @param annotation the annotation
     * @return false if not permitted
     */
    public boolean isPermitted(Category annotation) {
        return categorySelection.permits(annotation);
    }

    private static CategorySelection readCategoriesFromEnvironment() {
        return CategorySelection.of(
                readEnvironmentVariable(ENVIRONMENT_VARIABLE_INCLUDE),
                readEnvironmentVariable(ENVIRONMENT_VARIABLE_EXCLUDE));
    }

    private static String readEnvironmentVariable(String variableName) {
        return possibleSources(variableName)
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(Function.identity())
                .orElse("");
    }

    private static Stream<Supplier<Optional<String>>> possibleSources(String variableName) {
        return Stream.of(
            () -> Optional.ofNullable(System.getenv(variableName)),
            () -> Optional.ofNullable(System.getProperty(variableName)));
    }
}
