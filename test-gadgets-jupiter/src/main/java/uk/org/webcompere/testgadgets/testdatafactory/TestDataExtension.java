package uk.org.webcompere.testgadgets.testdatafactory;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class TestDataExtension implements TestInstancePostProcessor {
    private TestDataLoader testDataLoader = new TestDataLoader();

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        TestDataLoaderAnnotations.bindAnnotatedFields(testDataLoader, testInstance);
    }
}
