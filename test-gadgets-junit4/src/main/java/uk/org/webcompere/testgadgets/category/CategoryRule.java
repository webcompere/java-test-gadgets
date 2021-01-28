package uk.org.webcompere.testgadgets.category;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static org.junit.Assume.assumeTrue;
import static uk.org.webcompere.testgadgets.category.CategorySelection.readCategoriesFromEnvironment;

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
}
