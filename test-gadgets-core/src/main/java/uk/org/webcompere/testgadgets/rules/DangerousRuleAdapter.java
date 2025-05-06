package uk.org.webcompere.testgadgets.rules;

import static uk.org.webcompere.testgadgets.rules.ExecuteRules.executeWithRule;

import java.util.concurrent.CountDownLatch;
import org.junit.rules.TestRule;
import uk.org.webcompere.testgadgets.TestResource;
import uk.org.webcompere.testgadgets.plugin.ReferenceCountingTestResource;

/**
 * Before using this, consider if it really is a good idea! This uses threads, and expects a JUnit {@link TestRule}
 * to be thread safe enough to leave in the middle of its execution, while still providing state to the thread on which
 * the test is running. An instance of this adapter will make a test rule comply with the
 * {@link TestResource} interface, allowing it to be turned on with
 * {@link TestResource#setup()} and turned off with {@link TestResource#teardown()}. It does this by executing the
 * rule in another thread, where the execution is paused until teardown. This may be able to adapt JUnit rules
 * that do not require method annotations so they can be used stand-alone, or with the JUnit5
 * <code>PluginExtension</code>
 */
public class DangerousRuleAdapter<T extends TestRule>
        extends ReferenceCountingTestResource<DangerousRuleAdapter.DangerousRuleExecutor<T>> {

    /**
     * This uses the {@link ExecuteRules} class to execute the rule on another thread
     * @param <T> the type of test rule
     */
    static class DangerousRuleExecutor<T extends TestRule> implements TestResource {
        private T rule;
        private CountDownLatch tearDown = new CountDownLatch(1);
        private CountDownLatch tornDown = new CountDownLatch(1);

        public DangerousRuleExecutor(T rule) {
            this.rule = rule;
        }

        public T getRule() {
            return rule;
        }

        /**
         * Prepare the resource for testing
         *
         * @throws Exception on error starting
         */
        @Override
        public void setup() throws Exception {
            CountDownLatch isActive = new CountDownLatch(1);

            new Thread(() -> {
                        try {
                            executeWithRule(rule, () -> {
                                isActive.countDown();
                                tearDown.await();
                            });
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            tornDown.countDown();
                        }
                    })
                    .start();

            isActive.await();
        }

        /**
         * Clean up the resource
         *
         * @throws Exception on error cleaning up
         */
        @Override
        public void teardown() throws Exception {
            tearDown.countDown();
            tornDown.await();

            // reset for next time
            tearDown = new CountDownLatch(1);
            tornDown = new CountDownLatch(1);
        }
    }

    /**
     * Construct with the test rule to use
     * @param rule the rule
     */
    public DangerousRuleAdapter(T rule) {
        super(new DangerousRuleExecutor<>(rule));
    }

    /**
     * Get the wrapped test rule object, so its state can be used after it is set up
     * @return the test rule
     */
    public T get() {
        return getDecoratee().getRule();
    }
}
