package uk.org.webcompere.testgadgets.parallel;

import uk.org.webcompere.testgadgets.GenericThrowingCallable;
import uk.org.webcompere.testgadgets.GenericThrowingRunnable;
import uk.org.webcompere.testgadgets.parallel.statistics.EventStatistics;
import uk.org.webcompere.testgadgets.parallel.statistics.EventTracking;

import java.util.Set;

/**
 * A meter can be used to watch a concurrent process. By making each worker in the process call
 * either {@link Meter#startEvent()} and {@link Meter#endEvent()} or by wrapping each operation to
 * be watched in a call to {@link Meter#wrapEvent}, the meter will track the usage of each {@link Thread}.
 * At the end of the operation call {@link Meter#calculateStatistics()} to discover maximum concurrency and
 * total utilizations.<br>
 * For utilizations, the start and end time are taken as the earliest known start and end, but calling
 * {@link Meter#start()} before starting the test will set a start point, and calling {@link Meter#stop()}
 * after the operations are all complete will set an end point.
 */
public class Meter {
    private EventTracking<Thread> eventTracking = new EventTracking<>();

    public Set<Thread> getUniqueThreads() {
        return eventTracking.getChannels();
    }

    /**
     * Register that an event has started - this will mark the current thread
     * as busy and track the busy status until {@link #endEvent()} is called by the same
     * thread. If {@link #startEvent()} is called again before {@link #endEvent()}, then
     * the thread will be marked as having been active for 1ms.
     */
    public void startEvent() {
        eventTracking.addStart(Thread.currentThread());
    }

    /**
     * Register that an event on this thread has ended, and record the utilization etc
     */
    public void endEvent() {
        eventTracking.addEnd(Thread.currentThread());
    }

    /**
     * Do an event recording the start/end automatically
     * @param event the <code>void</code> function to wrap
     * @param <E> the type of exception thrown
     * @throws E as thrown by event - may be {@link RuntimeException}
     */
    public <E extends Exception> void wrapEvent(GenericThrowingRunnable<E> event) throws E {
        wrapEvent(event.asCallable());
    }

    /**
     * Do an event, recording the start/end automatically
     * @param event the function to wrap
     * @param <T> the type of value returned
     * @param <E> exception type
     * @return the return of the function
     * @throws E as thrown by event - may be {@link RuntimeException}
     */
    public <T, E extends Exception> T wrapEvent(GenericThrowingCallable<T, E> event) throws E {
        try {
            startEvent();
            return event.call();
        } finally {
            endEvent();
        }
    }

    /**
     * How many threads were involved
     * @return the total number of threads seen
     */
    public int getThreadCount() {
        return eventTracking.getChannelCount();
    }

    /**
     * Calculate the statistics for the events so far
     * @return the {@link EventStatistics} for working out utilisation etc
     */
    public EventStatistics<Thread> calculateStatistics() {
        return eventTracking.calculateStatistics();
    }

    /**
     * Clock the start point
     */
    public void start() {
        eventTracking.start();
    }

    /**
     * Clock the stop point
     */
    public void stop() {
        eventTracking.stop();
    }
}
