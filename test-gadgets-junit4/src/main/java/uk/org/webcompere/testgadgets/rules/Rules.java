package uk.org.webcompere.testgadgets.rules;

import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;
import uk.org.webcompere.testgadgets.TestResource;
import uk.org.webcompere.testgadgets.ThrowingRunnable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Helpers for creating Test Rules
 */
public class Rules {
    /**
     * Create a test rule from a runnable called before the test executes
     * @param beforeTest the runnable to perform before the test
     * @return a {@link TestRule}
     */
    public static TestRule doBefore(ThrowingRunnable beforeTest) {
        return (statement, description) -> asStatement(beforeTest, () -> {}, statement);
    }

    /**
     * Create a test rule from a runnable called after the test executes
     * @param afterTest the runnable to perform before the test
     * @return a {@link TestRule}
     */
    public static TestRule doAfter(ThrowingRunnable afterTest) {
        return (statement, description) -> asStatement(() -> {}, afterTest, statement);
    }

    /**
     * Create a test rule from a runnable called before and after the test executes. The after will be
     * called regardless of error
     * @param beforeTest the runnable to use before the test
     * @param afterTest the runnable to call after the test (if the before succeeded)
     * @return a {@link TestRule}
     */
    public static TestRule asRule(ThrowingRunnable beforeTest, ThrowingRunnable afterTest) {
        return (statement, description) -> asStatement(beforeTest, afterTest, statement);
    }

    /**
     * Create a test rule from a {@link TestResource} with its own {@link TestResource#setup()} and
     * {@link TestResource#teardown()} methods
     * @param testResource the resource to use as a JUnit 4 {@link TestRule}
     * @return a {@link TestRule}
     */
    public static TestRule asRule(TestResource testResource) {
        return asRule(testResource::setup, testResource::teardown);
    }

    /**
     * Create a statement by composing some behaviour around the inner statement
     * @param runBefore the thing to do first
     * @param runAfter the thing to do after the test (assuming before succeeded)
     * @param inner the inner statement
     * @return a statement which does both
     * @see org.junit.rules.ExternalResource
     */
    public static Statement asStatement(ThrowingRunnable runBefore,
                                        ThrowingRunnable runAfter,
                                        Statement inner) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                runBefore.run();
                try {
                    inner.evaluate();
                } finally {
                    runAfter.run();
                }
            }
        };
    }

    /**
     * Given a set of test rules, compose them into a single statement
     * @param testRules test rules to use
     * @return a test rule that executes the others in order
     */
    public static TestRule compose(TestRule... testRules) {
        return compose(Arrays.stream(testRules));
    }

    /**
     * Given a set of test rules, compose them into a single statement
     * @param testRules a stream of test rules to use
     * @return a test rule that executes the others in order
     */
    public static TestRule compose(Stream<TestRule> testRules) {
        // construct the rule from the stream
        return (statement, description) -> {

            // the chain of responsibilities is built from the inside
            // out
            List<TestRule> rules = testRules.collect(toList());
            Collections.reverse(rules);

            Statement lastStatement = statement;
            for (TestRule rule : rules) {
                lastStatement = rule.apply(lastStatement, description);
            }

            return lastStatement;
        };
    }


}
