package uk.org.webcompere.testgadgets.retry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class RetryerTest {

    @Mock
    private Callable<String> callable;

    @Mock
    private Retryer.ThrowingRunnable runnable;

    @Test
    void whenNothingFails() throws Exception {
        given(callable.call()).willReturn(null);

        assertNull(Retryer.retry(callable, Retryer.repeat()));

        then(callable).should().call();
    }

    @Test
    void whenItAlwaysFails() throws Exception {
        given(callable.call()).willThrow(new IOException("My exception"));

        assertThatThrownBy(() -> {
            try {
                Retryer.retry(callable, Retryer.repeat().times(10));
            } finally {
                verify(callable, times(10)).call();
            }})
            .isInstanceOf(IOException.class);
    }

    @Test
    void whenItAlwaysFailsWithError() throws Exception {
        given(callable.call()).willThrow(new OutOfMemoryError("Error!!!"));

        assertThatThrownBy(() -> {
            try {
                Retryer.retry(callable, Retryer.repeat().times(10));
            } finally {
                verify(callable, times(10)).call();
            }})
            .isInstanceOf(OutOfMemoryError.class);
    }

    @Test
    void whenItFailsAndThenPasses() throws Exception {
        given(callable.call())
            .willThrow(new IOException("My exception"))
            .willThrow(new IOException("My exception"))
            .willReturn("Hello world!");

        assertThat(Retryer.retry(callable, Retryer.repeat().times(10)))
            .isEqualTo("Hello world!");

        verify(callable, times(3)).call();
    }

    @Test
    void runnableFailsThenPasses() throws Exception {
        willThrow(new IOException("My exception"))
            .willThrow(new IOException("My exception"))
            .willDoNothing()
            .given(runnable).run();

        Retryer.retry(runnable, Retryer.repeat().times(10));
        then(runnable)
            .should(times(3)).run();
    }

    @Test
    void runnableFailsThenPassesWithGapsBetween() throws Exception {
        willThrow(new IOException("My exception"))
            .willThrow(new IOException("My exception"))
            .willDoNothing()
            .given(runnable).run();

        Retryer.retry(runnable, Retryer.repeat()
            .times(10).waitBetween(Duration.ofMillis(10)));
        then(runnable)
            .should(times(3)).run();
    }
}

