package uk.org.webcompere.testgadgets;

/**
 * A runnable that can throw an exception - essentially the code under test
 */
@FunctionalInterface
public interface ThrowingRunnable {
    /**
     * Execute the action that runs inside the test case
     * @throws Exception on error
     */
    void run() throws Exception;
}
