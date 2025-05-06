package uk.org.webcompere.testgadgets.category;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.webcompere.testgadgets.category.CategoryRelationship.EXCLUDE;
import static uk.org.webcompere.testgadgets.category.CategorySelection.ENVIRONMENT_VARIABLE_EXCLUDE;
import static uk.org.webcompere.testgadgets.category.CategorySelection.ENVIRONMENT_VARIABLE_INCLUDE;

import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.systemstubs.rules.EnvironmentVariablesRule;
import uk.org.webcompere.testgadgets.JUnitRunnerHelper;

public class CategoryRuleTest {
    @Rule
    public EnvironmentVariablesRule env = new EnvironmentVariablesRule();

    private JUnitRunnerHelper runnerHelper = new JUnitRunnerHelper();

    public static class CategoryDrivenTest {
        @Rule
        public CategoryRule categoryRule = new CategoryRule();

        @Test
        public void always() {}

        @Category("cat1")
        @Test
        public void onCat1() {}

        @Category("cat2")
        @Test
        public void onCat2() {}

        @Category({"cat1", "cat2"})
        @Test
        public void onBothCats() {}

        @Category(value = "cat1", will = EXCLUDE)
        @Test
        public void notOnCat1() {}
    }

    @Test
    public void whenCat1Selected() {
        env.set(ENVIRONMENT_VARIABLE_INCLUDE, "cat1");
        runnerHelper.runTestsAndCaptureTestOrder(CategoryDrivenTest.class);
        assertThat(runnerHelper.getAssumptionFailures()).isEqualTo(2);
    }

    @Test
    public void whenCat2Selected() {
        env.set(ENVIRONMENT_VARIABLE_INCLUDE, "cat2");
        runnerHelper.runTestsAndCaptureTestOrder(CategoryDrivenTest.class);
        assertThat(runnerHelper.getAssumptionFailures()).isEqualTo(1);
    }

    @Test
    public void whenCat1Excluded() {
        env.set(ENVIRONMENT_VARIABLE_EXCLUDE, "cat1");
        runnerHelper.runTestsAndCaptureTestOrder(CategoryDrivenTest.class);
        assertThat(runnerHelper.getAssumptionFailures()).isEqualTo(3);
    }
}
