package uk.org.webcompere.testgadgets.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.*;

@LifeCycleExtensions
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class BeforeEachNestedTestInstancePostProcessorTest {
    private static Map<String, Object> fakeDatabase = new HashMap<>();

    @BeforeEachNested
    static void cleanDatabase() {
        fakeDatabase.clear();
    }

    @Test
    void methodA() {
        assertThat(fakeDatabase).isEmpty();
        fakeDatabase.put("B", "value");
    }

    @Test
    void methodB() {
        assertThat(fakeDatabase).containsEntry("B", "value");
        fakeDatabase.put("C", "value");
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    @TestMethodOrder(MethodOrderer.Alphanumeric.class)
    class Test1 {
        @Test
        void methodA() {
            assertThat(fakeDatabase).isEmpty();
            fakeDatabase.put("A", "value");
        }

        // the second method is affected by state
        @Test
        void methodB() {
            assertThat(fakeDatabase).containsEntry("A", "value");
        }
    }

    // the second nested test gets a clean slate
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    @TestMethodOrder(MethodOrderer.Alphanumeric.class)
    class Test2 {
        @Test
        void methodA() {
            assertThat(fakeDatabase).isEmpty();
            fakeDatabase.put("A", "value");
        }

        @Test
        void methodB() {
            assertThat(fakeDatabase).containsEntry("A", "value");
        }
    }
}
