package uk.org.webcompere.testgadgets.testdatafactory;

import static uk.org.webcompere.testgadgets.rules.Rules.asStatement;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Load test data into the fields. To be used with <code>@Rule</code>
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
        return asStatement(() -> TestDataLoaderAnnotations.bindAnnotatedFields(loader, o), () -> {}, statement);
    }

    public TestDataLoader getLoader() {
        return this.loader;
    }
}
