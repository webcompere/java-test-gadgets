package uk.org.webcompere.testgadgets.plugin;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import uk.org.webcompere.testgadgets.TestResource;

/**
 * A test resource that protects a real resource with reference counting
 */
public class ReferenceCountingTestResource<T extends TestResource> implements TestResource {
    private int referenceCount = 0;
    private T decoratee;

    @SuppressFBWarnings("EI2")
    public ReferenceCountingTestResource(T decoratee) {
        this.decoratee = decoratee;
    }

    /**
     * Prepare the resource for testing
     *
     * @throws Exception on error starting
     */
    @Override
    public void setup() throws Exception {
        referenceCount++;
        if (referenceCount == 1) {
            decoratee.setup();
        }
    }

    /**
     * Clean up the resource
     *
     * @throws Exception on error cleaning up
     */
    @Override
    public void teardown() throws Exception {
        referenceCount--;
        if (referenceCount == 0) {
            decoratee.teardown();
        }
        if (referenceCount < 0) {
            referenceCount = 0;
        }
    }

    /**
     * Access the decorated test resource
     * @return the decorated test resource
     */
    @SuppressFBWarnings("EI")
    public T getDecoratee() {
        return decoratee;
    }
}
