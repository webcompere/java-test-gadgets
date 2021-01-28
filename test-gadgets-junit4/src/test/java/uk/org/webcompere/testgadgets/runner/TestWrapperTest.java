package uk.org.webcompere.testgadgets.runner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import uk.org.webcompere.testgadgets.plugin.AfterAction;
import uk.org.webcompere.testgadgets.plugin.BeforeAction;
import uk.org.webcompere.testgadgets.plugin.Plugin;
import uk.org.webcompere.testgadgets.plugin.TestFilter;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Enclosed.class)
public class TestWrapperTest {
    private static boolean filteredOutClassRan = false;
    private static boolean filteredInClassRan = false;
    private static boolean nestedTestRan = false;

    @AfterClass
    public static void afterAll() {
        assertThat(filteredOutClassRan).isFalse();
        assertThat(filteredInClassRan).isTrue();
        assertThat(nestedTestRan).isTrue();
        assertThat(ThisClassShouldExecuteItsBeforeAllAndAfterAllMethods.nestedField).isFalse();
    }

    @RunWith(TestWrapper.class)
    public static class TestWrapperWrapsATestCleanly {
        @Test
        public void aTestMethod() {
            assertThat(true).isTrue();
        }
    }

    @RunWith(TestWrapper.class)
    @WrapperOptions(runnerClass = Enclosed.class)
    public static class TestWrapperWrapsAnotherRunner {
        public static class Child {
            @Test
            public void aTestMethod() {
                nestedTestRan = true;
            }
        }
    }

    @RunWith(TestWrapper.class)
    public static class ThisClassShouldFilterOutChildTestsBecauseOfPredicate {
        @Plugin
        public static TestFilter shouldRun = clazz -> false;

        @Test
        public void thisTestShouldNotRun() {
            filteredOutClassRan = true;
        }
    }

    @RunWith(TestWrapper.class)
    public static class ThisClassShouldFilterInChildTestsBecauseOfPredicate {
        @Plugin
        public static TestFilter shouldRun = clazz -> true;

        @Test
        public void thisTestShouldRun() {
            filteredInClassRan = true;
        }
    }

    @RunWith(TestWrapper.class)
    public static class ThisClassShouldExecuteItsBeforeAllAndAfterAllMethods {
        private static boolean nestedField = false;

        @Plugin
        public static BeforeAction beforeAll = clazz -> nestedField = true;

        @Plugin
        public static AfterAction afterAll = clazz -> nestedField = false;

        @BeforeClass
        public static void beforeAll() {
            assertThat(nestedField).isTrue();
        }

        @Test
        public void test() {
            assertThat(nestedField).isTrue();
        }
    }
}
