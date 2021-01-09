package uk.org.webcompere.testgadgets.junit4.retry;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class RetryTestsTest {
    private static Set<String> failed = new HashSet<>();
    private static int numberOfMethods;

    // This is run by the test below - do not run it through normal test discovery
    @Ignore
    public static class Sporadic {
        private static int times_a = 0;
        private static int times_b = 0;
        private static int times_c = 0;

        @Rule
        public RetryTests retryTests = new RetryTests(2, Duration.ofMillis(2));

        @Test
        public void willSucceedSecondTime() {
            if (times_a++ < 1) {
                fail("not going to pass this time");
            }
        }

        @Test
        public void alwaysFails() {
            throw new RuntimeException("naughty sausage!");
        }

        @DoNotRetry
        @Test
        public void wouldSucceedSecondTimeButNotToBeRetried() {
            if (times_b++ < 1) {
                fail("not going to pass this time");
            }
        }

        @Test(timeout = 2)
        public void willNotRetryWithLowTimeout() throws Exception {
            if (times_c++ < 1) {
                fail("should not run more than once");
            }
            Thread.sleep(100);
        }
    }

    public static class UseRetryRuleTest {

        @Test
        public void checkWhatPassesAndFails() throws Exception {
            RunNotifier notifier = new RunNotifier();
            notifier.addListener(new RunListener() {
                @Override
                public void testStarted(Description description) throws Exception {
                    super.testStarted(description);
                    numberOfMethods++;
                }

                @Override
                public void testFailure(Failure failure) throws Exception {
                    super.testFailure(failure);
                    failed.add(failure.getDescription().getMethodName() + " " + failure.getMessage());
                    System.out.println(failed);
                }
            });

            new BlockJUnit4ClassRunner(Sporadic.class).run(notifier);

            assertTrue(failed.contains("wouldSucceedSecondTimeButNotToBeRetried not going to pass this time"));
            assertTrue(failed.contains("alwaysFails naughty sausage!"));
            assertFalse(failed.contains("willSucceedSecondTime not going to pass this time"));
            assertTrue(failed.contains("willNotRetryWithLowTimeout test timed out after 2 milliseconds"));
            assertThat(numberOfMethods).isEqualTo(4);
        }
    }

}
