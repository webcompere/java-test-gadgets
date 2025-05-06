package uk.org.webcompere.testgadgets.retry;

import java.time.Duration;
import java.util.concurrent.Callable;
import uk.org.webcompere.testgadgets.ThrowingRunnable;

/**
 * Wrap around test code that may need to be retried a few times in order to work. This
 * allows repeated polling of some resource that may eventually get the right answer,
 * or allows building in some resilience for flickering tests, caused by unresolveable
 * timing issues.<br>
 * Use the overloads of {@link Retryer#retryer} function to wrap around something that returns a value
 * or just does something. Use {@link Retryer#repeat()} to create the retries profile. There
 * is a default number of tries and {@link Duration} to sleep between tries, but this can be updated
 * with {@link Retries#waitBetween(Duration)} and {@link Retries#sleepBetween}
 */
public final class Retryer {
    private static final Duration DEFAULT_DURATION = Duration.ofMillis(50);

    private Retries retries;

    /**
     * Represents the retries required by the caller
     */
    public static class Retries {
        private int maxTimes;
        private Duration sleepBetween;

        /**
         * Construct a retries object
         * @param maxTimes the maximum number of times to try
         * @param sleepBetween the amount of time to wait before the next try
         */
        public Retries(int maxTimes, Duration sleepBetween) {
            this.maxTimes = maxTimes;
            this.sleepBetween = sleepBetween;
        }

        int getMaxTimes() {
            return maxTimes;
        }

        Duration getSleepBetween() {
            return sleepBetween;
        }

        /**
         * Fluent setter of the maximum number of times for the retry
         * @param maxTimes maximum number of attempts
         * @return this
         */
        public Retries times(int maxTimes) {
            this.maxTimes = maxTimes;
            return this;
        }

        /**
         * Fluent setter of the sleep between each attempt
         * @param sleepBetween sleep time
         * @return this
         */
        public Retries waitBetween(Duration sleepBetween) {
            this.sleepBetween = sleepBetween;
            return this;
        }
    }

    /**
     * Construct with the retries to use
     * @param retries the retry strategy configuration
     */
    public Retryer(Retries retries) {
        this.retries = retries;
    }

    /**
     * Default settings for retrying - you can modify this via the fluent methods
     * will default to 3 repeats with 50ms between
     * @return default retries object
     */
    public static Retries repeat() {
        return new Retries(3, DEFAULT_DURATION);
    }

    /**
     * Construct a {@link Retryer}
     * @return a retryer with default repeat
     */
    public static Retryer retryer() {
        return new Retryer(repeat());
    }

    /**
     * Call the retryer to retry an operation until it stops throwing exceptions
     * @param operation to run
     * @param retries retry settings
     * @throws Exception on any error that escapes retries
     */
    public static void retry(ThrowingRunnable operation, Retries retries) throws Exception {
        new Retryer(retries).retry(operation);
    }

    /**
     * Call the retryer to retry an operation until it stops throwing exceptions
     * @param operation to run
     * @param retries retry settings
     * @param <T> the type of value returned by the operation
     * @return whatever the callable returns on success
     * @throws Exception on any error that escapes retries
     */
    public static <T> T retry(Callable<T> operation, Retries retries) throws Exception {
        return new Retryer(retries).retry(operation);
    }

    /**
     * Call the retryer to retry an operation until it stops throwing exceptions
     * @param operation to run
     * @throws Exception on any error that escapes retries
     */
    public void retry(ThrowingRunnable operation) throws Exception {
        retry(
                () -> {
                    operation.run();
                    return null;
                },
                retries);
    }

    /**
     * Call the retryer to retry an operation until it stops throwing exceptions
     * @param operation to run
     * @param <T> the type of value returned by the operation
     * @return whatever the callable returns on success
     * @throws Exception on any error that escapes retries
     */
    public <T> T retry(Callable<T> operation) throws Exception {
        int maxTimes = retries.getMaxTimes();
        for (int i = 0; i < maxTimes; i++) {
            try {
                return operation.call();
            } catch (Throwable e) { // NOSONAR
                // if at the limit, then throw
                if (i == (maxTimes - 1)) {
                    throw e;
                } else {
                    try {
                        Thread.sleep(retries.getSleepBetween().toMillis());
                    } catch (InterruptedException ie) {
                        // ignore this
                    }
                }
            }
        }
        throw new RuntimeException("Should not reach this point - overshot the max retries");
    }

    /**
     * Fluent setter of the maximum number of times for the retry
     * @param maxTimes maximum number of attempts
     * @return this
     */
    public Retryer times(int maxTimes) {
        retries.times(maxTimes);
        return this;
    }

    /**
     * Fluent setter of the sleep between each attempt
     * @param sleepBetween sleep time
     * @return this
     */
    public Retryer waitBetween(Duration sleepBetween) {
        retries.waitBetween(sleepBetween);
        return this;
    }
}
