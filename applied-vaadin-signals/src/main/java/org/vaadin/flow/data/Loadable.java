package org.vaadin.flow.data;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A sealed sum type representing the four states of an asynchronous operation:
 * {@link NotLoaded}, {@link Loading}, {@link Ready}, or {@link Failed}.
 * <p>
 * Use the static factory methods {@link #notLoaded()}, {@link #loading()},
 * {@link #ready(Object)}, and {@link #failed(Exception)} to create instances.
 *
 * @param <T> the result type of the operation
 */
@NullMarked
public sealed interface Loadable<T extends @Nullable Object> {

    @SuppressWarnings("rawtypes")
    NotLoaded NOT_LOADED_INSTANCE = new NotLoaded();

    @SuppressWarnings("rawtypes")
    Loading LOADING_INSTANCE = new Loading();

    /**
     * No operation has been started yet.
     *
     * @param <T> the result type of the operation
     */
    record NotLoaded<T extends @Nullable Object>() implements Loadable<T> {
    }

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
     * Returns a cached {@link NotLoaded} instance.
     *
     * @param <T> the result type of the operation
     * @return a {@code NotLoaded} instance
     */
    @SuppressWarnings("unchecked")
    static <T extends @Nullable Object> NotLoaded<T> notLoaded() {
        return (NotLoaded<T>) NOT_LOADED_INSTANCE;
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
}
