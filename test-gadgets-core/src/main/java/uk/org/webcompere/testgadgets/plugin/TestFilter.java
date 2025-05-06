package uk.org.webcompere.testgadgets.plugin;

import java.util.function.Predicate;

/**
 * Determine whether a test should be allowed to run by inspecting the test class
 */
public interface TestFilter extends Predicate<Class<?>> {}
