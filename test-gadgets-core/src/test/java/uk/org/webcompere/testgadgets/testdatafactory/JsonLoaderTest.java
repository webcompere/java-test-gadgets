package uk.org.webcompere.testgadgets.testdatafactory;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class JsonLoaderTest {

    private static final Path JSONFILE = Paths.get("src", "test", "resources", "loader", "somejson.json");

    private static final JsonLoader JSON_LOADER = new JsonLoader();

    @Test
    void canLoadJsonToString() throws Exception {
        Catchphrase loaded = (Catchphrase) JSON_LOADER.load(JSONFILE, Catchphrase.class);

        assertThat(loaded.getName()).isEqualTo("Gadget");
        assertThat(loaded.getCatchPhrase()).isEqualTo("GadgetGadget");
    }
}
