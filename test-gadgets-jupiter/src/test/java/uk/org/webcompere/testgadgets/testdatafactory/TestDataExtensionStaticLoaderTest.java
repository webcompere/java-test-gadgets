package uk.org.webcompere.testgadgets.testdatafactory;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Nested
@ExtendWith(TestDataExtension.class)
public class TestDataExtensionStaticLoaderTest {

    @Loader
    private static TestDataLoader subDirLoader = new TestDataLoader().addPath(Paths.get("subdir"));

    @Loader
    private TestDataLoader whichLoader;

    @TestData("somefile.txt")
    private String someFile;

    @Test
    void someFileComesFromSubdir() {
        assertThat(someFile).isEqualTo("subdir hello");
    }

    @Test
    void loaderIsInstantiated() {
        assertThat(whichLoader).isSameAs(subDirLoader);
    }
}
