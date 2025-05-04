package uk.org.webcompere.testgadgets.runner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMember;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import uk.org.webcompere.testgadgets.plugin.AfterAction;
import uk.org.webcompere.testgadgets.plugin.BeforeAction;
import uk.org.webcompere.testgadgets.plugin.Plugin;
import uk.org.webcompere.testgadgets.plugin.TestFilter;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * The Test Wrapper adds a layer of indirection around a test class. This allows {@link Plugin} fields
 * inside the class to be defined to either filter out the running of the test under certain conditions,
 * or to perform actions before the <em>real</em> test runner gets to inspect the test class, and after
 * the entire test run has been completed.
 *
 * <p>The test runner used is the standard {@link BlockJUnit4ClassRunner}, but this can be changed by defining
 * a {@link WrapperOptions} annotation on the test class, where the target runner can be switched to something else.
 *
 */
@SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR", justification = "false positive")
public class TestWrapper extends ParentRunner<Runner> {
    private ParentRunner<?> actualRunner;

    /**
     * Constructor, called by JUnit framework reflectively
     * @param klass the type of the test class
     * @param builder the builder, used for downward building
     * @throws InitializationError on errors building the test runners
     */
    public TestWrapper(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass);

        // the real test runner we're about to construct may be affected by our setup
        beforeAll();

        WrapperOptions options = getTestClass().getAnnotation(WrapperOptions.class);
        if (options == null) {
            actualRunner = new BlockJUnit4ClassRunner(klass);
        } else {
            try {
                AnnotatedBuilder annotatedBuilder = new AnnotatedBuilder(builder);
                // make the runner from the options
                actualRunner = (ParentRunner<?>)annotatedBuilder.buildRunner(options.runWith(), klass);
            } catch (Exception e) {
                throw new InitializationError(e);
            }
        }
    }

    @Override
    protected List<Runner> getChildren() {
        if (!shouldRun()) {
            System.out.println("Filtering out: " + getTestClass().getJavaClass().getCanonicalName());
            return emptyList();
        }

        return singletonList(actualRunner);
    }

    @Override
    protected Description describeChild(Runner testWrapper) {
        return testWrapper.getDescription();
    }

    @Override
    protected void runChild(Runner testWrapper, RunNotifier runNotifier) {
        testWrapper.run(runNotifier);

        afterAll();
    }

    @SuppressWarnings("unchecked")
    private boolean shouldRun() {
        return getTestClass().getAnnotatedFields(Plugin.class)
            .stream()
            .filter(FrameworkMember::isStatic)
            .filter(field -> TestFilter.class.isAssignableFrom(field.getType()))
            .map(this::filterAsPredicate)
            .allMatch(predicate -> predicate.test(getTestClass().getJavaClass()));
    }

    private TestFilter filterAsPredicate(FrameworkField field) {
        try {
            return (TestFilter)field.getField().get(getTestClass().getJavaClass());
        } catch (IllegalAccessException e) {
            throw new TestWrapperError("Cannot apply filter in: " + field.toString(), e);
        }
    }

    private void beforeAll() {
        getTestClass().getAnnotatedFields(Plugin.class)
            .stream()
            .filter(FrameworkMember::isStatic)
            .filter(field -> BeforeAction.class.isAssignableFrom(field.getType()))
            .forEach(this::executeBeforeAction);
    }

    private void afterAll() {
        getTestClass().getAnnotatedFields(Plugin.class)
            .stream()
            .filter(FrameworkMember::isStatic)
            .filter(field -> AfterAction.class.isAssignableFrom(field.getType()))
            .forEach(this::executeAfterAction);
    }

    @SuppressWarnings("unchecked")
    private void executeBeforeAction(FrameworkField field) {
        try {
            Class<?> clazz = getTestClass().getJavaClass();
            ((BeforeAction)field.getField().get(clazz)).before(clazz);
        } catch (Throwable t) {
            throw new TestWrapperError("Cannot perform before action in: " + field.toString(), t);
        }
    }

    @SuppressWarnings("unchecked")
    private void executeAfterAction(FrameworkField field) {
        try {
            Class<?> clazz = getTestClass().getJavaClass();
            ((AfterAction)field.getField().get(clazz)).after(clazz);
        } catch (Throwable t) {
            throw new TestWrapperError("Cannot perform after action in: " + field.toString(), t);
        }
    }
}
