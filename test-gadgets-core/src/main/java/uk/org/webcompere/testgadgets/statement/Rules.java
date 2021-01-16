package uk.org.webcompere.testgadgets.statement;

import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;
import uk.org.webcompere.testgadgets.Box;
import uk.org.webcompere.testgadgets.ThrowingRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.runner.Description.createTestDescription;

/**
 * Helpers to work with JUnit4 rules
 */
public class Rules {
    /**
     * Execute multiple rules as a list
     */
    public static class MultiRuleExecutor {
        private List<TestRule> rules;

        private MultiRuleExecutor(List<TestRule> rules) {
            this.rules = rules;
        }

        /**
         * Execute a runnable within the rules in the list
         * @param runnable the code under test
         * @throws Exception when the test fails
         */
        public void execute(ThrowingRunnable runnable) throws Exception {
            execute(() -> {
                runnable.run();
                return null;
            });
        }

        /**
         * Execute a callable within the rules in the list
         * @param callable the code under test
         * @param <T> the type returned by the callable
         * @return the result of the callabl
         * @throws Exception when the test fails
         */
        public <T> T execute(Callable<T> callable) throws Exception {
            if (rules.isEmpty()) {
                return callable.call();
            }

            // recursively execute the next rule with a sub-multirule executor inside
            return executeWithRule(rules.get(0),
                () -> new MultiRuleExecutor(rules.subList(1, rules.size()))
                    .execute(callable));
        }
    }

    /**
     * Collect some rules together to run as a group
     * @param rules the {@link TestRule} objects in the order of initialization
     * @return a {@link MultiRuleExecutor} for use with {@link MultiRuleExecutor#execute}
     */
    public static MultiRuleExecutor withRules(TestRule... rules) {
        return new MultiRuleExecutor(Arrays.asList(rules));
    }

    /**
     * Execute the code under test within a given JUnit4 test rule, which is activated
     * during the execution. See {@link Rules#executeWithRule(TestRule, Callable)}
     * @param rule the rule to use
     * @param runnable code that doesn't return a value
     * @throws Exception on any exception in the code under test or rule
     */
    public static void executeWithRule(TestRule rule, ThrowingRunnable runnable) throws Exception {
        executeWithRule(rule, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Execute the code under test within a given JUnit4 test rule, which is activated
     * during the execution. This will work with some JUnit4 rules, but not all. This method
     * does not have access to the actual test method being executed and so does not
     * have access to its annotations. For some rules that would make the rule not work.
     * @param rule the rule to use
     * @param callable code that returns a value
     * @param <T> the type of value returned by the code under test
     * @return the return value of the callable
     * @throws Exception on any exception in the code under test or rule
     */
    public static <T> T executeWithRule(TestRule rule, Callable<T> callable) throws Exception {
        Box<T> box = new Box<>();
        try {
            constructStatement(rule, callable, box)
                .evaluate();
        } catch (Throwable t) {
            if (t instanceof Exception) {
                throw (Exception)t;
            }
            if (t instanceof Error) {
                throw (Error)t;
            }
        }
        return box.getValue();
    }

    private static <T> Statement constructStatement(TestRule rule, Callable<T> callable, Box<T> box) {
        return rule.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                box.setValue(callable.call());
            }
        }, createTestDescription(Rules.class, "executeWithRule"));
    }
}
