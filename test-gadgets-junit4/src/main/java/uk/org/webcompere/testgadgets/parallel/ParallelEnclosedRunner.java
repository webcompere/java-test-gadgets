package uk.org.webcompere.testgadgets.parallel;

import static org.junit.Assert.fail;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.experimental.runners.Enclosed;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.TestClass;

/**
 * A parallel runner for internal test classes. Example:<br>
 * <pre><code class="java">
 *     &#064;RunWith(ParallelEnclosedRunner.class)
 *     public class MyTest {
 *         public static class TestClass1 {
 *             // tests in here run linearly, but the other
 *             // test class also runs in parallel
 *             &#064;Test
 *             public void test1() {
 *             }
 *         }
 *
 *         &#064;RunWith(SomeOtherRunner.class)
 *         public static class TestClass2 {
 *             // test in here run in parallel with tests from the other class
 *             &#064;Test
 *             public void test2() {
 *             }
 *         }
 *     }
 * </code></pre>
 *
 * <p>The thread pool size defaults to {@link ParallelOptions#DEFAULT_POOL_SIZE} but can be
 * changed by annotating the class with a {@link ParallelOptions} annotation. E.g.:
 * <pre><code class="java">
 *     &#064;RunWith(ParallelEnclosedRunner.class)
 *     &#064;ParallelOptions(poolSize=99)
 *     public class MyTest {
 *     }
 * </code></pre>
 */
public class ParallelEnclosedRunner extends Enclosed {
    private static class Scheduler implements RunnerScheduler {
        private ExecutorService parallel;

        public Scheduler(int poolSize) {
            parallel = Executors.newFixedThreadPool(poolSize);
        }

        @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
        @Override
        public void schedule(Runnable childStatement) {
            parallel.submit(childStatement);
        }

        @Override
        public void finished() {
            parallel.shutdown();
            try {
                if (!parallel.awaitTermination(2, TimeUnit.MINUTES)) {
                    fail("Parallel tests failed to leave threadpool");
                }
            } catch (InterruptedException e) {
                fail("Parallel tests were interrupted");
            }
        }
    }

    /**
     * Only called reflectively. Do not use programmatically.
     */
    public ParallelEnclosedRunner(Class<?> klass, RunnerBuilder builder) throws Throwable {
        super(klass, builder);
        setScheduler(new Scheduler(poolSize(klass)));
    }

    private static int poolSize(Class<?> klass) {
        TestClass testClass = new TestClass(klass);
        ParallelOptions options = testClass.getAnnotation(ParallelOptions.class);
        if (options == null) {
            return ParallelOptions.DEFAULT_POOL_SIZE;
        } else {
            return options.poolSize();
        }
    }
}
