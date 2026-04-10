package org.vaadin.flow.signals;

import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

/**
 * A signal-based bridge between a model value and its presentation representation,
 * with built-in conversion and validation support.
 * <p>
 * This interface exposes reactive signals for the model value, the presentation value,
 * and the current validation state (invalid flag and error message). Implementations
 * use a {@link com.vaadin.flow.data.converter.Converter} to translate between the two
 * types and to detect conversion errors.
 *
 * @param <M> the model (domain) type
 * @param <P> the presentation (UI) type
 */
@NullMarked
public interface ConvertedSignal<M extends @Nullable Object, P extends @Nullable Object> extends Serializable {

    /**
     * Returns a signal that indicates whether the current presentation value
     * failed to convert to the model type.
     *
     * @return a boolean signal that is {@code true} when the last conversion
     *         produced an error, {@code false} otherwise
     */
    Signal<Boolean> invalid();

    /**
     * Returns a signal containing the error message from the last failed
     * conversion, or {@code null} when the conversion succeeded.
     *
     * @return a signal with the current error message, or {@code null} if valid
     */
    Signal<@Nullable String> errorMessage();

    /**
     * Returns a signal containing the current model (domain) value.
     *
     * @return the model signal
     */
    Signal<M> model();

    /**
     * Returns a signal containing the current presentation (UI) value.
     * <p>
     * When the conversion is in an invalid state, this signal returns the raw
     * presentation value that caused the error. When valid, it returns the
     * value derived by converting the model value to the presentation type.
     *
     * @return the presentation signal
     */
    Signal<P> presentation();
}
