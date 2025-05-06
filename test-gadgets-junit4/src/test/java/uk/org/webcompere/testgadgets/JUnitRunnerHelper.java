package uk.org.webcompere.testgadgets;

import java.util.ArrayList;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class JUnitRunnerHelper extends RunListener {
    private List<String> testOrder = new ArrayList<>();
    private int assumptionFailures = 0;

    @Override
    public void testStarted(Description description) {
        testOrder.add(description.getMethodName());
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        super.testAssumptionFailure(failure);
        assumptionFailures++;
    }

    public Result runTestsAndCaptureTestOrder(Class<?> testClass) {
        JUnitCore engine = new JUnitCore();
        engine.addListener(this);
        return engine.run(testClass);
    }

    public int getTestIndex(String name) {
        return testOrder.indexOf(name);
    }

    public int getAssumptionFailures() {
        return assumptionFailures;
    }

    public int getMethodCount() {
        return testOrder.size();
    }
}
