package uk.org.webcompere.testgadgets.testdatafactory;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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

    @Nested
    @ExtendWith(TestDataExtension.class)
    class LoaderIsInjected {

        @Loader
        private TestDataLoader loader;

        @Test
        void loaderIsInjected() {
            assertThat(loader).isNotNull();
        }
    }

    @Nested
    @ExtendWith(TestDataExtension.class)
    class LoaderIsUsed {

        @Loader
        private TestDataLoader subDirLoader = new TestDataLoader().addPath(Paths.get("subdir"));

        @TestData("somefile.txt")
        private String someFile;

        @Test
        void someFileComesFromSubdir() {
            assertThat(someFile).isEqualTo("subdir hello");
        }
    }
}
