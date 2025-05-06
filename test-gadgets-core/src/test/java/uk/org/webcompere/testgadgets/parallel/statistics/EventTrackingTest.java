package uk.org.webcompere.testgadgets.parallel.statistics;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EventTrackingTest {
    private EventTracking<String> eventTracking = new EventTracking<>();

    @Test
    void whenAddNoEventsThenNoChannels() {
        assertThat(eventTracking.getChannelCount()).isZero();
        assertThat(eventTracking.getChannels()).isEmpty();
    }

    @Test
    void whenOneChannelThenItRegisters() {
        eventTracking.addStart("chan1");
        assertThat(eventTracking.getChannelCount()).isOne();
        assertThat(eventTracking.getChannels()).containsExactly("chan1");
    }

    @Test
    void canMultiStartTheSameChannelWithoutEnd() {
        eventTracking.addStart("chan1");
        eventTracking.addStart("chan1");
    }

    @Test
    void canStartAndEndTheSameChannel() {
        eventTracking.addStart("chan1");
        eventTracking.addEnd("chan1");
        eventTracking.addStart("chan1");
        eventTracking.addEnd("chan1");
    }

    @Test
    void cannotEndAChannelWithNoEnd() {
        assertThatThrownBy(() -> eventTracking.addEnd("chan1")).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void cannotDoubleEndAChannel() {
        eventTracking.addStart("chan1");
        eventTracking.addEnd("chan1");

        assertThatThrownBy(() -> eventTracking.addEnd("chan1")).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void whenStartThenEndThenChannelIsAtFullUtilization() {
        eventTracking.addStart("c1", 0);
        eventTracking.addEnd("c1", 100);

        assertThat(eventTracking.calculateStatistics().getUtilization()).isEqualTo(1.0d);
        assertThat(eventTracking.calculateStatistics().getUtilization("c1")).isEqualTo(1.0d);
    }

    @Test
    void whenOverlappingStartsAndEndsThenChannelAtFullUtilization() {
        eventTracking.addStart("c1", 0);
        eventTracking.addEnd("c1", 100);
        eventTracking.addStart("c1", 100);
        eventTracking.addStart("c1", 100);

        assertThat(eventTracking.calculateStatistics().getUtilization()).isEqualTo(1.0d);
        assertThat(eventTracking.calculateStatistics().getUtilization("c1")).isEqualTo(1.0d);
    }

    @Test
    void whenAlmostZeroSizedEventThenUtilizationAtFull() {
        eventTracking.addStart("c1", 0);
        eventTracking.addEnd("c1", 0);

        assertThat(eventTracking.calculateStatistics().getUtilization()).isEqualTo(1.0d);
        assertThat(eventTracking.calculateStatistics().getUtilization("c1")).isEqualTo(1.0d);
    }

    @Test
    void whenStartIsEarlierThanEventThenUtilizationAffected() {
        eventTracking.start(0);
        eventTracking.addStart("c1", 100);
        eventTracking.addEnd("c1", 200);

        assertThat(eventTracking.calculateStatistics().getUtilization("c1")).isCloseTo(0.5d, withinPercentage(5));
    }

    @Test
    void aSubMillisecondEventHasUtilization() {
        eventTracking.addStart("c1", 1);
        eventTracking.addEnd("c1", 1);
        assertThat(eventTracking.calculateStatistics().getUtilization("c1")).isEqualTo(1.0d);
    }

    @Test
    void whenEndIsLaterThanEventThenUtilizationAffected() {
        eventTracking.addStart("c1", 100);
        eventTracking.addEnd("c1", 200);
        eventTracking.stop(300);

        assertThat(eventTracking.calculateStatistics().getUtilization("c1")).isCloseTo(0.5d, withinPercentage(5));
    }

    @Test
    void whensStartAndEndIsLaterThanEventThenUtilizationAffected() {
        eventTracking.start(0);
        eventTracking.addStart("c1", 100);
        eventTracking.addEnd("c1", 200);
        eventTracking.stop(300);

        assertThat(eventTracking.calculateStatistics().getUtilization("c1")).isCloseTo(0.33d, withinPercentage(5));
    }

    @Test
    void oneEventHasConcurrencyOfOne() {
        eventTracking.addStart("c1", 10);
        eventTracking.addEnd("c1", 20);

        assertThat(eventTracking.calculateStatistics().getMaxConcurrency()).isOne();
    }

    @Test
    void twoEventsAtSameTimePointCreateConcurrency() {
        eventTracking.addStart("c1", 10);
        eventTracking.addStart("c2", 10);
        eventTracking.addEnd("c1", 20);
        eventTracking.addEnd("c2", 20);

        assertThat(eventTracking.calculateStatistics().getMaxConcurrency()).isEqualTo(2);
    }

    @Test
    void twoEventsOverlappingCreateConcurrency() {
        eventTracking.addStart("c1", 10);
        eventTracking.addEnd("c1", 20);
        eventTracking.addStart("c2", 20);
        eventTracking.addEnd("c2", 40);

        assertThat(eventTracking.calculateStatistics().getMaxConcurrency()).isEqualTo(2);
    }

    @Test
    void twoDisjointEventsHaveNoConcurrency() {
        eventTracking.addStart("c1", 10);
        eventTracking.addEnd("c1", 20);
        eventTracking.addStart("c2", 21);
        eventTracking.addEnd("c2", 40);

        assertThat(eventTracking.calculateStatistics().getMaxConcurrency()).isEqualTo(1);
    }

    @Test
    void multipleEventsWhichEndAtSameMomentHaveConcurrencyWithEventJustStarting() {
        eventTracking.addStart("c1", 0);
        eventTracking.addStart("c2", 10);
        eventTracking.addStart("c3", 20);

        eventTracking.addEnd("c1", 30);
        eventTracking.addEnd("c2", 30);
        eventTracking.addEnd("c3", 30);
        eventTracking.addStart("c4", 30);

        assertThat(eventTracking.calculateStatistics().getMaxConcurrency()).isEqualTo(4);
    }

    @Test
    void multipleEventsWhichEndAtSameMomentHaveNoConcurrencyWithEventJustAfter() {
        eventTracking.addStart("c1", 0);
        eventTracking.addStart("c2", 10);
        eventTracking.addStart("c3", 20);

        eventTracking.addEnd("c1", 30);
        eventTracking.addEnd("c2", 30);
        eventTracking.addEnd("c3", 30);
        eventTracking.addStart("c4", 31);

        assertThat(eventTracking.calculateStatistics().getMaxConcurrency()).isEqualTo(3);
    }
}
