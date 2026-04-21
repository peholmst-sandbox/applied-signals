package org.vaadin.flow.signals;

import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
public class QueryParamSignal implements Signal<List<String>> {

    private final String parameterName;
    private final Signal<QueryParameters> queryParametersSignal;
    private final SerializableConsumer<QueryParameters> writeCallback;

    public QueryParamSignal(String parameterName, Signal<QueryParameters> queryParametersSignal, SerializableConsumer<QueryParameters> writeCallback) {
        this.parameterName = parameterName;
        this.queryParametersSignal = queryParametersSignal;
        this.writeCallback = writeCallback;
    }

    @Override
    public List<String> get() {
        return queryParametersSignal.get().getParameters(parameterName);
    }

    public void set(Collection<String> parameterValues) {
        var existingParams = queryParametersSignal.peek();
        if (parameterValues.isEmpty()) {
            writeCallback.accept(existingParams.excluding(parameterName));
        } else {
            writeCallback.accept(existingParams.mergingAll(Map.of(parameterName, List.copyOf(parameterValues))));
        }
    }

    public Signal<@Nullable String> asSingleValue() {
        return () -> {
            var list = QueryParamSignal.this.get();
            return list.isEmpty() ? null : list.getFirst();
        };
    }

    public Signal<Set<String>> asSet() {
        return () -> {
            var list = QueryParamSignal.this.get();
            return Set.copyOf(list);
        };
    }

    public void setSingleValue(@Nullable String singleParameterValue) {
        if (singleParameterValue == null || singleParameterValue.isBlank()) {
            set(List.of());
        } else {
            set(List.of(singleParameterValue));
        }
    }
}
