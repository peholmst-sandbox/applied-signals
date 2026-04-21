package org.vaadin.flow.signals;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ListSignal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility methods for working with {@link Signal} instances.
 */
@NullMarked
public final class SignalUtil {

    private SignalUtil() {
    }

    /**
     * Wraps a nullable signal so that it never returns {@code null},
     * substituting {@code defaultValue} whenever the underlying signal
     * produces {@code null}.
     *
     * @param signal       the source signal whose value may be {@code null}
     * @param defaultValue the non-null value to use when the source is {@code null}
     * @param <T>          the value type
     * @return a new signal that always returns a non-null value
     */
    public static <T> Signal<T> nullSafe(Signal<@Nullable T> signal, T defaultValue) {
        return () -> Objects.requireNonNullElse(signal.get(), defaultValue);
    }

    /**
     * Reconciles the contents of a {@link ListSignal} with a given list of items,
     * identifying items by the key returned from {@code identityProvider}.
     *
     * <p>After this method returns, the signal contains exactly the given
     * {@code items} in the given order. Items whose identity already exists in
     * the signal are kept (their backing child signals are preserved and moved
     * into place), new items are inserted, and items no longer present are
     * removed. This preserves per-item signal identity across refreshes, so
     * effects and UI bindings attached to individual child signals remain
     * stable.
     *
     * <p>Example usage:
     * <pre>{@code
     * var orders = new ListSignal<OrderDTO>();
     * SignalUtil.matchItems(orders, orderService.findAllOrders(), OrderDTO::id);
     * }</pre>
     *
     * @param signal           the list signal to update in place
     * @param items            the desired contents of the list, in order
     * @param identityProvider a function mapping each item to a stable identity
     *                         key used to match against existing entries
     * @param <T>              the item type
     */
    public static <T> void matchItems(ListSignal<T> signal, List<T> items, Function<T, Object> identityProvider) {
        var existingSignals = signal.peek().stream().collect(Collectors.toMap(s -> identityProvider.apply(s.peek()), s -> s));
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            var existingSignal = existingSignals.remove(identityProvider.apply(item));
            if (existingSignal == null) {
                signal.insertAt(i, item);
            } else {
                signal.moveTo(existingSignal, i);
            }
        }
        existingSignals.values().forEach(signal::remove);
    }

    /**
     * Creates a two-way binding between a {@link Signal} of {@link RouteParameters} and the
     * browser URL of the given view. When the signal value changes, the browser navigates to the
     * URL corresponding to the new route parameters, preserving the query parameters of the
     * currently active view location. When the user navigates (e.g. via the address bar or
     * browser history), the updated route parameters are passed to {@code writeCallback}.
     *
     * <p>This method only tracks and writes route parameters; query parameters are left
     * untouched. To bind both route and query parameters together, use
     * {@link #bindNavigationParameters(Component, Signal, SerializableConsumer, Signal,
     * SerializableConsumer)}.
     *
     * <p>The returned {@link Registration} removes both the navigation effect and the
     * after-navigation listener when called.
     *
     * <p>Example usage with a {@link com.vaadin.flow.signals.local.ValueSignal}:
     * <pre>{@code
     * @Route("urlparams/:id/:action?")
     * class UrlParamsView extends VerticalLayout {
     *     UrlParamsView() {
     *         var routeParams = new ValueSignal<RouteParameters>(RouteParameters.empty());
     *         SignalUtil.bindRouteParameters(this, routeParams, routeParams::set);
     *
     *         var id = new RouteParamSignal("id", routeParams, routeParams::set);
     *         // ...
     *     }
     * }
     * }</pre>
     *
     * @param view          the routed view component that owns the binding
     * @param signal        the signal holding the current route parameters
     * @param writeCallback the callback invoked with new route parameters after each navigation
     * @return a registration that removes the binding when called
     */
    public static Registration bindRouteParameters(Component view, Signal<RouteParameters> signal, SerializableConsumer<RouteParameters> writeCallback) {
        return Registration.combine(Signal.effect(view, () -> {
                    var routeParameters = signal.get();
                    // TODO This assumes the active view location is `view`, but doesn't check it. If that's not the case, the query parameters would be wrong.
                    view.getUI().ifPresent(ui -> ui.navigate(view.getClass(), routeParameters, ui.getActiveViewLocation().getQueryParameters()));
                }),
                view.addAttachListener(attachEvent -> {
                    var registration = attachEvent.getUI().addAfterNavigationListener(afterNavigationEvent -> writeCallback.accept(afterNavigationEvent.getRouteParameters()));
                    view.addDetachListener(detachEvent -> {
                        detachEvent.unregisterListener();
                        registration.remove();
                    });
                }));
    }

    /**
     * Creates a two-way binding between signals of {@link RouteParameters} and
     * {@link QueryParameters} and the browser URL of the given view. When either signal value
     * changes, the browser navigates to the URL corresponding to the combined route and query
     * parameters. When the user navigates (e.g. via the address bar or browser history), the
     * updated route parameters are passed to {@code routeWriteCallback} and the updated query
     * parameters to {@code queryWriteCallback}.
     *
     * <p>Use this method when the view depends on both route and query parameters. If only
     * route parameters are needed, use
     * {@link #bindRouteParameters(Component, Signal, SerializableConsumer)} instead.
     *
     * <p>The returned {@link Registration} removes both the navigation effect and the
     * after-navigation listener when called.
     *
     * <p>Example usage with two {@link com.vaadin.flow.signals.local.ValueSignal}s:
     * <pre>{@code
     * @Route("urlparams/:id/:action?")
     * class UrlParamsView extends VerticalLayout {
     *     UrlParamsView() {
     *         var routeParams = new ValueSignal<>(RouteParameters.empty());
     *         var queryParams = new ValueSignal<>(QueryParameters.empty());
     *         SignalUtil.bindNavigationParameters(this,
     *                 routeParams, routeParams::set,
     *                 queryParams, queryParams::set);
     *
     *         var id = new RouteParamSignal("id", routeParams, routeParams::set);
     *         var sort = new QueryParamSignal("sort", queryParams, queryParams::set);
     *         // ...
     *     }
     * }
     * }</pre>
     *
     * @param view               the routed view component that owns the binding
     * @param routeSignal        the signal holding the current route parameters
     * @param routeWriteCallback the callback invoked with new route parameters after each
     *                           navigation
     * @param querySignal        the signal holding the current query parameters
     * @param queryWriteCallback the callback invoked with new query parameters after each
     *                           navigation
     * @return a registration that removes the binding when called
     */
    public static Registration bindNavigationParameters(Component view,
                                                        Signal<RouteParameters> routeSignal, SerializableConsumer<RouteParameters> routeWriteCallback,
                                                        Signal<QueryParameters> querySignal, SerializableConsumer<QueryParameters> queryWriteCallback) {
        return Registration.combine(Signal.effect(view, () -> {
                    var routeParameters = routeSignal.get();
                    var queryParameters = querySignal.get();
                    view.getUI().ifPresent(ui -> ui.navigate(view.getClass(), routeParameters, queryParameters));
                }),
                view.addAttachListener(attachEvent -> {
                    var registration = attachEvent.getUI().addAfterNavigationListener(afterNavigationEvent -> {
                        routeWriteCallback.accept(afterNavigationEvent.getRouteParameters());
                        queryWriteCallback.accept(afterNavigationEvent.getLocation().getQueryParameters());
                    });
                    view.addDetachListener(detachEvent -> {
                        detachEvent.unregisterListener();
                        registration.remove();
                    });
                }));
    }

    /**
     * Splits a {@code Signal<Result<M>>} into separate narrower signals for the
     * three pieces of information a {@link Result} carries: whether it is an
     * error, the error message, and the successful value.
     *
     * <p>The returned {@link ResultDemux} derives all three signals from the
     * same source, so they stay consistent and update whenever the source
     * changes. The result is typically used together with
     * {@link #bindValidation(Component, ResultDemux)} to drive field validation
     * state.
     *
     * @param signal the source signal holding a {@link Result}
     * @param <M>    the successful value type of the source {@code Result}
     * @return a demultiplexed view of the source signal
     * @see ResultDemux
     */
    public static <M extends @Nullable Object> ResultDemux<M> demuxResult(Signal<Result<M>> signal) {
        var invalid = signal.map(Result::isError);
        var errorMessage = signal.map(r -> r.getMessage().orElse(null));
        return new ResultDemux<>() {
            @Override
            public Signal<Boolean> invalid() {
                return invalid;
            }

            @Override
            public Signal<@Nullable String> errorMessage() {
                return errorMessage;
            }

            @Override
            public Signal<M> valueOrElse(M other) {
                return signal.map(r -> {
                    try {
                        return r.getOrThrow(InvalidModelValueException::new);
                    } catch (InvalidModelValueException e) {
                        return other;
                    }
                });
            }
        };
    }

    /**
     * Binds the validation state of a field to a {@link ResultDemux}. The field
     * is switched to manual validation mode, and its invalid flag and error
     * message are kept in sync with the demux through a {@link Signal#effect
     * signal effect} that is scoped to the field's attach lifecycle.
     *
     * <p>Typical use is in combination with {@link #demuxResult(Signal)} to
     * surface conversion errors on an input field:
     * <pre>{@code
     * var emailDemux = SignalUtil.demuxResult(emailSignal);
     * SignalUtil.bindValidation(emailField, emailDemux);
     * }</pre>
     *
     * @param field the component to bind validation state to; must implement
     *              {@link HasValidation}
     * @param demux the demux supplying {@code invalid} and {@code errorMessage}
     *              signals
     * @param <C>   the field type
     * @return a registration that removes the binding effect when called
     */
    public static <C extends Component & HasValidation> Registration bindValidation(C field, ResultDemux<?> demux) {
        field.setManualValidation(true);
        return Signal.effect(field, () -> {
            field.setInvalid(demux.invalid().get());
            field.setErrorMessage(demux.errorMessage().get());
        });
    }

    /**
     * Creates a {@link PresentationBackedConvertedSignal} over a signal that
     * holds the raw presentation value. Equivalent to
     * {@link #presentationBacked(Converter, Signal, SerializableConsumer, SerializableSupplier)}
     * with a supplier that creates a fresh default {@link ValueContext} on each
     * conversion.
     *
     * @param converter     the converter between presentation and model types
     * @param signal        the source signal holding the presentation value
     * @param writeCallback the callback used to write a new presentation value
     *                      back into the source
     * @param <M>           the model type
     * @param <P>           the presentation type
     * @return a converted signal exposing the model view of the source
     * @see PresentationBackedConvertedSignal
     */
    public static <M extends @Nullable Object, P extends @Nullable Object> PresentationBackedConvertedSignal<M> presentationBacked(Converter<P, M> converter, Signal<@Nullable P> signal, SerializableConsumer<@Nullable P> writeCallback) {
        return presentationBacked(converter, signal, writeCallback, ValueContext::new);
    }

    /**
     * Creates a {@link PresentationBackedConvertedSignal} over a signal that
     * holds the raw presentation value.
     *
     * <p>Reading the returned signal runs
     * {@link Converter#convertToModel(Object, ValueContext)} on the current
     * presentation value, so conversion errors surface as
     * {@link Result#error(String)}. Writing via
     * {@link PresentationBackedConvertedSignal#setModel(Object) setModel}
     * converts the given model value with
     * {@link Converter#convertToPresentation(Object, ValueContext)} and passes
     * the result to {@code writeCallback}.
     *
     * <p>Typical use is to expose a strongly typed model view over a signal
     * whose natural representation is a string, for example a route parameter:
     * <pre>{@code
     * var id = new RouteParamSignal("id", routeParams, routeParams::set);
     * var idModel = SignalUtil.presentationBacked(new UUIDConverter(), id, id::set);
     * }</pre>
     *
     * @param converter       the converter between presentation and model types
     * @param signal          the source signal holding the presentation value
     * @param writeCallback   the callback used to write a new presentation value
     *                        back into the source
     * @param contextSupplier supplies the {@link ValueContext} passed to the
     *                        converter on each conversion
     * @param <M>             the model type
     * @param <P>             the presentation type
     * @return a converted signal exposing the model view of the source
     * @see PresentationBackedConvertedSignal
     */
    public static <M extends @Nullable Object, P extends @Nullable Object> PresentationBackedConvertedSignal<M> presentationBacked(Converter<P, M> converter, Signal<@Nullable P> signal, SerializableConsumer<@Nullable P> writeCallback, SerializableSupplier<ValueContext> contextSupplier) {
        return new PresentationBackedConvertedSignal<>() {

            @Override
            public Result<M> get() {
                return converter.convertToModel(signal.get(), contextSupplier.get());
            }

            @Override
            public void setModel(M model) {
                writeCallback.accept(converter.convertToPresentation(model, contextSupplier.get()));
            }
        };
    }

    /**
     * Creates a {@link ModelBackedConvertedSignal} over a signal that holds the
     * model value as a {@link Result}. Equivalent to
     * {@link #modelBacked(Converter, Signal, SerializableConsumer, SerializableSupplier)}
     * with a supplier that creates a fresh default {@link ValueContext} on each
     * conversion.
     *
     * @param converter     the converter between presentation and model types
     * @param model         the source signal holding the model value as a
     *                      {@link Result}
     * @param writeCallback the callback used to write a new {@link Result} back
     *                      into the source
     * @param <M>           the model type
     * @param <P>           the presentation type
     * @return a converted signal exposing the presentation view of the source
     * @see ModelBackedConvertedSignal
     */
    public static <M extends @Nullable Object, P extends @Nullable Object> ModelBackedConvertedSignal<P> modelBacked(Converter<P, M> converter, Signal<Result<@Nullable M>> model, SerializableConsumer<Result<@Nullable M>> writeCallback) {
        return modelBacked(converter, model, writeCallback, ValueContext::new);
    }

    /**
     * Creates a {@link ModelBackedConvertedSignal} over a signal that holds the
     * model value as a {@link Result}.
     *
     * <p>Reading the returned signal runs
     * {@link Converter#convertToPresentation(Object, ValueContext)} on the
     * successful model value. Writing via
     * {@link ModelBackedConvertedSignal#setPresentation(Object) setPresentation}
     * runs {@link Converter#convertToModel(Object, ValueContext)} and passes
     * the resulting {@link Result} to {@code writeCallback}. When the current
     * model is in an error state, reads fall back to the presentation value
     * most recently passed to {@code setPresentation}, so a bound field keeps
     * showing the user's invalid input instead of reverting.
     *
     * <p>Typical use is to drive a validated input field from a model-typed
     * signal:
     * <pre>{@code
     * var emailSignal = new ValueSignal<Result<@Nullable Email>>(Result.ok(null));
     * var emailPresentation = SignalUtil.modelBacked(new EmailConverter(), emailSignal, emailSignal::set);
     *
     * emailField.bindValue(emailPresentation, emailPresentation::setPresentation);
     * }</pre>
     *
     * @param converter       the converter between presentation and model types
     * @param model           the source signal holding the model value as a
     *                        {@link Result}
     * @param writeCallback   the callback used to write a new {@link Result}
     *                        back into the source
     * @param contextSupplier supplies the {@link ValueContext} passed to the
     *                        converter on each conversion
     * @param <M>             the model type
     * @param <P>             the presentation type
     * @return a converted signal exposing the presentation view of the source
     * @see ModelBackedConvertedSignal
     */
    public static <M extends @Nullable Object, P extends @Nullable Object> ModelBackedConvertedSignal<P> modelBacked(Converter<P, M> converter, Signal<Result<@Nullable M>> model, SerializableConsumer<Result<@Nullable M>> writeCallback, SerializableSupplier<ValueContext> contextSupplier) {
        return new ModelBackedConvertedSignal<>() {

            private boolean cachedPresentationSet = false;
            private P cachedPresentation;

            @Override
            public P get() {
                try {
                    return converter.convertToPresentation(model.get().getOrThrow(InvalidModelValueException::new), contextSupplier.get());
                } catch (InvalidModelValueException e) {
                    if (!cachedPresentationSet) {
                        throw new IllegalStateException("No presentation value to return");
                    }
                    return cachedPresentation;
                }
            }

            @Override
            public void setPresentation(P presentation) {
                this.cachedPresentationSet = true;
                this.cachedPresentation = presentation;
                writeCallback.accept(converter.convertToModel(presentation, contextSupplier.get()));
            }
        };
    }

    private static class InvalidModelValueException extends RuntimeException {

        public InvalidModelValueException(String message) {
            super(message);
        }
    }

}
