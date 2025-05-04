package uk.org.webcompere.testgadgets.testdataloader;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextLoaderTest {

    public static class SomeType {}

    private static final Path TEXTFILE = Paths.get("src", "test", "resources", "loader", "somefile.txt");

    private static final TextLoader TEXT_LOADER = new TextLoader();

    @Test
    void cannotLoadTextIntoArbitraryType() {
        assertThatThrownBy(() -> TEXT_LOADER.load(TEXTFILE, SomeType.class))
            .isInstanceOf(IOException.class);
    }

    @Test
    void canLoadTextFileToString() throws Exception {
        String text = (String)TEXT_LOADER.load(TEXTFILE, String.class);
        assertThat(text).isEqualTo("Line 1\nLine 2");
    }

    @Test
    void canLoadTextFileToStringArray() throws Exception {
        String[] text = (String[])TEXT_LOADER.load(TEXTFILE, String[].class);
        assertThat(text).containsExactly("Line 1", "Line 2");
    }
}
