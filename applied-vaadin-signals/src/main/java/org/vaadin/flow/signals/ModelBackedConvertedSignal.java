package org.vaadin.flow.signals;

import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface ModelBackedConvertedSignal<P extends @Nullable Object> extends Signal<P> {

    void setPresentation(P presentation);
}
