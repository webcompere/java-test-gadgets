package uk.org.webcompere.testgadgets.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import uk.org.webcompere.testgadgets.rules.DangerousRuleAdapter;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PluginExtension.class)
public class DangerousRuleAdapterExampleTest {
    @Plugin
    private DangerousRuleAdapter<TemporaryFolder> adapter = new DangerousRuleAdapter<>(new TemporaryFolder());

    @Test
    void theFolderWorks() {
        assertThat(adapter.get().getRoot()).exists();
    }
}
