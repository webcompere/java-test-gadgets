package uk.org.webcompere.testgadgets.parallel;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import uk.org.webcompere.testgadgets.ThrowingBiConsumer;
import uk.org.webcompere.testgadgets.ThrowingConsumer;
import uk.org.webcompere.testgadgets.ThrowingRunnable;

/**
 * Helpers for launching multiple activities at the same time for concurrent testing
 */
public class Concurrently {

    /**
     * Execute each of the given actions at (approximately) the same time during the test
     * and return control when they're all done
     * @param actions the actions to execute
     */
    public static void executeTogether(ThrowingRunnable... actions) {
        executeOver(Arrays.stream(actions), ThrowingRunnable::run);
    }

    /**
     * Execute the same action multiple times at (approximately) the same time during the test
     * and return control when all are done
     * @param count the number of threads/repeats of the action
     * @param action the action to perform
     */
    public static void executeMultiple(int count, ThrowingRunnable action) {
        executeOver(IntStream.range(0, count).boxed(), (i, j) -> action.run());
    }

    /**
     * Execute the same action multiple times at (approximately) the same time during the test
     * and return control when all are done
     * @param count the number of threads/repeats of the action
     * @param actionOnIndex the action to perform, which is also provided with which number action it is
     */
    public static void executeMultiple(int count, ThrowingConsumer<Integer> actionOnIndex) {
        executeOver(IntStream.range(0, count).boxed(), (i, index) -> actionOnIndex.accept(index));
    }

    /**
     * Execute an action over the data in a stream, each instance run at (approximately) the same time
     * during the test, returning control when everything is done
     * @param data the data to pass to each action
     * @param action the action to perform
     * @param <T> the type of the data in the stream
     */
    public static <T> void executeOver(Stream<T> data, ThrowingConsumer<T> action) {
        executeOver(data, (val, index) -> action.accept(val));
    }

    /**
     * Execute an action over the data in a stream, each instance run at (approximately) the same time
     * during the test, returning control when everything is done
     * @param data the data to pass to each action
     * @param actionOnIndex the action to perform, which also receives the index in the stream of the item
     *                      being processed
     * @param <T> the type of the data in the stream
     */
    public static <T> void executeOver(Stream<T> data, ThrowingBiConsumer<T, Integer> actionOnIndex) {
        List<T> sourceData = data.collect(toList());
        CountDownLatch startFlag = new CountDownLatch(1);
        CountDownLatch everythingFinished = new CountDownLatch(sourceData.size());
        List<Exception> errors = Collections.synchronizedList(new LinkedList<>());

        for (int i = 0; i < sourceData.size(); i++) {
            int index = i;
            new Thread(() -> executeAnAction(actionOnIndex, sourceData, startFlag, everythingFinished, errors, index))
                    .start();
        }

        startAndWaitForTheThreads(startFlag, everythingFinished);

        if (!errors.isEmpty()) {
            throw new AssertionError("There were errors in the worker threads: " + errors);
        }
    }

    private static <T> void executeAnAction(
            ThrowingBiConsumer<T, Integer> actionOnIndex,
            List<T> sourceData,
            CountDownLatch startFlag,
            CountDownLatch everythingFinished,
            List<Exception> errors,
            int index) {
        try {
            startFlag.await();

            actionOnIndex.accept(sourceData.get(index), index);
        } catch (Exception e) {
            errors.add(e);
        } finally {
            everythingFinished.countDown();
        }
    }

    private static void startAndWaitForTheThreads(CountDownLatch startFlag, CountDownLatch everythingFinished) {
        try {
            startFlag.countDown();
            everythingFinished.await();
        } catch (Exception threadException) {
            throw new AssertionError("Error awaiting the thread completion");
        }
    }
}
