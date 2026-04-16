package org.vaadin.flow.signals;

import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;

@NullMarked
public class RouteParamSignal implements Signal<@Nullable String> {

    private final String parameterName;
    private final Signal<RouteParameters> routeParametersSignal;
    private final SerializableConsumer<RouteParameters> writeCallback;

    public RouteParamSignal(String parameterName, Signal<RouteParameters> routeParametersSignal, SerializableConsumer<RouteParameters> writeCallback) {
        this.parameterName = parameterName;
        this.routeParametersSignal = routeParametersSignal;
        this.writeCallback = writeCallback;
    }

    @Override
    public @Nullable String get() {
        return routeParametersSignal.get().get(parameterName).orElse(null);
    }

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
