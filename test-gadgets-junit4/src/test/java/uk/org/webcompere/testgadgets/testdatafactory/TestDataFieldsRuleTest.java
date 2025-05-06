package uk.org.webcompere.testgadgets.testdatafactory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

public class TestDataFieldsRuleTest {

    @Rule
    public TestDataFieldsRule testDataFieldsRule = new TestDataFieldsRule();

    @Loader
    private TestDataLoader loader;

    @TestData("somefile.txt")
    private String somefile;

    @Test
    public void fileIsLoaded() {
        assertThat(somefile).isEqualTo("Hello world");
    }

    @Test
    public void loaderIsInstantiated() {
        assertThat(loader).isNotNull();
    }
}
