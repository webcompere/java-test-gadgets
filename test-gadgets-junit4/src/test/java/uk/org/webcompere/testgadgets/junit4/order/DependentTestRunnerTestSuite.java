package uk.org.webcompere.testgadgets.junit4.order;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Include only the top-level tests of {@link DependentTestRunnerTests} and not the inner tests, which are
 * intended to fail to show the test runner's behaviour is correct
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({DependentTestRunnerTests.class})
public class DependentTestRunnerTestSuite {
}
