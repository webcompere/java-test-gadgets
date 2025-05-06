package uk.org.webcompere.testgadgets.testdataloader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TestDataLoaderTest {

    @Mock
    private ObjectLoader mockLoader;

    @Test
    void loaderCanLoadTextFile() throws Exception {
        var loader = new TestDataLoader();
        loader.addPath(Paths.get("loader"));
        String text = loader.load(Paths.get("somefile.txt"), String.class, false);

        assertThat(text).isEqualTo("Line 1\nLine 2");
    }

    @Test
    void loaderCanLoadTextFileUsingDefaultExtension() throws Exception {
        var loader = new TestDataLoader();
        loader.addPath(Paths.get("loader"));
        loader.setDefaultExtension(".txt");
        String text = loader.load(Paths.get("somefile"), String.class, false);

        assertThat(text).isEqualTo("Line 1\nLine 2");
    }

    @Test
    void loaderCannotLoadUnknownFileExtension() {
        var loader = new TestDataLoader();
        loader.addPath(Paths.get("loader"));
        assertThatThrownBy(() -> loader.load(Paths.get("somefile.xls"), String.class, false))
            .isInstanceOf(IOException.class);
    }

    @Test
    void loaderCannotLoadUnknownFileExtensionWhenCached() {
        var loader = new TestDataLoader();
        loader.addPath(Paths.get("loader"));
        assertThatThrownBy(() -> loader.load(Paths.get("somefile.xls"), String.class, true))
            .isInstanceOf(IOException.class);
    }

    @Test
    void loadingTwiceWithNoCacheProducesDifferentObjects() throws Exception {
        var loader = new TestDataLoader();
        loader.addPath(Paths.get("loader"));
        String text1 = loader.load(Paths.get("somefile.txt"), String.class, false);
        String text2 = loader.load(Paths.get("somefile.txt"), String.class, false);

        assertThat(text1).isNotSameAs(text2);
    }

    @Test
    void loadingTwiceWithCacheProducesSameObject() throws Exception {
        var loader = new TestDataLoader();
        loader.addPath(Paths.get("loader"));
        String text1 = loader.load(Paths.get("somefile.txt"), String.class, true);
        String text2 = loader.load(Paths.get("somefile.txt"), String.class, true);

        assertThat(text1).isSameAs(text2);
    }

    @Test
    void defaultsToMutableObjects() {
        assertThat(new TestDataLoader().getImmutableMode()).isEqualTo(Immutable.MUTABLE);
    }

    @Test
    void canBeChangedToImmutable() {
        var loader = new TestDataLoader();
        loader.setImmutableMode(Immutable.IMMUTABLE);
        assertThat(loader.getImmutableMode()).isEqualTo(Immutable.IMMUTABLE);
    }

    @Test
    void canBeChangedToMutable() {
        var loader = new TestDataLoader();
        loader.setImmutableMode(Immutable.MUTABLE);
        assertThat(loader.getImmutableMode()).isEqualTo(Immutable.MUTABLE);
    }

    @Test
    void whenChangedToDefaultIsTreatedAsMutable() {
        var loader = new TestDataLoader();
        loader.setImmutableMode(Immutable.DEFAULT);
        assertThat(loader.getImmutableMode()).isEqualTo(Immutable.MUTABLE);
    }

    @Test
    void canAddObjectLoader() throws Exception {
        var loader = new TestDataLoader().addLoader(".TXT", mockLoader);

        loader.load(Paths.get("somefile.txt"), String.class, false);

        then(mockLoader).should().load(any(), any());
    }
}
