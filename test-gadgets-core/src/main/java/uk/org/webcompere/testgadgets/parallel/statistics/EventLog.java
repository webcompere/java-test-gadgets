package uk.org.webcompere.testgadgets.parallel.statistics;

import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Quickly store switch on and off events for a channel, and provide support for calculating
 * statistics afterwards.
 */
public class EventLog {
    private LinkedList<Event> events = new LinkedList<>();

    /**
     * A timepoint for a channel, used to help with establish the switch on/off timeline
     * @param <T> the channel
     */
    static final class Timepoint<T> implements Comparable<Timepoint<T>> {
        private final long when;
        private final T channel;

        public Timepoint(T channel, long when) {
            this.channel = channel;
            this.when = when;
        }

        /**
         * Which channel
         * @return the channel
         */
        public T getChannel() {
            return channel;
        }

        /**
         * When did the operation happen
         * @return timepoint
         */
        public long getWhen() {
            return when;
        }

        /**
         * Sortable into ascending order
         * @param other the other one
         * @return the comparison
         */
        @Override
        public int compareTo(Timepoint<T> other) {
            return Long.compare(when, other.when);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Timepoint<?> timepoint = (Timepoint<?>) o;
            return when == timepoint.when && Objects.equals(channel, timepoint.channel);
        }

        @Override
        public int hashCode() {
            return Objects.hash(when, channel);
        }
    }

    /**
     * An event object
     */
    static class Event {
        private long start;
        private long end;

        Event(long start) {
            this(start, -1);
        }

        Event(long start, long end) {
            this.start = start;
            this.end = end;
        }

        long getStart() {
            return start;
        }

        void setEnd(long end) {
            this.end = end;
        }

        long getEffectiveEnd() {
            if (end == -1 || end == start) {
                return start + 1;
            }
            return end;
        }

        boolean isRunning() {
            return end == -1;
        }

        void merge(Event event) {
            end = event.getEffectiveEnd();
        }

        boolean overlaps(Event event) {
            return event.getStart() <= start || event.getStart() <= end;
        }

        Event copy() {
            return new Event(start, end);
        }
    }

    /**
     * Add a starting event
     * @param start the start time
     */
    public void addStart(long start) {
        events.add(new Event(start));
    }

    /**
     * Add an ending event time to the last event
     * @param end the start time
     */
    public void addEnd(long end) {
        events.getLast().setEnd(end);
    }

    /**
     * Return whether the last event has a finish time yet
     * @return true if the last event is started and not finished
     */
    public boolean isRunning() {
        return events.getLast().isRunning();
    }

    /**
     * Get the earliest start time
     * @return the first event's start
     */
    public long getEarliest() {
        return events.getFirst().getStart();
    }

    /**
     * Get the latest end time
     * @return the last event's end
     */
    public long getLatest() {
        return events.getLast().getEffectiveEnd();
    }

    /**
     * Provide all start times, adding in the channel the outside world thinks
     * they relate to
     * @param channel the channel identifier
     * @param <T> the type of channel
     * @return all start times as a stream
     */
    public <T> Stream<Timepoint<T>> getStarts(T channel) {
        return events.stream().map(event -> new Timepoint<>(channel, event.getStart()));
    }

    /**
     * Get all finish times
     * @param channel the channel identifier
     * @param <T> the type of channel
     * @return all finish times as a stream
     */
    public <T> Stream<Timepoint<T>> getFinishes(T channel) {
        return events.stream().map(event -> new Timepoint<>(channel, event.getEffectiveEnd()));
    }

    /**
     * Work out the total time spent by this channel. Avoid double counting consecutive events which overlap
     * @return the total time spent doing things
     */
    public double totalActivityTime() {
        LinkedList<Event> simplified = new LinkedList<>();
        events.forEach(event -> mergeInto(event, simplified));

        return simplified.stream()
            .mapToDouble(event -> event.getEffectiveEnd() - event.getStart())
            .sum();
    }

    private static void mergeInto(Event event, LinkedList<Event> simplified) {
        if (!simplified.isEmpty() && simplified.getLast().overlaps(event)) {
            // merge
            simplified.getLast().merge(event);
        } else {
            simplified.add(event.copy());
        }
    }
}
