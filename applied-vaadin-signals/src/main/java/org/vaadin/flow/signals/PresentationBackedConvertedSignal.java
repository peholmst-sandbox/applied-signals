package org.vaadin.flow.signals;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A converted {@link Signal} that exposes a <em>model</em> view, as a
 * {@link Result}, over storage that holds a <em>presentation</em> value.
 *
 * <p>The underlying storage is presentation-authoritative: {@link #get()}
 * returns the result of running the converter's
 * {@link com.vaadin.flow.data.converter.Converter#convertToModel(Object, com.vaadin.flow.data.binder.ValueContext)
 * convertToModel} on the current presentation value, so conversion errors
 * surface as {@link Result#error(String)}. {@link #setModel(Object)} writes
 * back by converting the given model value with
 * {@link com.vaadin.flow.data.converter.Converter#convertToPresentation(Object, com.vaadin.flow.data.binder.ValueContext)
 * convertToPresentation} and storing the result in the backing presentation
 * signal.
 *
 * <p>Typical use is to expose a strongly typed model over a signal whose
 * natural representation is a string (or other raw presentation form), for
 * example route parameters. The {@code Result} signal can be demultiplexed
 * with {@link SignalUtil#demuxResult(Signal)} to drive field validation.
 * Example:
 * <pre>{@code
 * var id = new RouteParamSignal("id", routeParams, routeParams::set);
 * var idModel = SignalUtil.presentationBacked(new UUIDConverter(), id, id::set);
 * var idDemux = SignalUtil.demuxResult(idModel);
 *
 * var idField = new TextField("id");
 * idField.bindValue(nullSafe(id, ""), id::set);
 * SignalUtil.bindValidation(idField, idDemux);
 *
 * // Write back through the converter:
 * var generateNewIdBtn = new Button("Generate ID", _ -> idModel.setModel(UUID.randomUUID()));
 * }</pre>
 *
 * @param <M> the model type exposed by this signal (wrapped in {@link Result})
 * @see SignalUtil#presentationBacked(com.vaadin.flow.data.converter.Converter, Signal, com.vaadin.flow.function.SerializableConsumer)
 * @see ModelBackedConvertedSignal
 */
@NullMarked
public interface PresentationBackedConvertedSignal<M extends @Nullable Object> extends Signal<Result<M>> {

    /**
     * Writes a new model value into the backing presentation signal by
     * converting it to its presentation form. The converter is assumed to
     * always succeed when converting from model to presentation.
     *
     * @param model the new model value to write
     */
    void setModel(M model);
}
