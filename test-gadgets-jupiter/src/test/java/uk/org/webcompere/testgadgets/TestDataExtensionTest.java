package uk.org.webcompere.testgadgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
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

    @Nested
    @ExtendWith(TestDataExtension.class)
    class BindingAvailableInBeforeEach {

        @TestData("somefile.txt")
        private String somefile;

        @BeforeEach
        void beforeEach() {
            somefile = somefile + "!!!!!";
        }

        @Test
        void loadsFile() {
            assertThat(somefile).isEqualTo("hello world!!!!!");
        }
    }

    @Nested
    @ExtendWith(TestDataExtension.class)
    class BindingParameter {

        @Test
        void loadsFile(@TestData("somefile.txt") String somefile) {
            assertThat(somefile).isEqualTo("hello world");
        }

        @Test
        void loadsFileToSupplier(@TestData("somefile.txt") Supplier<String> somefile) {
            assertThat(somefile.get()).isEqualTo("hello world");
        }
    }
}
