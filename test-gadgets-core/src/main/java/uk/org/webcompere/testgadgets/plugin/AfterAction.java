package uk.org.webcompere.testgadgets.plugin;

/**
 * An action that can be taken after a test
 */
public interface AfterAction {
    /**
     * Execute an action after a test class has run
     * @param clazz the type of the test class
     * @throws Throwable any error in performing the action
     */
    void after(Class<?> clazz) throws Throwable;
}
