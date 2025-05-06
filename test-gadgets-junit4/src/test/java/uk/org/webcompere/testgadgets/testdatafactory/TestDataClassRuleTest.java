package uk.org.webcompere.testgadgets.testdatafactory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class TestDataClassRuleTest {
    private static final TestDataLoader loader = new TestDataLoader();

    @TestData("somefile.txt")
    private static String staticFile;

    @ClassRule
    public static TestDataClassRule testDataClassRule = new TestDataClassRule(loader);

    @Rule
    public TestDataFieldsRule testDataFieldsRule = new TestDataFieldsRule(loader);

    @TestData("somefile.txt")
    private String somefile;

    @Loader
    private static TestDataLoader injectedLoader;

    @Test
    public void fileIsLoaded() {
        assertThat(somefile).isEqualTo("Hello world");
    }

    @Test
    public void fileIsLoadedStatically() {
        assertThat(staticFile).isEqualTo("Hello world");
    }

    @Test
    public void staticLoaderIsInstantiated() {
        assertThat(injectedLoader).isSameAs(loader);
    }
}
