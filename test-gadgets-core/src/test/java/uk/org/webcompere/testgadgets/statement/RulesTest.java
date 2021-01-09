package uk.org.webcompere.testgadgets.statement;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.org.webcompere.testgadgets.statement.Rules.executeWithRule;
import static uk.org.webcompere.testgadgets.statement.Rules.withRules;

class RulesTest {

    @Test
    void canExecuteUsingJUnit4Rule() throws Exception {
        // given the temporary folder doesn't exist at first
        TemporaryFolder tempFolderRule = new TemporaryFolder();
        assertThatThrownBy(tempFolderRule::getRoot)
            .isInstanceOf(IllegalStateException.class);

        executeWithRule(tempFolderRule, () -> {
            // the temp folder should exist within the confines of the rule
            assertThat(tempFolderRule.getRoot()).exists();
        });
    }

    @Test
    void anExceptionThrownInARuleCanBeCaught() {
        TemporaryFolder tempFolderRule = new TemporaryFolder();

        assertThatThrownBy(() -> executeWithRule(tempFolderRule, () -> {
            // simulate something going wrong
            throw new IOException("bang");
        })).isInstanceOf(IOException.class);
    }

    @Test
    void anAssertionFailureThrownInARuleMakesSense() {
        TemporaryFolder tempFolderRule = new TemporaryFolder();

        assertThatThrownBy(() -> executeWithRule(tempFolderRule, () -> {
            Assertions.fail("This is a fail");
        })).isInstanceOf(AssertionError.class);
    }

    @Test
    void canExtractAReturnValueFromTheCodeUnderTest() throws Exception {
        TemporaryFolder tempFolderRule = new TemporaryFolder();

        assertThat(executeWithRule(tempFolderRule, () -> {
            // this is the test code
            return "some value";
        })).isEqualTo("some value");
    }

    @Test
    void canExecuteWithMultipleRules() throws Exception {
        TemporaryFolder tempFolderRule1 = new TemporaryFolder();
        TemporaryFolder tempFolderRule2 = new TemporaryFolder();

        Set<String> folders = new HashSet<>();

        withRules(tempFolderRule1, tempFolderRule2)
            .execute(() -> {
                folders.add(tempFolderRule1.getRoot().getAbsolutePath());
                folders.add(tempFolderRule2.getRoot().getAbsolutePath());
            });

        assertThat(folders).hasSize(2);
    }

    @Test
    void canExecuteAndReturnWithMultipleRules() throws Exception {
        TemporaryFolder tempFolderRule1 = new TemporaryFolder();
        TemporaryFolder tempFolderRule2 = new TemporaryFolder();

        Set<String> returnedFolders = withRules(tempFolderRule1, tempFolderRule2)
            .execute(() -> {
                Set<String> folders = new HashSet<>();
                folders.add(tempFolderRule1.getRoot().getAbsolutePath());
                folders.add(tempFolderRule2.getRoot().getAbsolutePath());
                return folders;
             });

        assertThat(returnedFolders).hasSize(2);
    }
}
