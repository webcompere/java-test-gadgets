package uk.org.webcompere.testgadgets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.testgadgets.testdatafactory.TestData;
import uk.org.webcompere.testgadgets.testdatafactory.TestDataExtension;

class TestDataExtensionTest {

    @Nested
    @ExtendWith(TestDataExtension.class)
    class SimpleBinding {

        @TestData("somefile.txt")
        private String somefile;

        @Test
        void loadsFile() {
            assertThat(somefile).isEqualTo("hello world");
        }
    }
}
