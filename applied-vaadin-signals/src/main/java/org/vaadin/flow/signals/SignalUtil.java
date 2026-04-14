package org.vaadin.flow.signals;

import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class SignalUtil {

    private SignalUtil() {
    }

    public static <T> Signal<@NonNull T> nullSafe(Signal<@Nullable T> signal, @NonNull T defaultValue) {
        return () -> Objects.requireNonNullElse(signal.get(), defaultValue);
    }
}
