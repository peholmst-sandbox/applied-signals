package org.vaadin.flow.signals;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ListSignal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
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
     * URL corresponding to the new route parameters. When the user navigates (e.g. via the address
     * bar or browser history), the updated route parameters are passed to {@code writeCallback}.
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
                    view.getUI().ifPresent(ui -> ui.navigate(view.getClass(), routeParameters));
                }),
                view.addAttachListener(attachEvent -> {
                    var registration = attachEvent.getUI().addAfterNavigationListener(afterNavigationEvent -> writeCallback.accept(afterNavigationEvent.getRouteParameters()));
                    view.addDetachListener(detachEvent -> {
                        detachEvent.unregisterListener();
                        registration.remove();
                    });
                }));
    }

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

    public static <C extends Component & HasValidation> Registration bindValidation(C field, ResultDemux<?> demux) {
        field.setManualValidation(true);
        return Signal.effect(field, () -> {
            field.setInvalid(demux.invalid().get());
            field.setErrorMessage(demux.errorMessage().get());
        });
    }

    public static <M extends @Nullable Object, P extends @Nullable Object> PresentationBackedConvertedSignal<M> presentationBacked(Converter<P, M> converter, Signal<@Nullable P> signal, SerializableConsumer<@Nullable P> writeCallback) {
        return presentationBacked(converter, signal, writeCallback, ValueContext::new);
    }

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

    public static <M extends @Nullable Object, P extends @Nullable Object> ModelBackedConvertedSignal<P> modelBacked(Converter<P, M> converter, Signal<Result<@Nullable M>> model, SerializableConsumer<Result<@Nullable M>> writeCallback) {
        return modelBacked(converter, model, writeCallback, ValueContext::new);
    }

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
