package org.vaadin.flow.signals;

import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.AbstractLocalSignal;
import com.vaadin.flow.signals.local.ListSignal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility methods for working with {@link Signal} instances.
 */
@NullMarked
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
    public static <T> Signal<T> nullSafe(Signal<@Nullable T> signal, T defaultValue) {
        return () -> Objects.requireNonNullElse(signal.get(), defaultValue);
    }

    public static <T> void matchItems(ListSignal<T> signal, List<T> items, Function<T, Object> identityProvider) {
        var existingSignals = signal.peek().stream().collect(Collectors.toMap(s -> identityProvider.apply(s.peek()), s -> s));
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            var existingSignal = existingSignals.remove(identityProvider.apply(item));
            if (existingSignal == null) {
                signal.insertAt(i, item);
            } else {
                signal.moveTo(existingSignal, i);
            }
        }
        existingSignals.values().forEach(signal::remove);
    }
}
