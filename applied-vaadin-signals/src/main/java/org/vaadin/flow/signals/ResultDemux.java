package org.vaadin.flow.signals;

import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

/**
 * Splits a {@code Signal<Result<V>>} into separate narrower signals for the
 * three pieces of information a {@link com.vaadin.flow.data.binder.Result
 * Result} carries: whether it is an error, the error message, and the
 * successful value.
 *
 * <p>Created via {@link SignalUtil#demuxResult(Signal)}. Each returned signal
 * is derived from the same source, so they stay consistent with each other and
 * update whenever the source changes.
 *
 * <p>A common use is to drive field validation: the {@link #invalid()} and
 * {@link #errorMessage()} signals can be wired directly to a
 * {@link com.vaadin.flow.component.HasValidation} field via
 * {@link SignalUtil#bindValidation(com.vaadin.flow.component.Component, ResultDemux)},
 * while {@link #valueOrElse(Object)} exposes a plain-valued view for read-only
 * display. Example:
 * <pre>{@code
 * var emailDemux = SignalUtil.demuxResult(emailSignal);
 *
 * SignalUtil.bindValidation(emailField, emailDemux);
 *
 * var modelValueSpan = new Span();
 * modelValueSpan.bindText(emailDemux.valueOrElse(null).map(Objects::toString));
 * }</pre>
 *
 * @param <V> the successful value type of the source {@code Result}
 * @see SignalUtil#demuxResult(Signal)
 * @see SignalUtil#bindValidation(com.vaadin.flow.component.Component, ResultDemux)
 */
@NullMarked
public interface ResultDemux<V extends @Nullable Object> extends Serializable {

    /**
     * A signal that is {@code true} whenever the source {@code Result} is an
     * error and {@code false} when it holds a value.
     *
     * @return a signal tracking the error state of the source
     */
    Signal<Boolean> invalid();

    /**
     * A signal that exposes the error message of the source {@code Result}, or
     * {@code null} when the source currently holds a value.
     *
     * @return a signal tracking the error message of the source
     */
    Signal<@Nullable String> errorMessage();

    /**
     * A signal that exposes the successful value of the source {@code Result},
     * falling back to {@code other} whenever the source is an error.
     *
     * @param other the fallback value to expose while the source is an error
     * @return a signal tracking the value of the source, with a fallback
     */
    Signal<V> valueOrElse(V other);
}
