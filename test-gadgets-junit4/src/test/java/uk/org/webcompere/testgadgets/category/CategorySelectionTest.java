package uk.org.webcompere.testgadgets.category;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.webcompere.testgadgets.category.CategoryRelationship.EXCLUDE;

public class CategorySelectionTest {
    private Category annotatedWithCategory1Annotation;
    private Category annotatedWithCategory2Annotation;
    private Category annotatedWithCategory1and2Annotation;
    private Category annotatedWithExcludeCategory1Annotation;
    private Category annotatedWithExcludeCategory2Annotation;
    private Category annotatedWithExcludeCategory1and2Annotation;

    @Before
    public void before() throws Exception {
        annotatedWithCategory1Annotation = getAnnotationFrom("annotatedWithCategory1");
        annotatedWithCategory2Annotation = getAnnotationFrom("annotatedWithCategory2");
        annotatedWithCategory1and2Annotation = getAnnotationFrom("annotatedWithCategory1and2");
        annotatedWithExcludeCategory1Annotation = getAnnotationFrom("annotatedWithExcludeCategory1");
        annotatedWithExcludeCategory2Annotation = getAnnotationFrom("annotatedWithExcludeCategory2");
        annotatedWithExcludeCategory1and2Annotation = getAnnotationFrom("annotatedWithExcludeCategory1and2");
    }

    @Test
    public void whenCategoryIsActiveThenItsTagsAreActive() {
        CategorySelection selection = CategorySelection.of("cat1", null);
        assertThat(selection.permits(annotatedWithCategory1Annotation)).isTrue();
        assertThat(selection.permits(annotatedWithCategory2Annotation)).isFalse();
        assertThat(selection.permits(annotatedWithCategory1and2Annotation)).isTrue();
    }

    @Test
    public void whenDifferentCategoryIsActiveThenItsTagsAreActive() {
        CategorySelection selection = CategorySelection.of("cat2", null);
        assertThat(selection.permits(annotatedWithCategory1Annotation)).isFalse();
        assertThat(selection.permits(annotatedWithCategory2Annotation)).isTrue();
        assertThat(selection.permits(annotatedWithCategory1and2Annotation)).isTrue();
    }

    @Test
    public void whenAllCategoriesAreActiveThenAllTagsAreActive() {
        CategorySelection selection = CategorySelection.of("cat1,cat2", null);
        assertThat(selection.permits(annotatedWithCategory1Annotation)).isTrue();
        assertThat(selection.permits(annotatedWithCategory2Annotation)).isTrue();
        assertThat(selection.permits(annotatedWithCategory1and2Annotation)).isTrue();
    }

    @Test
    public void whenCategoryIsExcludedThenItsExcludersAreAllowed() {
        CategorySelection selection = CategorySelection.of(null, "cat1");
        assertThat(selection.permits(annotatedWithExcludeCategory1Annotation)).isTrue();
        assertThat(selection.permits(annotatedWithExcludeCategory2Annotation)).isTrue();
        assertThat(selection.permits(annotatedWithExcludeCategory1and2Annotation)).isTrue();
    }

    @Test
    public void whenOtherCategoryIsExcludedThenItsTagsAreFine() {
        CategorySelection selection = CategorySelection.of(null, "cat2");
        assertThat(selection.permits(annotatedWithExcludeCategory1Annotation)).isTrue();
        assertThat(selection.permits(annotatedWithExcludeCategory2Annotation)).isTrue();
        assertThat(selection.permits(annotatedWithExcludeCategory1and2Annotation)).isTrue();
    }

    @Test
    public void whenAllCategoryAreExcludedThenExcludersCanBeAllowed() {
        CategorySelection selection = CategorySelection.of(null, "cat1,cat2");
        assertThat(selection.permits(annotatedWithExcludeCategory1Annotation)).isTrue();
        assertThat(selection.permits(annotatedWithExcludeCategory2Annotation)).isTrue();
        assertThat(selection.permits(annotatedWithExcludeCategory1and2Annotation)).isTrue();
    }

    @Test
    public void whenACategoryExclusionAppliesYouCannotBeInEvenIfIncluded() {
        CategorySelection selection = CategorySelection.of("cat1", "cat2");
        assertThat(selection.permits(annotatedWithCategory1and2Annotation)).isFalse();
    }

    @Test
    public void whenACategoryExclusionAppliesAndItBeingIncludedWouldExcludeYou() {
        CategorySelection selection = CategorySelection.of(null, "cat2");
        assertThat(selection.permits(annotatedWithExcludeCategory2Annotation)).isTrue();
    }

    @Test
    public void noCategoryMeansAllowed() {
        CategorySelection selection = CategorySelection.of("cat1", "cat2");
        assertThat(selection.permits(null)).isTrue();
    }

    @Category("cat1")
    public void annotatedWithCategory1() {
    }

    @Category("cat2")
    public void annotatedWithCategory2() {
    }

    @Category({"cat1","cat2"})
    public void annotatedWithCategory1and2() {
    }

    @Category(value = "cat1", will = EXCLUDE)
    public void annotatedWithExcludeCategory1() {
    }

    @Category(value = "cat2", will = EXCLUDE)
    public void annotatedWithExcludeCategory2() {
    }

    @Category(value = {"cat1","cat2"}, will = EXCLUDE)
    public void annotatedWithExcludeCategory1and2() {
    }

    private Category getAnnotationFrom(String methodName) throws Exception {
        return getClass().getMethod(methodName).getAnnotation(Category.class);
    }
}
