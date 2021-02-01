package uk.org.webcompere.testgadgets;

import java.util.LinkedList;
import java.util.concurrent.Callable;

/**
 * A general purpose test resource. Subclass this to provide a generic test resource, or use
 * {@link #from} to build one
 */
public interface TestResource {
    /**
     * Prepare the resource for testing
     * @throws Exception on error starting
     */
    void setup() throws Exception;

    /**
     * Clean up the resource
     * @throws Exception on error cleaning up
     */
    void teardown() throws Exception;

    /**
     * Construct a test resource from two lambdas
     * @param before the code to run to set up a test
     * @param after the code to run after a test
     * @return
     */
    static TestResource from(ThrowingRunnable before, ThrowingRunnable after) {
        return new TestResource() {
            @Override
            public void setup() throws Exception {
                before.run();
            }

            @Override
            public void teardown() throws Exception {
                after.run();
            }
        };
    }

    /**
     * Execute this test resource around a callable. Setup will be called, but an incomplete
     * setup will not be torn down. {@link #teardown()} will be called at all costs if {@link #setup()}
     * is successful.
     * @param callable the callable to execute
     * @param <T> the type of object to return
     * @return the result of the operation
     * @throws Exception on any error thrown by the callable
     */
    default <T> T execute(Callable<T> callable) throws Exception {
        setup();
        try {
            return callable.call();
        } finally {
            teardown();
        }
    }

    /**
     * Execute this test resource around a runnable, as {@link #execute(Callable)}
     * @param runnable the runnable to execute
     * @throws Exception on any error thrown by the callable
     */
    default void execute(ThrowingRunnable runnable) throws Exception {
        execute(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Construct a single test resource made of all the others
     * @param resources resources to compose
     * @return a {@link TestResource} that executes all the others as one
     */
    static TestResource with(TestResource... resources) {
        return new TestResource() {
            private LinkedList<TestResource> resourcesSetUp = new LinkedList<>();

            @Override
            public void setup() throws Exception {
                // go through all the resources, setting them up, adding to the list
                // when they succeed
                for (TestResource resource: resources) {
                    resource.setup();

                    // keep adding to head, to ensure tear down in reverse order
                    resourcesSetUp.addFirst(resource);
                }
            }

            @Override
            public void teardown() throws Exception {
                Exception firstExceptionThrownOnTidyUp = null;
                for (TestResource resource : resourcesSetUp) {
                    try {
                        resource.teardown();
                    } catch (Exception e) {
                        firstExceptionThrownOnTidyUp = firstExceptionThrownOnTidyUp == null ?
                            e : firstExceptionThrownOnTidyUp;
                    }
                }
                if (firstExceptionThrownOnTidyUp != null) {
                    throw firstExceptionThrownOnTidyUp;
                }
            }

            @Override
            public <T> T execute(Callable<T> callable) throws Exception {
                // the composite nature of setup means that teardown must be called
                // but will still follow the rule that only complete setups are torn down
                try {
                    setup();
                    return callable.call();
                } finally {
                    teardown();
                }
            }
        };
    }
}
