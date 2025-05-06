package uk.org.webcompere.testgadgets.parallel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.org.webcompere.testgadgets.parallel.Concurrently.*;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ConcurrentlyTest {

    @Nested
    class DifferentActivities {
        private Map<String, String> map = new ConcurrentHashMap<>();

        @Test
        void doConcurrently() {
            executeTogether(() -> map.put("a", "b"), () -> map.put("b", "c"));

            assertThat(map).containsExactlyEntriesOf(ImmutableMap.of("a", "b", "b", "c"));
        }
    }

    @Nested
    class RepeatedActivity {
        private Map<String, Integer> map = new ConcurrentHashMap<>();

        @Test
        void repeatedlyCallSameFunction() {
            executeMultiple(12, () -> increment("key"));

            assertThat(map.get("key")).isEqualTo(12);
        }

        private void increment(String value) {
            map.merge(value, 1, Integer::sum);
        }
    }

    @Nested
    class RepeatedActivityWithIndex {
        private Map<String, Integer> map = new ConcurrentHashMap<>();

        @Test
        void repeatedlyCallSameFunction() {
            executeMultiple(3, index -> incrementByItemPosition("key", index));

            assertThat(map.get("key")).isEqualTo(6);
        }

        private void incrementByItemPosition(String value, int index) {
            map.merge(value, index + 1, Integer::sum);
        }
    }

    @Nested
    class TestWithData {
        private Multiset<String> set = ConcurrentHashMultiset.create();

        @Test
        void addDataSimultaneously() {
            executeOver(Stream.of("a", "b", "c", "c", "d"), val -> set.add(val));

            assertThat(set.stream()).containsExactlyInAnyOrder("a", "b", "c", "c", "d");
        }
    }

    @Nested
    class TestWithDataAndIndex {
        private Multiset<String> set = ConcurrentHashMultiset.create();

        @Test
        void addDataSimultaneously() {
            executeOver(Stream.of("a", "b", "c", "c", "d"), (value, index) -> set.add(value + index));

            assertThat(set.stream()).containsExactlyInAnyOrder("a0", "b1", "c2", "c3", "d4");
        }
    }

    @Nested
    class ErrorHandling {
        @Test
        void whenErrorInWorkerThenAssertionError() {
            assertThatThrownBy(() -> {
                        executeMultiple(10, () -> {
                            throw new RuntimeException("Boom");
                        });
                    })
                    .isInstanceOf(AssertionError.class);
        }
    }
}
