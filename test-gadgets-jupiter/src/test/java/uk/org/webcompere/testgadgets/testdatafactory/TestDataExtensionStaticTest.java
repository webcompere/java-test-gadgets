package uk.org.webcompere.testgadgets.testdatafactory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestDataExtension.class)
class TestDataExtensionStaticTest {

    @TestData("somefile.txt")
    private static String someFile;

    @Test
    void staticFieldIsPopulated() {
        assertThat(someFile).isEqualTo("hello world");
    }
}
