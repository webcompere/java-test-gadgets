package uk.org.webcompere.testgadgets;

/**
 * A mutable container of a typed object
 * @param <T> the type of the object
 */
public class Box<T> {
    private T value;

    /**
     * Get the value from the box
     * @return value - can be null
     */
    public T getValue() {
        return value;
    }

    /**
     * Set the value in the box
     * @param value - can be null
     */
    public void setValue(T value) {
        this.value = value;
    }
}
