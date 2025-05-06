package uk.org.webcompere.testgadgets.testdatafactory;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Adds support for test data loading to a JUnit 5 test class. Static fields will be populated before all
 * tests. Non static files are populated before each test instance.
 */
public class TestDataExtension implements BeforeEachCallback, BeforeAllCallback {
    private TestDataLoader testDataLoader = new TestDataLoader();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        TestDataLoaderAnnotations.bindAnnotatedStaticFields(testDataLoader, context.getRequiredTestClass());
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        TestDataLoaderAnnotations.bindAnnotatedFields(testDataLoader, context.getRequiredTestInstance());
    }
}
