package uk.org.webcompere.testgadgets.parallel.statistics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toMap;

/**
 * Tracks the on-off statuses of a series of objects. Thread safe when receiving events.
 * @param <T> events
 */
public class EventTracking<T> {
    private Map<T, EventLog> eventTracking = new ConcurrentHashMap<>();
    private long endTime = -1;
    private long startTime = -1;

    /**
     * Record a channel starting
     * @param channel the channel that's starting
     */
    public void addStart(T channel) {
        addStart(channel, System.currentTimeMillis());
    }

    /**
     * Record a channel starting
     * @param channel the channel that's starting
     * @param time the start timepoint
     */
    public void addStart(T channel, long time) {
        eventTracking.computeIfAbsent(channel, c -> new EventLog())
            .addStart(time);
    }

    /**
     * Record a channel ending
     * @param channel the channel that's ending
     */
    public void addEnd(T channel) {
        addEnd(channel, System.currentTimeMillis());
    }

    /**
     * Record a channel ending
     * @param channel the channel that's ending
     * @param time the end timepoint
     */
    public void addEnd(T channel, long time) {
        Optional.ofNullable(eventTracking.get(channel))
            .filter(EventLog::isRunning)
            .orElseThrow(() -> new IndexOutOfBoundsException("End provided for channel with no start"))
            .addEnd(time);
    }

    /**
     * Set a start point - this is optional. If left unstarted, the start time is taken as the earliest event.
     */
    public void start() {
        start(System.currentTimeMillis());
    }

    /**
     * Set a start point - this is optional. If left unstarted, the start time is taken as the earliest event.
     * @param atTime start time - set to -1 to mean earliest event (default)
     */
    public void start(long atTime) {
        startTime = atTime;
    }

    /**
     * Set a stop time, which may, for utilization reasons, be later than the last event
     */
    public void stop() {
        stop(System.currentTimeMillis());
    }

    /**
     * Set a stop time, which may, for utilization reasons, be later than the last event
     * @param endTime the time to set it
     */
    public void stop(long endTime) {
        this.endTime = endTime;
    }

    /**
     * Provide a safe copy of the set of all channels tracked
     * @return the channels tracked
     */
    public Set<T> getChannels() {
        return new HashSet<>(eventTracking.keySet());
    }

    /**
     * How many channels appeared
     * @return the channels
     */
    public int getChannelCount() {
        return eventTracking.size();
    }

    /**
     * Calculate all the statistics for a test to assert with
     * @return a new {@link EventStatistics} object
     */
    public EventStatistics<T> calculateStatistics() {
        long minTime = startTime != -1 ? startTime : getEarliestEventStart();
        long maxTime = endTime != -1 ? endTime : getLatestEventEnd();
        if (maxTime == minTime) {
            maxTime++;
        }

        EventLog.Timepoint<T>[] starts = getStarts();
        EventLog.Timepoint<T>[] finishes = getFinishes();

        double largestDuration = (double)maxTime - minTime;
        Map<T, Double> totalTimeSpentActive = gatherActivity(minTime, maxTime);
        calculateUtilizations(totalTimeSpentActive, largestDuration);

        return new EventStatistics<>(totalTimeSpentActive, calcMaxConcurrency(starts, finishes), starts.length);
    }

    private Map<T, Double> gatherActivity(long min, long max) {
        return eventTracking.entrySet()
            .stream()
            .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().totalActivityTime()));
    }

    private int calcMaxConcurrency(EventLog.Timepoint<T>[] starts, EventLog.Timepoint<T>[] finishes) {
        Set<T> currentlyActive = new HashSet<>();

        int startsIndex = 0;
        int finishesIndex = 0;
        EventLog.Timepoint<T> currentStart = starts[0];
        currentlyActive.add(currentStart.getChannel());
        int maxConcurrency = 1;
        EventLog.Timepoint<T> currentFinish = finishes[0];

        while (startsIndex < starts.length) {
            // is the next start event
            if (currentFinish == null ||
                (startsIndex < starts.length - 1 && starts[startsIndex + 1].getWhen() <= currentFinish.getWhen())) {
                // move the starts forward
                startsIndex++;
                if (startsIndex < starts.length) {
                    currentStart = starts[startsIndex];
                    currentlyActive.add(currentStart.getChannel());
                    maxConcurrency = Math.max(maxConcurrency, currentlyActive.size());
                }
            } else {
                // process and increment the current finish
                currentlyActive.remove(currentFinish.getChannel());
                finishesIndex++;
                if (finishesIndex < finishes.length - 1) {
                    currentFinish = finishes[finishesIndex];
                } else {
                    currentFinish = null;
                }
            }
        }

        return maxConcurrency;
    }

    @SuppressWarnings("unchecked")
    private EventLog.Timepoint<T>[] getStarts() {
        return (EventLog.Timepoint<T>[])eventTracking.entrySet()
            .stream()
            .flatMap(entry -> entry.getValue().getStarts(entry.getKey()))
            .sorted()
            .toArray(EventLog.Timepoint[]::new);
    }

    @SuppressWarnings("unchecked")
    private EventLog.Timepoint<T>[] getFinishes() {
        return (EventLog.Timepoint<T>[])eventTracking.entrySet()
            .stream()
            .flatMap(entry -> entry.getValue().getFinishes(entry.getKey()))
            .sorted()
            .toArray(EventLog.Timepoint[]::new);
    }

    private long getEarliestEventStart() {
        return eventTracking.values()
            .stream()
            .mapToLong(EventLog::getEarliest)
            .min()
            .orElse(0);
    }

    private long getLatestEventEnd() {
        return eventTracking.values()
            .stream()
            .mapToLong(EventLog::getLatest)
            .max()
            .orElse(0);
    }

    private void calculateUtilizations(Map<T, Double> totalTimeSpentActive, double largestDuration) {
        if (largestDuration == 0.0d) {
            throw new RuntimeException("Error: no time spent on anything!");
        }
        totalTimeSpentActive.forEach((key, value1) ->
            totalTimeSpentActive.compute(key, (k, value) -> value / largestDuration));
    }
}
