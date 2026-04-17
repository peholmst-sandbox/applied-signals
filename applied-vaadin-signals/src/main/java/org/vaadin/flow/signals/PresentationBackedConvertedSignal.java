package org.vaadin.flow.signals;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface PresentationBackedConvertedSignal<M extends @Nullable Object> extends Signal<Result<M>> {

    void setModel(M model);
}
