package org.vaadin.flow.signals;

import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

@NullMarked
public interface ResultDemux<V extends @Nullable Object> extends Serializable {

    Signal<Boolean> invalid();

    Signal<@Nullable String> errorMessage();

    Signal<V> valueOrElse(V other);
}
