package uk.org.webcompere.testgadgets.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static uk.org.webcompere.testgadgets.category.CategorySelection.ENVIRONMENT_VARIABLE_INCLUDE;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.*;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import uk.org.webcompere.systemstubs.rules.EnvironmentVariablesRule;
import uk.org.webcompere.testgadgets.JUnitRunnerHelper;
import uk.org.webcompere.testgadgets.category.Category;

/**
 * The tests here can be run in themselves, but should be excluded from a test runner that digs into inner classes.
 * Therefore, they're brought in via maven and the {@link DependentTestRunnerTestSuite} but may fail when run
 * directly in an IDE.
 */
public class DependentTestRunnerTests {

    @Rule
    public EnvironmentVariablesRule env = new EnvironmentVariablesRule();

    private JUnitRunnerHelper runnerHelper = new JUnitRunnerHelper();

    // these must be static as the inner test class is static
    private static boolean test1ShouldPass;
    private static boolean test2ShouldPass;
    private static boolean test3ShouldPass;
    private static boolean test4ShouldPass;

    @Before
    public void resetTests() {
        test1ShouldPass = true;
        test2ShouldPass = true;
        test3ShouldPass = true;
        test4ShouldPass = true;
    }

    @RunWith(DependentTestRunner.class)
    public static class TestWithDependenciesInOrder {
        @Test
        public void test1() {
            assertTrue(test1ShouldPass);
        }

        @Test
        public void test2() {
            assertTrue(test2ShouldPass);
        }

        @DependOnPassing("test4")
        @Test
        public void test3() {
            assertTrue(test3ShouldPass);
        }

        @Test
        public void test4() {
            assertTrue(test4ShouldPass);
        }
    }

    @Test
    public void dependentTestRunBeforeDepender() {
        runnerHelper.runTestsAndCaptureTestOrder(TestWithDependenciesInOrder.class);

        assertThat(runnerHelper.getTestIndex("test4")).isLessThan(runnerHelper.getTestIndex("test3"));
    }

    @Test
    public void allTestsPass() {
        Result result = runnerHelper.runTestsAndCaptureTestOrder(TestWithDependenciesInOrder.class);

        assertThat(result.getFailureCount()).isEqualTo(0);
    }

