package uk.org.webcompere.testgadgets.category;

import static uk.org.webcompere.testgadgets.category.CategorySelection.readCategoriesFromEnvironment;

import org.junit.runners.model.TestClass;
import uk.org.webcompere.testgadgets.plugin.Plugin;
import uk.org.webcompere.testgadgets.plugin.TestFilter;

/**
 * A filter, that can be used as a {@link Plugin} annotated field on a class run using the
 * {@link uk.org.webcompere.testgadgets.runner.TestWrapper} runner, so that the whole class is filtered out of a test
 * run without doing anything expensive
 */
public class CategoryFilter implements TestFilter {
    /**
     * Does the test class run at all?
     *
     * @param clazz the test class
     * @return {@code true} if the test class's category is missing, or allows the current environmental config
     */
    @Override
    public boolean test(Class<?> clazz) {
        CategorySelection selection = readCategoriesFromEnvironment();
        Category category = new TestClass(clazz).getAnnotation(Category.class);
        if (category == null) {
            return true;
        }
        return selection.permits(category);
    }
}
