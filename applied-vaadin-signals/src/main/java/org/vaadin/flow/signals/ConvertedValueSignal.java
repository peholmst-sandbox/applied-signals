package org.vaadin.flow.signals;

import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 *
 * @param <M>
 * @param <P>
 */
@NullMarked
public class ConvertedValueSignal<M extends @Nullable Object, P extends @Nullable Object> implements ConvertedSignal<M, P> {

    private final Signal<M> modelSignal;
    private final SerializableConsumer<M> writeCallback;
    private final Converter<P, M> converter;
    private final Signal<P> presentationSignal;
    private final ValueSignal<Boolean> invalid = new ValueSignal<>(false);
    private final ValueSignal<@Nullable String> errorMessage = new ValueSignal<>(null);
    private P typedPresentationValue;

    /**
     *
     * @param modelSignal
     * @param writeCallback
     * @param converter
     */
    public ConvertedValueSignal(Signal<M> modelSignal, SerializableConsumer<M> writeCallback,
                                Converter<P, M> converter) {
        this.modelSignal = modelSignal;
        this.writeCallback = writeCallback;
        this.converter = converter;
        this.presentationSignal = () -> {
            if (requireNonNull(invalid.get())) {
                return typedPresentationValue;
            } else {
                return converter.convertToPresentation(modelSignal.get(), null);
            }
        };
    }

    @Override
    public Signal<Boolean> invalid() {
        return invalid;
    }

    @Override
    public Signal<@Nullable String> errorMessage() {
        return errorMessage;
    }

    @Override
    public Signal<M> model() {
        return modelSignal;
    }

    @Override
    public Signal<P> presentation() {
        return presentationSignal;
    }

    /**
     *
     * @param presentation
     */
    public void setPresentation(P presentation) {
        this.typedPresentationValue = presentation;
        var result = converter.convertToModel(presentation, null);
        invalid.set(result.isError());
        errorMessage.set(result.getMessage().orElse(null));
        result.ifOk(writeCallback);
    }
}
