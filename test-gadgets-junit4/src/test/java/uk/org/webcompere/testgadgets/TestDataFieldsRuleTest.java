package uk.org.webcompere.testgadgets;

import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.testgadgets.testdatafactory.TestData;
import uk.org.webcompere.testgadgets.testdatafactory.TestDataFieldsRule;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDataFieldsRuleTest {

    @Rule
    public TestDataFieldsRule testDataFieldsRule = new TestDataFieldsRule();

    @TestData("somefile.txt")
    private String somefile;

    @Test
    public void fileIsLoaded() {
        assertThat(somefile).isEqualTo("Hello world");
    }
}
