package uk.org.webcompere.testgadgets.testdatafactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tags a reference of type {@link TestDataLoader} as either needing to be
 * instantiated with a reference to the current test data loader, or as providing
 * a reference to the test data loader. In JUnit 5, the loader can be created
 * within the body of the test class, tagged as Loader. In JUnit 4, the loader, injected
 * into the test rule can be injected into a loader field.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Loader {}
