package org.vaadin.flow.signals;

import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A converted {@link Signal} that exposes a <em>presentation</em> view over
 * storage that holds a <em>model</em> value wrapped in a
 * {@link com.vaadin.flow.data.binder.Result Result}.
 *
 * <p>The underlying storage is model-authoritative: the converter's
 * {@link com.vaadin.flow.data.converter.Converter#convertToPresentation(Object, com.vaadin.flow.data.binder.ValueContext)
 * convertToPresentation} is used to derive the presentation value returned by
 * {@link #get()}, and {@link #setPresentation(Object)} writes back through the
 * converter's
 * {@link com.vaadin.flow.data.converter.Converter#convertToModel(Object, com.vaadin.flow.data.binder.ValueContext)
 * convertToModel} so that the model stays the source of truth.
 *
 * <p>When the current model value is an error (i.e. a previous conversion from
 * presentation to model failed), {@link #get()} falls back to the presentation
 * value most recently passed to {@link #setPresentation(Object)}. This lets a
 * bound field retain the user's invalid input while validation errors are
 * displayed, instead of reverting to the last successfully converted value.
 *
 * <p>Typical use is to bind an input field to a model-typed signal through a
 * {@link com.vaadin.flow.data.converter.Converter}. Validation state can be
 * observed by demultiplexing the backing {@code Result} signal with
 * {@link SignalUtil#demuxResult(Signal)}. Example:
 * <pre>{@code
 * var emailSignal = new ValueSignal<Result<@Nullable Email>>(Result.ok(null));
 * var emailDemux = SignalUtil.demuxResult(emailSignal);
 * var emailPresentation = SignalUtil.modelBacked(new EmailConverter(), emailSignal, emailSignal::set);
 *
 * var emailField = new EmailField("E-mail");
 * emailField.bindValue(emailPresentation, emailPresentation::setPresentation);
 * SignalUtil.bindValidation(emailField, emailDemux);
 * }</pre>
 *
 * @param <P> the presentation type exposed by this signal
 * @see SignalUtil#modelBacked(com.vaadin.flow.data.converter.Converter, Signal, com.vaadin.flow.function.SerializableConsumer)
 * @see PresentationBackedConvertedSignal
 */
@NullMarked
public interface ModelBackedConvertedSignal<P extends @Nullable Object> extends Signal<P> {

    /**
     * Writes a new presentation value into the backing model signal by
     * converting it to a model value. If the conversion fails, the backing
     * signal receives a {@link com.vaadin.flow.data.binder.Result#error(String)
     * Result.error(...)} and this presentation value is cached so that
     * subsequent calls to {@link #get()} return it instead of throwing while
     * the model is in an error state.
     *
     * @param presentation the new presentation value to write
     */
    void setPresentation(P presentation);
}
