package uk.org.webcompere.testgadgets;

import java.util.concurrent.Callable;

/**
 * A callable that throws something specific
 * @param <T> the return type
 * @param <E> type of exception thrown - can implicitly become strongly typed exception
 *            or just {@link RuntimeException} when creating from a Lambda.
 */
@FunctionalInterface
public interface GenericThrowingCallable<T, E extends Exception> extends Callable<T> {
    @Override
    T call() throws E;

    /**
     * A throwing callable that throws {@link Throwable}
     * @param <R> the return value type
     */
    @FunctionalInterface
    interface Thrower<R> {
        /**
         * Calls the method and returns the value
         * @return the return value
         * @throws Throwable on errors
         */
        R call() throws Throwable;
    }

    /**
     * Take something that throws {@link Throwable} and convert it to {@link GenericThrowingCallable}
     * @param thrower the thrower
     * @param <R> the type of return value
     * @return a {@link GenericThrowingCallable} with the right type not to throw {@link Throwable}
     */
    static <R> GenericThrowingCallable<R, Exception> wrap(Thrower<R> thrower) {
        return () -> {
            try {
                return thrower.call();
            } catch (Exception e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        };
    }
}
