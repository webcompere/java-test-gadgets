package uk.org.webcompere.testgadgets.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.testgadgets.TestResource;

class PluginExtensionTest {
    @Nested
    @ExtendWith(PluginExtension.class)
    class TestResourceIsActiveDuringTest {
        private String testState;

        @Plugin
        private TestResource someResource = TestResource.from(() -> testState = "Good", () -> testState = "None");

        @Test
        void insideTest() {
            assertThat(testState).isEqualTo("Good");
        }
    }

    /**
     * Example resource - creates a file with some string in it
     */
    public static class TempFileResource implements TestResource {
        private File file;

        public File getFile() {
            return file;
        }

        /**
         * Prepare the resource for testing
         *
         * @throws Exception on error starting
         */
        @Override
        public void setup() throws Exception {
            file = File.createTempFile("foo", "bar");
            Files.write(file.toPath(), "some string".getBytes(StandardCharsets.UTF_8));
        }

        /**
         * Clean up the resource
         *
         * @throws Exception on error cleaning up
         */
        @Override
        public void teardown() throws Exception {
            file.delete();
        }
    }

    @Nested
    @ExtendWith(PluginExtension.class)
    class FieldIsInitialized {
        @Plugin
        private TempFileResource resource;

        @Test
        void fileIsPresent() {
            assertThat(resource.getFile()).hasContent("some string");
        }
    }

    @Nested
    @ExtendWith(PluginExtension.class)
    class ParamaterIsInitialized {
        @Test
        void fileIsPresent(TempFileResource resource) {
            assertThat(resource.getFile()).hasContent("some string");
        }
    }
}
