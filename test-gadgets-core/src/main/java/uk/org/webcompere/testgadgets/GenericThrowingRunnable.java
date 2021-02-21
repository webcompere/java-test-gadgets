package uk.org.webcompere.testgadgets;

/**
 * A {@link Runnable} which adapts to the exception being thrown
 * @param <E> type of exception thrown - can implicitly become strongly typed exception
 *           or just {@link RuntimeException} when creating from a Lambda.
 */
@FunctionalInterface
public interface GenericThrowingRunnable<E extends Exception> {
    /**
     * Execute the action that runs inside the test case
     * @throws E on error
     */
    void run() throws E;

    /**
     * Convert to callable
     * @return the runnable as a callable
     */
    default GenericThrowingCallable<Void, E> asCallable() {
        return () -> {
            run();
            return null;
        };
    }
}
