package uk.org.webcompere.testgadgets.plugin;

/**
 * An action that can be taken before a test
 */
public interface BeforeAction {
    /**
     * Execute something before the test class
     * @param clazz the type of the test class
     * @throws Throwable any error in performing the action
     */
    void before(Class<?> clazz) throws Throwable;
}
