package org.vaadin.flow.signals;

import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * A {@link ConvertedSignal} implementation backed by a {@link ValueSignal}.
 * <p>
 * This class wires a model signal to a presentation signal through a
 * {@link Converter}. When the presentation value is set via
 * {@link #setPresentation(Object)}, the converter attempts to produce a model
 * value. If conversion succeeds the model is updated through the provided write
 * callback; if it fails the {@link #invalid()} signal becomes {@code true} and
 * {@link #errorMessage()} carries the converter's error message.
 * <p>
 * The {@link #presentation()} signal is computed: while the conversion is valid
 * it derives its value from the model signal via the converter; while invalid it
 * returns the raw presentation value that caused the error, so the UI can keep
 * displaying what the user typed.
 *
 * @param <M> the model (domain) type
 * @param <P> the presentation (UI) type
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
     * Creates a new converted value signal.
     *
     * @param modelSignal   the source signal providing the model value
     * @param writeCallback a callback invoked with the converted model value
     *                      when a presentation-to-model conversion succeeds
     * @param converter     the converter used to translate between the
     *                      presentation and model types
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
     * Sets the presentation value and attempts to convert it to the model type.
     * <p>
     * If the conversion succeeds, the write callback is invoked with the
     * resulting model value and the {@link #invalid()} signal is set to
     * {@code false}. If the conversion fails, the invalid signal is set to
     * {@code true} and the {@link #errorMessage()} signal is updated with
     * the converter's error message.
     *
     * @param presentation the new presentation value to convert and apply
     */
    public void setPresentation(P presentation) {
        this.typedPresentationValue = presentation;
        var result = converter.convertToModel(presentation, null);
        invalid.set(result.isError());
        errorMessage.set(result.getMessage().orElse(null));
        result.ifOk(writeCallback);
    }
}
