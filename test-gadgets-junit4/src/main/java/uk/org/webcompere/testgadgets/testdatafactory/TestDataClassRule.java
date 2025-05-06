package uk.org.webcompere.testgadgets.testdatafactory;

import static uk.org.webcompere.testgadgets.rules.Rules.asStatement;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Applies test data loading to the static fields of the class. Used with <code>@ClassRule</code>
 */
public class TestDataClassRule implements TestRule {
    private final TestDataLoader loader;

    public TestDataClassRule() {
        this(new TestDataLoader());
    }

    public TestDataClassRule(TestDataLoader loader) {
        this.loader = loader;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return asStatement(
                () -> TestDataLoaderAnnotations.bindAnnotatedStaticFields(loader, description.getTestClass()),
                () -> {},
                statement);
    }
}
