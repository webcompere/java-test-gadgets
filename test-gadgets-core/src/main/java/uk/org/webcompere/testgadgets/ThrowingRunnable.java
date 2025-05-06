package uk.org.webcompere.testgadgets;

/**
 * A runnable that can throw an exception - essentially the code under test
 */
@FunctionalInterface
public interface ThrowingRunnable extends GenericThrowingRunnable<Exception> {}
