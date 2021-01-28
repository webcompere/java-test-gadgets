package uk.org.webcompere.testgadgets.category;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.testgadgets.plugin.AfterAction;
import uk.org.webcompere.testgadgets.plugin.BeforeAction;
import uk.org.webcompere.testgadgets.plugin.Plugin;
import uk.org.webcompere.testgadgets.runner.TestWrapper;
import uk.org.webcompere.testgadgets.runner.WrapperOptions;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.webcompere.testgadgets.category.CategorySelection.ENVIRONMENT_VARIABLE_INCLUDE;

@RunWith(TestWrapper.class)
@WrapperOptions(runnerClass = Enclosed.class)
public class CategoryFilterTest {
    private static boolean catARan = false;
    private static boolean catBRan = false;

    private static EnvironmentVariables environmentVariables =
        new EnvironmentVariables(ENVIRONMENT_VARIABLE_INCLUDE, "catA");

    @Plugin
    public static BeforeAction beforeAll = clazz -> environmentVariables.setup();

    @Plugin
    public static AfterAction afterAll = clazz -> environmentVariables.teardown();

    @AfterClass
    public static void afterClass() {
        assertThat(catARan).isTrue();
        assertThat(catBRan).isFalse();
    }

    @RunWith(TestWrapper.class)
    @Category("catA")
    public static class CategoryATest {

        @Plugin
        public static CategoryFilter filter = new CategoryFilter();

        @Test
        public void testWillRun() {
            catARan = true;
        }
    }

    @RunWith(TestWrapper.class)
    @Category("catB")
    public static class CategoryBTest {

        @Plugin
        public static CategoryFilter filter = new CategoryFilter();

        @Test
        public void testWillNotRun() {
            catBRan = true;
        }
    }

}
