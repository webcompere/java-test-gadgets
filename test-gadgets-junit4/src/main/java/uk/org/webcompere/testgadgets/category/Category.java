package uk.org.webcompere.testgadgets.category;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.org.webcompere.testgadgets.order.DependentTestRunner;

/**
 * Tag a test with categories to indicate that it will ONLY run if a category is
 * active - <code>will = INCLUDE</code>, or that it is excluded when a category is
 * active - <code>will = EXCLUDE</code>.<br>
 * You can attach multiple categories to a test. The annotation is supported on methods
 * by use of the {@link CategoryRule} and is also supported as a filter on a whole test class
 * if using the {@link DependentTestRunner}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Category {
    /**
     * Provide the categories this is a member of
     */
    String[] value();

    /**
     * Provide the criterion for membership of the category
     */
    CategoryRelationship will() default CategoryRelationship.INCLUDE;
}
