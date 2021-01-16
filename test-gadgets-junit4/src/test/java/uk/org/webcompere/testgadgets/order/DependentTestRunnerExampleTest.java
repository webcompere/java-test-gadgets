package uk.org.webcompere.testgadgets.order;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Example showing method order and dependencies between tests - not a unit test, but a template for using
 * Junit method ordering with dependencies.
 */
@RunWith(DependentTestRunner.class)
public class DependentTestRunnerExampleTest {
    @Test
    @Priority(1)
    public void aTest() {

    }

    @Test
    @Priority(2)
    @DependOnPassing("anotherTest")
    public void dependentTest() {

    }

    @Test
    public void anotherTest() {

    }
}
