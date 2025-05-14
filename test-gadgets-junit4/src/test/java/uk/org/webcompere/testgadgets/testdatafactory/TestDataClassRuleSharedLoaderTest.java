package uk.org.webcompere.testgadgets.testdatafactory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class TestDataClassRuleSharedLoaderTest {
    @TestData("somefile.txt")
    private static String staticFile;

    @ClassRule
    public static TestDataClassRule testDataClassRule = new TestDataClassRule();

    @Rule
    public TestDataFieldsRule testDataFieldsRule = new TestDataFieldsRule(testDataClassRule.getLoader());

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
    public void loaderIsInstantiated() {
        assertThat(injectedLoader).isSameAs(testDataClassRule.getLoader());
        assertThat(injectedLoader).isSameAs(testDataFieldsRule.getLoader());
    }
}
