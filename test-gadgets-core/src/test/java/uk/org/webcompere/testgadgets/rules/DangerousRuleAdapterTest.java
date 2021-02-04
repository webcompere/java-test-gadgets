package uk.org.webcompere.testgadgets.rules;

import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class DangerousRuleAdapterTest {
    @Test
    void givenATestRuleItCanBeBrokenIntoSetupTearDown() throws Exception {
        // create the adapter
        DangerousRuleAdapter<TemporaryFolder> ruleAdapter = new DangerousRuleAdapter<>(new TemporaryFolder());

        // turn the rule on
        ruleAdapter.setup();

        // try to use the rule by `get`ting it from the adapter
        File file = ruleAdapter.get().getRoot();
        assertThat(file).exists();

        // turn the rule off
        ruleAdapter.teardown();

        // see the rule has tidied up
        assertThat(file).doesNotExist();
    }

    @Test
    void adapterCanBeUsedTwice() throws Exception {
        DangerousRuleAdapter<TemporaryFolder> ruleAdapter = new DangerousRuleAdapter<>(new TemporaryFolder());

        ruleAdapter.setup();
        ruleAdapter.teardown();

        ruleAdapter.setup();

        File file = ruleAdapter.get().getRoot();
        assertThat(file).exists();

        ruleAdapter.teardown();
    }
}
