package uk.org.webcompere.testgadgets.parallel;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ParallelEnclosedRunner.class)
public class ParallelEnclosedRunnerTest {
    private static AtomicInteger total = new AtomicInteger();

    @AfterClass
    public static void afterClass() {
        assertEquals(9, total.get());
    }

    @RunWith(ParallelEnclosedRunner.class)
    @ParallelOptions(poolSize = 1)
    public static class NestedParallelWithThreadPoolOne {
        public static class SubTests {
            @Test
            public void test1() {
                total.incrementAndGet();
            }

            @Test
            public void test2() {
                total.incrementAndGet();
            }

            @Test
            public void test3() {
                total.incrementAndGet();
            }
        }

        public static class SubTests2 {
            @Test
            public void test1() {
                total.incrementAndGet();
            }

            @Test
            public void test2() throws Exception {
                // this test delays the whole run - i.e. the
                // parallel runners don't get impatient and end without each test running
                Thread.sleep(100);
                total.incrementAndGet();
            }

            @Test
            public void test3() {
                total.incrementAndGet();
            }
        }
    }

    public static class SubTests {
        @Test
        public void test1() {
            total.incrementAndGet();
        }

        @Test
        public void test2() {
            total.incrementAndGet();
        }

        @Test
        public void test3() {
            total.incrementAndGet();
        }
    }

}
