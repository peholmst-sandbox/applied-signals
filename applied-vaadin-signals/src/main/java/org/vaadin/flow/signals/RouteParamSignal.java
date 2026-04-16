package org.vaadin.flow.signals;

import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;

/**
 * A {@link Signal} that reads and writes a single named route parameter from an underlying
 * {@link RouteParameters} signal. Reading the signal ({@link #get()}) extracts the parameter
 * value from the current route parameters, returning {@code null} if the parameter is absent.
 * Writing ({@link #set(String)}) creates a new {@code RouteParameters} instance with the
 * updated value and passes it to the write callback, preserving all other parameters.
 *
 * <p>This is typically used together with
 * {@link SignalUtil#bindRouteParameters(com.vaadin.flow.component.Component, Signal,
 * SerializableConsumer)} to bind individual route parameters to UI components:
 * <pre>{@code
 * var routeParams = new ValueSignal<RouteParameters>(RouteParameters.empty());
 * SignalUtil.bindRouteParameters(this, routeParams, routeParams::set);
 *
 * var id = new RouteParamSignal("id", routeParams, routeParams::set);
 * textField.bindValue(SignalUtil.nullSafe(id, ""), id::set);
 * }</pre>
 *
 * @see SignalUtil#bindRouteParameters(com.vaadin.flow.component.Component, Signal,
 *      SerializableConsumer)
 */
@NullMarked
public class RouteParamSignal implements Signal<@Nullable String> {

    private final String parameterName;
    private final Signal<RouteParameters> routeParametersSignal;
    private final SerializableConsumer<RouteParameters> writeCallback;

    /**
     * Creates a new signal for the given route parameter.
     *
     * @param parameterName          the name of the route parameter (must match a parameter
     *                               placeholder in the route template, e.g. {@code "id"} for
     *                               a route defined as {@code @Route("view/:id")})
     * @param routeParametersSignal  the signal holding the full set of route parameters, typically
     *                               bound to the URL via {@link SignalUtil#bindRouteParameters}
     * @param writeCallback          the callback invoked with updated route parameters when
     *                               {@link #set(String)} is called
     */
    public RouteParamSignal(String parameterName, Signal<RouteParameters> routeParametersSignal, SerializableConsumer<RouteParameters> writeCallback) {
        this.parameterName = parameterName;
        this.routeParametersSignal = routeParametersSignal;
        this.writeCallback = writeCallback;
    }

    /**
     * Returns the current value of this route parameter, or {@code null} if the parameter is
     * absent from the current route parameters.
     */
    @Override
    public @Nullable String get() {
        return routeParametersSignal.get().get(parameterName).orElse(null);
    }

    /**
     * Sets the value of this route parameter. A {@code null} or blank value removes the parameter
     * from the route parameters. All other existing parameters are preserved.
     *
     * @param parameterValue the new value, or {@code null} to remove the parameter
     */
    public void set(@Nullable String parameterValue) {
        var existingParams = routeParametersSignal.peek();
        var newValues = new HashMap<String, String>();
        existingParams.getParameterNames().forEach(parameterName -> {
            existingParams.get(parameterName).ifPresent(value -> newValues.put(parameterName, value));
        });
        if (parameterValue == null || parameterValue.isBlank()) {
            newValues.remove(parameterName);
        } else {
            newValues.put(parameterName, parameterValue);
        }
        writeCallback.accept(new RouteParameters(newValues));
    }
}
