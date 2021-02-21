package uk.org.webcompere.testgadgets.parallel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.willAnswer;
import static uk.org.webcompere.testgadgets.GenericThrowingCallable.wrap;
import static uk.org.webcompere.testgadgets.parallel.Concurrently.executeMultiple;

@ExtendWith(MockitoExtension.class)
class MeterSpyExampleTest {
    private static class DoThing {
        void doThing() throws InterruptedException {
            Thread.sleep(50);
        }
    }

    @Spy
    private DoThing someAction = new DoThing();

    private Meter meter = new Meter();

    @Test
    void hookSpyToMeter() throws Exception {
        willAnswer(invocation -> meter.wrapEvent(wrap(invocation::callRealMethod)))
            .given(someAction).doThing();

        executeMultiple(12, someAction::doThing);

        assertThat(meter.getThreadCount()).isEqualTo(12);
        assertThat(meter.calculateStatistics().getMaxConcurrency()).isEqualTo(12);
    }
}
