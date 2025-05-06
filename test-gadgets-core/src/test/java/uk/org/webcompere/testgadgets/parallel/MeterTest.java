package uk.org.webcompere.testgadgets.parallel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;
import static uk.org.webcompere.testgadgets.parallel.Concurrently.executeMultiple;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class MeterTest {
    private Meter meter = new Meter();

    @Test
    void whenNothingHappensThenNoThreads() {
        assertThat(meter.getUniqueThreads()).isEmpty();
    }

    @Test
    void whenOneThingHappensThenSomeThreads() {
        meter.startEvent();

        assertThat(meter.getThreadCount()).isOne();
    }

    @Test
    void wrapSupplier() {
        // let's say this is the supplier from our real code under test
        Supplier<String> someSupplier = () -> "Hello";

        // we can replace it with a wrapped version
        Supplier<String> wrappedSupplier = () -> meter.wrapEvent(someSupplier::get);

        // then when the code under test uses it
        wrappedSupplier.get();

        // the meter is aware
        assertThat(meter.getThreadCount()).isOne();
    }

    @Test
    void whenOneThingHappensByEventWrappingThenSomeThreads() {
        // note: no checked exception
        meter.wrapEvent(() -> {});

        assertThat(meter.getThreadCount()).isOne();
    }

    @Test
    void whenOneThingHappensWithCheckedExceptionThenCheckedException() throws SomeException {
        // note: no checked exception
        meter.wrapEvent(this::returnValue);

        assertThat(meter.getThreadCount()).isOne();
    }

    @Test
    void whenOneCallableHappensByEventWrappingThenSomeThreads() {
        // note: no checked exceptions
        String val = meter.wrapEvent(() -> "Hello");
        assertThat(val).isEqualTo("Hello");

        assertThat(meter.getThreadCount()).isOne();
    }

    @Test
    void whenThrowingCallableHappensByEventWrappingThenCanCatchWithTypedExceptionHandler() throws SomeException {
        try {
            meter.wrapEvent(this::returnValue);
        } catch (SomeException e) {
            throw e;
        }
    }

    @Test
    void whenMultipleEventsHappenThenTheyAreCounted() {
        meter.wrapEvent(() -> {});
        meter.wrapEvent(() -> {});

        assertThat(meter.calculateStatistics().getTotalEvents()).isEqualTo(2);
    }

    @Test
    void calculateMaxConcurrency() {
        executeMultiple(12, () -> meter.wrapEvent(() -> Thread.sleep(100)));

        assertThat(meter.calculateStatistics().getMaxConcurrency()).isEqualTo(12);
    }

    @Test
    void calculateUtilization() {
        executeMultiple(12, () -> meter.wrapEvent(() -> Thread.sleep(100)));
        assertThat(meter.calculateStatistics().getUtilization()).isCloseTo(1.0, withPercentage(90));
    }

    @Test
    void calculateUtilizationOfOneThread() {
        executeMultiple(12, () -> meter.wrapEvent(() -> Thread.sleep(100)));
        Thread first = meter.getUniqueThreads().stream().findFirst().get();
        assertThat(meter.calculateStatistics().getUtilization(first)).isCloseTo(1.0, withPercentage(90));
    }

    @Test
    void startAndStopPointsAffectUtilization() throws Exception {
        meter.start();
        Thread.sleep(20);
        meter.wrapEvent(() -> Thread.sleep(10));
        Thread.sleep(20);
        meter.stop();

        // 50ms of sleep, 10 of which is inside the event calcs
        // bracketed by start and stop
        assertThat(meter.calculateStatistics().getUtilization()).isCloseTo(10.0 / 50.0, withPercentage(20));
    }

    // example strong exception
    private static class SomeException extends Exception {}

    private String returnValue() throws SomeException {
        return "value";
    }
}