    @Test
    public void whenTest1Fails() {
        test1ShouldPass = false;
        Result result = runnerHelper.runTestsAndCaptureTestOrder(TestWithDependenciesInOrder.class);

        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0).getDescription().getMethodName()).isEqualTo("test1");
    }

    @Test
    public void whenDependentTestFailsThenItIsAFailureAndOtherTestDoesNotRun() {
        // given test three would fail if it could, but isn't going to be run owing to test 4 failing first
        test4ShouldPass = false;
        test3ShouldPass = false;

        Result result = runnerHelper.runTestsAndCaptureTestOrder(TestWithDependenciesInOrder.class);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0).getDescription().getMethodName()).isEqualTo("test4");

        assertThat(result.getRunCount()).isEqualTo(4);
    }

    @RunWith(DependentTestRunner.class)
    public static class CyclicDependency {
        @DependOnPassing("test2")
        @Test
        public void test1() {}

        @DependOnPassing("test1")
        @Test
        public void test2() {}
    }

    @Test
    public void cannotHaveCyclicDependency() {
        Result result = runnerHelper.runTestsAndCaptureTestOrder(CyclicDependency.class);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0).getDescription().getClassName()).endsWith("CyclicDependency");
        assertThat(result.getFailures().get(0).getMessage()).startsWith("Cyclic dependency: ");
    }

    @RunWith(DependentTestRunner.class)
    public static class DependOnNonExistent {
        @DependOnPassing("test2")
        @Test
        public void test1() {}
    }

    @Test
    public void cannotDependOnNonExistent() {
        Result result = runnerHelper.runTestsAndCaptureTestOrder(DependOnNonExistent.class);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0).getDescription().getClassName()).endsWith("DependOnNonExistent");
    }

    @RunWith(DependentTestRunner.class)
    public static class ChainOfDependencies {
        @Test
        public void test1() {
            fail();
        }

        @DependOnPassing("test1")
        @Test
        public void test2() {
            // would fail, but shouldn't be run
            fail();
        }

        @DependOnPassing("test2")
        @Test
        public void test3() {
            // would fail, but as test 2 didn't run shouldn't run too
            fail();
        }
    }

    @Test
    public void chainOfTestsResultsInOneFailureAndTwoSkipped() {
        Result result = runnerHelper.runTestsAndCaptureTestOrder(ChainOfDependencies.class);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0).getDescription().getMethodName()).isEqualTo("test1");

        assertThat(result.getRunCount()).isEqualTo(3);
    }

    // can interplay with the priority order and used here to prove that the priority then takes precedence
    @FixMethodOrder(MethodSorters.JVM)
    @RunWith(DependentTestRunner.class)
    public static class TestWithSortOrder {
        @Test
        @Priority(4)
        public void test4() {
            fail();
        }

        @Test
        @Priority(3)
        public void test3() {
            fail();
        }

        @Test
        @Priority(2)
        public void test2() {
            fail();
        }

        @Test
        @Priority(1)
        public void test1() {
            fail();
        }
    }

    @Test
    public void sortOrderByPriorityIsApplied() {
        // easiest to prove with looking at a failure list
        Result result = runnerHelper.runTestsAndCaptureTestOrder(TestWithSortOrder.class);
        assertThat(result.getFailureCount()).isEqualTo(4);
        assertThat(result.getFailures().get(0).getDescription().getMethodName()).isEqualTo("test1");
        assertThat(result.getFailures().get(1).getDescription().getMethodName()).isEqualTo("test2");
        assertThat(result.getFailures().get(2).getDescription().getMethodName()).isEqualTo("test3");
        assertThat(result.getFailures().get(3).getDescription().getMethodName()).isEqualTo("test4");
    }

    @RunWith(DependentTestRunner.class)
    public static class TestsWithCategories {
        @Category("cat1")
        @Test
        public void category1() {}

        @Test
        public void always() {}
    }

    @Test
    public void whenNoCategoryOnlyOneTestIsFound() {
        runnerHelper.runTestsAndCaptureTestOrder(TestsWithCategories.class);
        assertThat(runnerHelper.getMethodCount()).isEqualTo(1);
    }

    @Test
    public void whenCategoryActiveTestsAreFound() {
        env.set(ENVIRONMENT_VARIABLE_INCLUDE, "cat1");
        runnerHelper.runTestsAndCaptureTestOrder(TestsWithCategories.class);
        assertThat(runnerHelper.getMethodCount()).isEqualTo(2);
    }

    @RunWith(DependentTestRunner.class)
    @Category("cat2")
    public static class TestsWithCategoriesAndHeadCategory {
        // this is a boxed integer we can increment
        private static final AtomicInteger COUNTER = new AtomicInteger();

        public static int getCount() {
            return COUNTER.get();
        }

        @BeforeClass
        public static void beforeClass() {
            COUNTER.incrementAndGet();
        }

        @AfterClass
        public static void afterClass() {
            COUNTER.incrementAndGet();
        }

        @Category("cat1")
        @Test
        public void category1() {}

        @Test
        public void always() {}
    }

    @Test
    public void whenNoCategoryForClassNoTestsFound() {
        runnerHelper.runTestsAndCaptureTestOrder(TestsWithCategoriesAndHeadCategory.class);
        assertThat(runnerHelper.getMethodCount()).isEqualTo(0);
    }

    @Test
    public void whenCategoryForClassAlwaysTestsFound() {
        env.set(ENVIRONMENT_VARIABLE_INCLUDE, "cat2");
        runnerHelper.runTestsAndCaptureTestOrder(TestsWithCategoriesAndHeadCategory.class);
        assertThat(runnerHelper.getMethodCount()).isEqualTo(1);
    }

    @Test
    public void whenCategoryForClassAndInternalTestThenFound() {
        env.set(ENVIRONMENT_VARIABLE_INCLUDE, "cat1,cat2");
        runnerHelper.runTestsAndCaptureTestOrder(TestsWithCategoriesAndHeadCategory.class);
        assertThat(runnerHelper.getMethodCount()).isEqualTo(2);
    }

    @Test
    public void whenCategoryPresentTheBeforeAndAfterClassAreRun() {
        int initialCount = TestsWithCategoriesAndHeadCategory.getCount();
        env.set(ENVIRONMENT_VARIABLE_INCLUDE, "cat2");
        runnerHelper.runTestsAndCaptureTestOrder(TestsWithCategoriesAndHeadCategory.class);

        assertThat(TestsWithCategoriesAndHeadCategory.getCount()).isEqualTo(initialCount + 2);
    }

    @Test
    public void whenCategoryNotPresentTheBeforeAndAfterClassAreNotRun() {
        int initialCount = TestsWithCategoriesAndHeadCategory.getCount();
        runnerHelper.runTestsAndCaptureTestOrder(TestsWithCategoriesAndHeadCategory.class);

        assertThat(TestsWithCategoriesAndHeadCategory.getCount()).isEqualTo(initialCount);
    }
}
