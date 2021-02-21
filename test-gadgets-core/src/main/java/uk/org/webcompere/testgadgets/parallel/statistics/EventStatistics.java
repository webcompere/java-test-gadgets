package uk.org.webcompere.testgadgets.parallel.statistics;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * POJO to hold the statistics for all the events
 * @param <T> the type of channel being monitored
 */
public class EventStatistics<T> {
    private Map<T, Double> utilizations;
    private int maxConcurrency;
    private int totalEvents;

    /**
     * Construct with event data
     * @param utilizations utilization per channel
     * @param maxConcurrency maximum concurrency reached
     * @param totalEvents the total events recorded
     */
    public EventStatistics(Map<T, Double> utilizations, int maxConcurrency, int totalEvents) {
        this.utilizations = utilizations;
        this.maxConcurrency = maxConcurrency;
        this.totalEvents = totalEvents;
    }

    /**
     * How many events were there
     * @return total events recorded
     */
    public int getTotalEvents() {
        return totalEvents;
    }

    /**
     * What was the peak concurrency reached
     * @return the maximum number of overlapping events
     */
    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    /**
     * What was the average utilization across all channels?
     * @return 0.0 - 1.0
     */
    public double getUtilization() {
        return utilizations.values()
            .stream()
            .mapToDouble(d -> d)
            .average()
            .orElse(0);
    }

    /**
     * What was the utilization of a single channel
     * @param channel the utilization of one channel
     * @return utilization 0.0 to 1.0
     */
    public double getUtilization(T channel) {
        return Optional.ofNullable(utilizations.get(channel))
            .orElseThrow(() -> new NoSuchElementException("No records for " + channel));
    }
}
