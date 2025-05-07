package uk.org.webcompere.testgadgets.testdatafactory;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
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

    public static class SomeObjectLoader implements ObjectLoader {

        @Override
        public Object load(Path source, Type targetType) throws IOException {
            return source.toString();
        }
    }

    @Nested
    @TestDataFactory(
            path = {"path", "to"},
            immutable = Immutable.IMMUTABLE,
            loaders = {@FileTypeLoader(extension = ".txt", loadedBy = SomeObjectLoader.class)})
    class Configured {

        @TestData("somefile.txt")
        private Object someFile; // marked as object to stop default caching

        @TestData("somefile.txt")
        private Object someFile2;

        @Test
        void someFileComesFromSubdir() {
            assertThat(someFile).isEqualTo("src/test/resources/path/to/somefile.txt");
        }

        @Test
        void cachingIsPresentSoBothCopiesHaveSameReference() {
            assertThat(someFile).isSameAs(someFile2);
        }
    }

    @Nested
    @TestDataFactory(
            root = {"path", "to"},
            loaders = {@FileTypeLoader(extension = ".txt", loadedBy = SomeObjectLoader.class)})
    class ConfiguredNonCachingAndWithRoot {

        @TestData("somefile.txt")
        private Object someFile; // marked as object to stop default caching

        @TestData("somefile.txt")
        private Object someFile2;

        @Test
        void someFileComesFromSubdir() {
            assertThat(someFile).isEqualTo("path/to/somefile.txt");
        }

        @Test
        void cachingIsNotPresentSoBothCopiesHaveDifferentReference() {
            assertThat(someFile).isNotSameAs(someFile2);
        }
    }
}
