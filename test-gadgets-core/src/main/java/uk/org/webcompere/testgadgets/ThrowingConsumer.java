package uk.org.webcompere.testgadgets;

/**
 * Similar to {@link java.util.function.Consumer} but allows exceptions to be thrown
 * @param <T> the type of value consumed
 */
@FunctionalInterface
public interface ThrowingConsumer<T> {
    /**
     * Accepts/receives the given value
     * @param value the value to receive
     * @throws Exception on any error
     */
    void accept(T value) throws Exception;
}
