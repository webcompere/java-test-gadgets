package uk.org.webcompere.testgadgets.testdatafactory;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static uk.org.webcompere.testgadgets.rules.Rules.asStatement;

/**
 * Load test data into the fields
 */
public class TestDataFieldsRule implements MethodRule {
    private final TestDataLoader loader;

    public TestDataFieldsRule() {
        this(new TestDataLoader());
    }

    public TestDataFieldsRule(TestDataLoader loader) {
        this.loader = loader;
    }

    @Override
    public Statement apply(Statement statement, FrameworkMethod frameworkMethod, Object o) {
        return asStatement(
            () -> TestDataLoaderAnnotations.bindAnnotatedFields(loader, o), () -> {}, statement);
    }
}
