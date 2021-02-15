package uk.org.webcompere.testgadgets;

/**
 * Similar to {@link java.util.function.BiConsumer} but allows exceptions to be thrown
 * @param <T> the type of value consumed
 * @param <U> the second type of value consumed
 */
@FunctionalInterface
public interface ThrowingBiConsumer<T, U> {
    /**
     * Accepts/receives the given value
     * @param value1 the first value to receive
     * @param value2 the second value to receive
     * @throws Exception on any error
     */
    void accept(T value1, U value2) throws Exception;
}
