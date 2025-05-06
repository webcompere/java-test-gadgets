package uk.org.webcompere.testgadgets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.testgadgets.testdatafactory.TestData;
import uk.org.webcompere.testgadgets.testdatafactory.TestDataExtension;

@ExtendWith(TestDataExtension.class)
class TestDataExtensionStaticTest {

    @TestData("somefile.txt")
    private static String someFile;

    @Test
    void staticFieldIsPopulated() {
        assertThat(someFile).isEqualTo("hello world");
    }
}
