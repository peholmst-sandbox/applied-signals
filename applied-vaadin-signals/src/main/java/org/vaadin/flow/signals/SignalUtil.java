package org.vaadin.flow.signals;

import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Utility methods for working with {@link Signal} instances.
 */
public final class SignalUtil {

    private SignalUtil() {
    }

    /**
     * Wraps a nullable signal so that it never returns {@code null},
     * substituting {@code defaultValue} whenever the underlying signal
     * produces {@code null}.
     *
     * @param signal       the source signal whose value may be {@code null}
     * @param defaultValue the non-null value to use when the source is {@code null}
     * @param <T>          the value type
     * @return a new signal that always returns a non-null value
     */
    public static <T> Signal<@NonNull T> nullSafe(Signal<@Nullable T> signal, @NonNull T defaultValue) {
        return () -> Objects.requireNonNullElse(signal.get(), defaultValue);
    }
}
