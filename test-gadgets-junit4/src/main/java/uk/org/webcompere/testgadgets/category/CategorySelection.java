package uk.org.webcompere.testgadgets.category;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static uk.org.webcompere.testgadgets.category.CategoryRelationship.INCLUDE;

/**
 * This class parses the configuration values for included and excluded categories, which are <code>,</code> delimited
 * strings of category names. It is then able to look at the list of categories inside a {@link Category} annotation
 * and determine whether that means the test/test class is permitted. The logic for this:
 * <ul>
 *     <li>If there is no annotation, then the test can always run</li>
 *     <li>An annotation marked with {@link CategoryRelationship#INCLUDE} requires one of the chosen categories to be in
 *     the <code>included</code> categories in order to run; however, if the annotation has a tag which is in the
 *     <code>excluded</code> categories, then the test is prevent from running</li>
 *     <li>An annotation marked with {@link CategoryRelationship#EXCLUDE} will run unless any of its tags are found
 *     in the <code>excluded</code> categories</li>
 * </ul>
 * It's likely that in most cases tests will be simply marked as opt-in or opt-outs based on a simple exclusion list.
 * However, the category tagging will allow for a test to be given multiple tags so it can be selected in and out
 * in finer grained detail.
 */
public class CategorySelection {
    private static final String DELIMITER = ",";

    private Set<String> included;
    private Set<String> excluded;

    private CategorySelection(Set<String> included, Set<String> excluded) {
        this.included = included;
        this.excluded = excluded;
    }

    /**
     * Provide the inclusions/exclusions as <code>,</code> separated values
     * @param include category inclusion data
     * @param exclude category exclusion data
     * @return a new {@link CategorySelection} object with the given data
     */
    public static CategorySelection of(String include, String exclude) {
        return new CategorySelection(parse(include), parse(exclude));
    }

    private static Set<String> parse(String input) {
        return Arrays.stream(splitIntoValues(input))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(toSet());
    }

    private static String[] splitIntoValues(String input) {
        return Optional.ofNullable(input)
                .map(str -> str.split(DELIMITER))
                .orElse(new String[0]);
    }

    /**
     * Does the configuration of categories permit the annotation provided to run the tests?
     * @param categoryAnnotation the annotation, or <code>null</code> if the method/class doesn't have one
     * @return <code>true</code> when the test can be run
     */
    public boolean permits(Category categoryAnnotation) {
        // null means no limit
        if (categoryAnnotation == null) {
            return true;
        }

        Set<String> categories = setOf(categoryAnnotation.value());

        if (categoryAnnotation.will().equals(INCLUDE)) {
            return found(categories, included) && !found(categories, excluded);
        }

        return !found(categories, included);
    }

    private static boolean found(Set<String> someValues, Set<String> in) {
        return someValues.stream()
                .anyMatch(in::contains);
    }

    private static Set<String> setOf(String[] values) {
        if (values == null) {
            return emptySet();
        }
        return Arrays.stream(values).collect(toSet());
    }
}
