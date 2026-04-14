package org.vaadin.flow.signals;

import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.vaadin.flow.data.Loadable;

/**
 * A {@link ValueSignal} holding a {@link Loadable} value, with computed
 * convenience signals for each aspect of the loading state.
 * <p>
 * The derived signals ({@link #finished()}, {@link #failed()},
 * {@link #loading()}, {@link #error()}) are computed from the current
 * {@code Loadable} value and only propagate downstream when their
 * individual values actually change.
 *
 * @param <T> the result type of the loadable operation
 * @deprecated Use {@link org.vaadin.flow.data.Loader} instead.
 */
@NullMarked
@Deprecated(forRemoval = true)
public class LoadableSignal<T extends @Nullable Object> extends ValueSignal<Loadable<T>> {

    private final Signal<Boolean> notLoaded;
    private final Signal<@Nullable T> finished;
    private final Signal<Boolean> failed;
    private final Signal<Boolean> loading;
    private final Signal<@Nullable Exception> error;

    /**
     * Creates a new loadable signal with the given initial state.
     *
     * @param initialValue the initial {@link Loadable} state
     */
    public LoadableSignal(Loadable<T> initialValue) {
        super(initialValue);
        notLoaded = Signal.computed(() -> LoadableSignal.this.get() instanceof Loadable.NotLoaded<T>);
        finished = Signal.computed(() -> {
            var value = LoadableSignal.this.get();
            if (value instanceof Loadable.Ready<T>(T result)) {
                return result;
            }
            return null;
        });
        failed = Signal.computed(() -> LoadableSignal.this.get() instanceof Loadable.Failed<T>);
        loading = Signal.computed(() -> LoadableSignal.this.get() instanceof Loadable.Loading<T>);
        error = Signal.computed(() -> {
            var value = LoadableSignal.this.get();
            if (value instanceof Loadable.Failed<T>(Exception exception)) {
                return exception;
            }
            return null;
        });
    }

    /**
     * Returns a signal that is {@code true} when in the
     * {@link Loadable.NotLoaded} state.
     *
     * @return a computed boolean signal
     */
    public Signal<Boolean> notLoaded() {
        return notLoaded;
    }

    /**
     * Returns a signal containing the result value when in the
     * {@link Loadable.Ready} state, or {@code null} otherwise.
     *
     * @return a computed signal with the result value, or {@code null}
     */
    public Signal<@Nullable T> finished() {
        return finished;
    }

    /**
     * Returns a signal that is {@code true} when in the
     * {@link Loadable.Failed} state.
     *
     * @return a computed boolean signal
     */
    public Signal<Boolean> failed() {
        return failed;
    }

    /**
     * Returns a signal that is {@code true} when in the
     * {@link Loadable.Loading} state.
     *
     * @return a computed boolean signal
     */
    public Signal<Boolean> loading() {
        return loading;
    }

    /**
     * Returns a signal containing the exception when in the
     * {@link Loadable.Failed} state, or {@code null} otherwise.
     *
     * @return a computed signal with the exception, or {@code null}
     */
    public Signal<@Nullable Exception> error() {
        return error;
    }
}
