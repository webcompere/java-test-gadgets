package uk.org.webcompere.testgadgets.rules;

import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import uk.org.webcompere.systemstubs.rules.EnvironmentVariablesRule;
import uk.org.webcompere.testgadgets.TestResource;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.webcompere.testgadgets.rules.Rules.*;

@RunWith(Enclosed.class)
public class RulesTest {

    public static class TestRuleBefore {
        private static int testNumber = 0;

        @Rule
        public TestRule rule = doBefore(() -> testNumber++);

        @Test
        public void theRuleFired() {
            assertThat(testNumber).isEqualTo(1);
        }
    }

    public static class TestRuleAfter {
        private static int testNumber = 0;

        @Rule
        public TestRule rule = doAfter(() -> testNumber++);

        @AfterClass
        public static void afterClass() {
            assertThat(testNumber).isEqualTo(1);
        }

        @Test
        public void theRuleFiresLater() {
            assertThat(testNumber).isZero();
        }
    }

    public static class TestRuleBoth {
        private static int testNumber = 0;

        @Rule
        public TestRule rule = asRule(() -> testNumber++, () -> testNumber--);

        @Test
        public void theRuleFired() {
            assertThat(testNumber).isEqualTo(1);
        }

        @Test
        public void theAfterRuleFiredToo() {
            assertThat(testNumber).isEqualTo(1);
        }
    }

    public static class TestRuleFromTestResource {
        private static int testNumber = 0;

        @Rule
        public TestRule rule = asRule(new TestResource() {
            @Override
            public void setup() {
                testNumber++;
            }

            @Override
            public void teardown() {
                testNumber--;
            }
        });

        @Test
        public void theRuleFired() {
            assertThat(testNumber).isEqualTo(1);
        }

        @Test
        public void theAfterRuleFiredToo() {
            assertThat(testNumber).isEqualTo(1);
        }
    }

    public static class IntegrationTestUseCase {
        private static File folder1;
        private static File folder2;
        private static TemporaryFolder temporaryFolder = new TemporaryFolder();
        private static EnvironmentVariablesRule environmentVariablesRule = new EnvironmentVariablesRule();

        @ClassRule
        public static TestRule setUpEnvironment = compose(temporaryFolder,
            doBefore(() -> folder1 = temporaryFolder.newFolder("f1")),
            doBefore(() -> folder2 = temporaryFolder.newFolder("f2")),
            environmentVariablesRule,
            doBefore(() -> environmentVariablesRule.set("FOLDER1", folder1.getAbsolutePath())
                .set("FOLDER2", folder2.getAbsolutePath())));

        @Test
        public void codeUnderTest() {
            assertThat(new File(System.getenv("FOLDER1"))).exists();
            assertThat(new File(System.getenv("FOLDER2"))).exists();
        }
    }

    public static class IntegrationTestUseCaseWithRuleChain {
        private static File folder1;
        private static File folder2;
        private static TemporaryFolder temporaryFolder = new TemporaryFolder();
        private static EnvironmentVariablesRule environmentVariablesRule = new EnvironmentVariablesRule();

        @ClassRule
        public static RuleChain setUpEnvironment = RuleChain.outerRule(temporaryFolder)
                .around(doBefore(() -> folder1 = temporaryFolder.newFolder("f1")))
                .around(doBefore(() -> folder2 = temporaryFolder.newFolder("f2")))
                .around(environmentVariablesRule)
                .around(doBefore(() -> environmentVariablesRule.set("FOLDER1", folder1.getAbsolutePath())
                    .set("FOLDER2", folder2.getAbsolutePath())));

        @Test
        public void codeUnderTest() {
            assertThat(new File(System.getenv("FOLDER1"))).exists();
            assertThat(new File(System.getenv("FOLDER2"))).exists();
        }
    }
}
