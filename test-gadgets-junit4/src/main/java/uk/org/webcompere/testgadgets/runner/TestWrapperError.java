package uk.org.webcompere.testgadgets.runner;

/**
 * Exception thrown if test wrapper plugins fail
 */
public class TestWrapperError extends RuntimeException {

    public TestWrapperError(String message, Throwable cause) {
        super(message, cause);
    }
}
