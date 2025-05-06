package uk.org.webcompere.testgadgets.retry;

import static uk.org.webcompere.testgadgets.retry.Retryer.repeat;
import static uk.org.webcompere.testgadgets.retry.Retryer.retry;

import java.time.Duration;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * JUnit rule that retries tests. Add this to a test class so that each of the test methods
 * will be retried if it fails at first.
 */
public class RetryTests implements MethodRule {
    private int retries;
    private Duration gapBetween;

    /**
     * Add a retry logic around all tests - use {@link DoNotRetry} to mark a test method as not for retrying.
     * Example - <code>@Rule public RetryTests retryTests = new RetryTests(10, 1000);</code> for testing
     * with 10 attempts and a second in between.
     * @param retries maximum retries
     * @param gapBetween how long to wait between retries
     */
    public RetryTests(int retries, Duration gapBetween) {
        this.retries = retries;
        this.gapBetween = gapBetween;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                DoNotRetry doNotRetry = method.getAnnotation(DoNotRetry.class);
                if (doNotRetry != null) {
                    base.evaluate();
                } else {
                    // do retries
                    retry(
                            () -> {
                                try {
                                    base.evaluate();
                                } catch (Exception e) {
                                    throw new RuntimeException(e.getMessage(), e);
                                } catch (Throwable t) {
                                    throw new RuntimeException(t);
                                }
                            },
                            repeat().times(retries).waitBetween(gapBetween));
                }
            }
        };
    }
}
