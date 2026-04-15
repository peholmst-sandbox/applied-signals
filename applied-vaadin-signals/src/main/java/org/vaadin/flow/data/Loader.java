package org.vaadin.flow.data;

import com.vaadin.flow.function.SerializableConsumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A callback-driven controller for tracking the state of an asynchronous
 * operation through the {@link Loadable} lifecycle.
 * <p>
 * When the {@link Loadable} value changes, the loader invokes the registered
 * callbacks so that the UI (or any other consumer) can react to each state:
 * <ul>
 *   <li>{@code setNotLoaded} &mdash; called with {@code true} when in
 *       {@link Loadable.NotLoaded}, {@code false} otherwise</li>
 *   <li>{@code setLoading} &mdash; called with {@code true} when in
 *       {@link Loadable.Loading}, {@code false} otherwise</li>
 *   <li>{@code setReady} &mdash; called with the result when in
 *       {@link Loadable.Ready}; called with {@code null} when leaving Ready
 *       unless {@code retainResult} is {@code true}</li>
 *   <li>{@code setError} &mdash; called with the exception when in
 *       {@link Loadable.Failed}, {@code null} otherwise</li>
 *   <li>{@code setFailed} &mdash; called with {@code true} when in
 *       {@link Loadable.Failed}, {@code false} otherwise</li>
 * </ul>
 * Every callback is optional (may be {@code null}) and is only invoked when
 * the value actually changes (equality is checked via {@link Objects#equals}).
 * <p>
 * Use the {@link #load(Supplier)} or {@link #load(Supplier, Duration)} methods
 * to execute an asynchronous operation. A {@link Loadable.Loading} state is
 * emitted only if the supplier takes longer than a configurable delay (default
 * 300&nbsp;ms), avoiding brief loading-indicator flashes for fast operations.
 * <p>
 * Instances are best created via the {@link Builder}:
 * <pre>{@code
 * Loader<String> loader = new Loader.Builder<String>()
 *     .bindLoading(spinner::setVisible)
 *     .bindReady(label::setText)
 *     .bindError(err -> errorLabel.setText(err.getMessage()))
 *     .build();
 *
 * loader.load(() -> service.fetchData());
 * }</pre>
 *
 * @param <T> the result type of the operation
 * @see Loadable
 * @see Builder
 */
@NullMarked
public class Loader<T extends @Nullable Object> implements Serializable {

    private final boolean retainResult;
    private final @Nullable SerializableConsumer<Boolean> setNotLoaded;
    private final @Nullable SerializableConsumer<Boolean> setLoading;
    private final @Nullable SerializableConsumer<@Nullable T> setReady;
    private final @Nullable SerializableConsumer<@Nullable Exception> setError;
    private final @Nullable SerializableConsumer<Boolean> setFailed;

    private Loadable<T> value;

    /**
     * Creates a new loader with the given initial value, retention policy,
     * and callbacks.
     * <p>
     * All callbacks are invoked immediately to reflect the initial state.
     * Prefer {@link Builder} over this constructor for readability.
     *
     * @param initialValue the starting {@link Loadable} state
     * @param retainResult if {@code true}, the {@code setReady} callback is
     *                     <em>not</em> called with {@code null} when
     *                     transitioning away from {@link Loadable.Ready}
     * @param setNotLoaded callback for the not-loaded flag, or {@code null}
     * @param setLoading   callback for the loading flag, or {@code null}
     * @param setReady     callback for the result value, or {@code null}
     * @param setError     callback for the error exception, or {@code null}
     * @param setFailed    callback for the failed flag, or {@code null}
     */
    public Loader(Loadable<T> initialValue,
                  boolean retainResult,
                  @Nullable SerializableConsumer<Boolean> setNotLoaded,
                  @Nullable SerializableConsumer<Boolean> setLoading,
                  @Nullable SerializableConsumer<@Nullable T> setReady,
                  @Nullable SerializableConsumer<@Nullable Exception> setError,
                  @Nullable SerializableConsumer<Boolean> setFailed) {
        this.retainResult = retainResult;
        this.setNotLoaded = setNotLoaded;
        this.setLoading = setLoading;
        this.setReady = setReady;
        this.setError = setError;
        this.value = initialValue;
        this.setFailed = setFailed;
        callback();
    }

    /**
     * Sets the {@link Loadable} value and invokes callbacks if the value has
     * changed. If the new value is {@linkplain Objects#equals equal} to the
     * current value, no callbacks are fired.
     *
     * @param value the new loadable state
     */
    public void set(Loadable<T> value) {
        if (!Objects.equals(this.value, value)) {
            this.value = value;
            callback();
        }
    }

    /**
     * Executes the supplier and drives the loader through the appropriate
     * {@link Loadable} state transitions, using a default loading delay of
     * 300&nbsp;milliseconds.
     * <p>
     * This is equivalent to calling {@link #load(Supplier, Duration)} with
     * a delay of 300&nbsp;ms.
     *
     * @param outcomeProvider supplies the result value
     * @see #load(Supplier, Duration)
     */
    public void load(Supplier<T> outcomeProvider) {
        load(outcomeProvider, Duration.ofMillis(300));
    }

    /**
     * Executes the supplier and drives the loader through the appropriate
     * {@link Loadable} state transitions.
     * <p>
     * A {@link Loadable.Loading} state is emitted only if the supplier has not
     * completed within {@code loadingDelay}. This avoids brief
     * loading-indicator flashes for fast operations. If the supplier completes
     * (successfully or with an exception) before the delay elapses, only
     * {@link Loadable.Ready} or {@link Loadable.Failed} is emitted.
     * <p>
     * The supplier runs synchronously on the calling thread; a separate virtual
     * thread is used internally for the delay timer.
     *
     * @param outcomeProvider supplies the result value
     * @param loadingDelay    how long to wait before emitting
     *                        {@link Loadable.Loading}
     */
    public void load(Supplier<T> outcomeProvider, Duration loadingDelay) {
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
                    set(Loadable.loading());
                }
            }
        });

        try {
            T result = outcomeProvider.get();
            synchronized (lock) {
                done[0] = true;
                set(Loadable.ready(result));
            }
        } catch (Exception exception) {
            synchronized (lock) {
                done[0] = true;
                set(Loadable.failed(exception));
            }
        }
    }

    private void callback() {
        setNotLoaded(value instanceof Loadable.NotLoaded<T>);
        setLoading(value instanceof Loadable.Loading<T>);
        if (value instanceof Loadable.Ready(T result)) {
            setReady(result);
        } else if (!retainResult) {
            setReady(null);
        }
        if (value instanceof Loadable.Failed(Exception error)) {
            setError(error);
        } else {
            setError(null);
        }
    }

    private void setNotLoaded(boolean notLoaded) {
        if (setNotLoaded != null) {
            setNotLoaded.accept(notLoaded);
        }
    }

    private void setLoading(boolean loading) {
        if (setLoading != null) {
            setLoading.accept(loading);
        }
    }

    private void setReady(@Nullable T result) {
        if (setReady != null) {
            setReady.accept(result);
        }
    }

    private void setError(@Nullable Exception cause) {
        if (setError != null) {
            setError.accept(cause);
        }
        if (setFailed != null) {
            setFailed.accept(cause != null);
        }
    }

    /**
     * A fluent builder for constructing {@link Loader} instances.
     * <p>
     * All callbacks are optional. By default, {@code retainResult} is
     * {@code true} and the initial state is {@link Loadable.NotLoaded}.
     * <p>
     * Example:
     * <pre>{@code
     * Loader<Order> loader = new Loader.Builder<Order>()
     *     .bindNotLoaded(placeholder::setVisible)
     *     .bindLoading(spinner::setVisible)
     *     .bindReady(grid::setItems)
     *     .bindError(err -> Notification.show(err.getMessage()))
     *     .bindFailed(errorPanel::setVisible)
     *     .setRetainResult(false)
     *     .build();
     * }</pre>
     *
     * @param <T> the result type of the operation
     */
    public static class Builder<T extends @Nullable Object> {

        private @Nullable SerializableConsumer<Boolean> setNotLoaded;
        private @Nullable SerializableConsumer<Boolean> setLoading;
        private @Nullable SerializableConsumer<@Nullable T> setReady;
        private @Nullable SerializableConsumer<@Nullable Exception> setError;
        private @Nullable SerializableConsumer<Boolean> setFailed;
        private boolean retainResult = true;

        /**
         * Binds a callback that is invoked with {@code true} when the loader
         * enters the {@link Loadable.NotLoaded} state and {@code false}
         * otherwise.
         *
         * @param setNotLoaded the callback
         * @return this builder
         */
        public Builder<T> bindNotLoaded(SerializableConsumer<Boolean> setNotLoaded) {
            this.setNotLoaded = setNotLoaded;
            return this;
        }

        /**
         * Binds a callback that is invoked with {@code true} when the loader
         * enters the {@link Loadable.Loading} state and {@code false}
         * otherwise.
         *
         * @param setLoading the callback
         * @return this builder
         */
        public Builder<T> bindLoading(SerializableConsumer<Boolean> setLoading) {
            this.setLoading = setLoading;
            return this;
        }

        /**
         * Binds a callback that is invoked with the result value when the
         * loader enters the {@link Loadable.Ready} state. When leaving the
         * Ready state, the callback is invoked with {@code null} unless
         * {@link #setRetainResult(boolean) retainResult} is {@code true}.
         *
         * @param setReady the callback
         * @return this builder
         */
        public Builder<T> bindReady(SerializableConsumer<@Nullable T> setReady) {
            this.setReady = setReady;
            return this;
        }

        /**
         * Binds a callback that is invoked with the exception when the loader
         * enters the {@link Loadable.Failed} state and with {@code null}
         * otherwise.
         *
         * @param setError the callback
         * @return this builder
         */
        public Builder<T> bindError(SerializableConsumer<@Nullable Exception> setError) {
            this.setError = setError;
            return this;
        }

        /**
         * Binds a callback that is invoked with {@code true} when the loader
         * enters the {@link Loadable.Failed} state and {@code false}
         * otherwise.
         *
         * @param setFailed the callback
         * @return this builder
         */
        public Builder<T> bindFailed(SerializableConsumer<Boolean> setFailed) {
            this.setFailed = setFailed;
            return this;
        }

        /**
         * Sets whether the last successful result should be retained when
         * the loader transitions away from {@link Loadable.Ready}.
         * <p>
         * When {@code true} (the default), the {@code setReady} callback is
         * <em>not</em> called with {@code null} on state transitions away from
         * Ready, allowing the UI to continue displaying stale data while a
         * reload is in progress.
         *
         * @param retainResult {@code true} to retain the result, {@code false}
         *                     to clear it
         * @return this builder
         */
        public Builder<T> setRetainResult(boolean retainResult) {
            this.retainResult = retainResult;
            return this;
        }

        /**
         * Builds a new {@link Loader} with the given initial state.
         *
         * @param initialValue the starting {@link Loadable} state
         * @return a new loader
         */
        public Loader<T> build(Loadable<T> initialValue) {
            return new Loader<>(initialValue, retainResult, setNotLoaded, setLoading, setReady, setError, setFailed);
        }

        /**
         * Builds a new {@link Loader} starting in the
         * {@link Loadable.NotLoaded} state.
         *
         * @return a new loader
         */
        public Loader<T> build() {
            return build(Loadable.notLoaded());
        }
    }
}
