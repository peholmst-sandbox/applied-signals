package org.vaadin.flow.signals;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A sealed sum type representing the three states of an asynchronous operation:
 * {@link Loading}, {@link Ready}, or {@link Failed}.
 * <p>
 * Use the static factory methods {@link #loading()}, {@link #ready(Object)},
 * and {@link #failed(Exception)} to create instances. The {@link #load(Supplier,
 * Consumer)} convenience method runs a supplier and drives a consumer through
 * the appropriate state transitions, emitting {@link Loading} only when the
 * operation takes longer than a configurable delay.
 *
 * @param <T> the result type of the operation
 */
@NullMarked
public sealed interface Loadable<T extends @Nullable Object> {

    @SuppressWarnings("rawtypes")
    Loading LOADING_INSTANCE = new Loading();

    /**
     * The operation is in progress and no result is available yet.
     *
     * @param <T> the result type of the operation
     */
    record Loading<T extends @Nullable Object>() implements Loadable<T> {
    }

    /**
     * The operation completed successfully with a result.
     *
     * @param result the result value
     * @param <T>    the result type of the operation
     */
    record Ready<T extends @Nullable Object>(T result) implements Loadable<T> {
    }

    /**
     * The operation failed with an exception.
     *
     * @param error the exception that caused the failure
     * @param <T>   the result type of the operation
     */
    record Failed<T extends @Nullable Object>(Exception error) implements Loadable<T> {
    }

    /**
     * Returns a cached {@link Loading} instance.
     *
     * @param <T> the result type of the operation
     * @return a {@code Loading} instance
     */
    @SuppressWarnings("unchecked")
    static <T extends @Nullable Object> Loading<T> loading() {
        return (Loading<T>) LOADING_INSTANCE;
    }

    /**
     * Creates a {@link Ready} instance wrapping the given result.
     *
     * @param result the result value
     * @param <T>    the result type of the operation
     * @return a {@code Ready} instance containing the result
     */
    static <T extends @Nullable Object> Ready<T> ready(T result) {
        return new Ready<>(result);
    }

    /**
     * Creates a {@link Failed} instance wrapping the given exception.
     *
     * @param error the exception that caused the failure
     * @param <T>   the result type of the operation
     * @return a {@code Failed} instance containing the exception
     */
    static <T extends @Nullable Object> Failed<T> failed(Exception error) {
        return new Failed<>(error);
    }

    /**
     * Executes the supplier and reports state transitions to the consumer,
     * using a default loading delay of 300 milliseconds.
     * <p>
     * This is equivalent to calling
     * {@link #load(Supplier, Consumer, Duration)} with a delay of 300 ms.
     * Intended to be called from a virtual thread.
     *
     * @param outcomeProvider  supplies the result value
     * @param outcomeConsumer  receives state transitions
     * @param <T>              the result type of the operation
     * @see #load(Supplier, Consumer, Duration)
     */
    static <T extends @Nullable Object> void load(Supplier<T> outcomeProvider, Consumer<Loadable<T>> outcomeConsumer) {
        load(outcomeProvider, outcomeConsumer, Duration.ofMillis(300));
    }

    /**
     * Executes the supplier and reports state transitions to the consumer.
     * <p>
     * A {@link Loading} state is emitted only if the supplier has not completed
     * within {@code loadingDelay}. This avoids brief loading-indicator flashes
     * for fast operations. If the supplier completes (successfully or with an
     * exception) before the delay elapses, only {@link Ready} or {@link Failed}
     * is emitted.
     * <p>
     * This method is intended to be called from a virtual thread. The supplier
     * runs synchronously on the calling thread; a separate virtual thread is
     * used internally for the delay timer.
     *
     * @param outcomeProvider  supplies the result value
     * @param outcomeConsumer  receives state transitions
     * @param loadingDelay     how long to wait before emitting {@link Loading}
     * @param <T>              the result type of the operation
     */
    static <T extends @Nullable Object> void load(Supplier<T> outcomeProvider, Consumer<Loadable<T>> outcomeConsumer,
                                                   Duration loadingDelay) {
        var lock = new Object();
        var done = new boolean[]{false};

        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(loadingDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            synchronized (lock) {
                if (!done[0]) {
                    outcomeConsumer.accept(loading());
                }
            }
        });

        try {
            T result = outcomeProvider.get();
            synchronized (lock) {
                done[0] = true;
                outcomeConsumer.accept(ready(result));
            }
        } catch (Exception exception) {
            synchronized (lock) {
                done[0] = true;
                outcomeConsumer.accept(failed(exception));
            }
        }
    }
}
