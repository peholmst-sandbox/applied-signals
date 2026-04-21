package org.vaadin.flow.signals;

import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * A {@link Signal} that reads and writes the values of a single named query parameter from an
 * underlying {@link QueryParameters} signal. Reading the signal ({@link #get()}) returns the
 * list of values currently associated with the parameter, or an empty list if the parameter is
 * absent. Writing ({@link #set(Collection)}) creates a new {@code QueryParameters} instance with
 * the updated values and passes it to the write callback, preserving all other parameters.
 *
 * <p>This is typically used together with
 * {@link SignalUtil#bindNavigationParameters(com.vaadin.flow.component.Component, Signal,
 * SerializableConsumer, Signal, SerializableConsumer)} to bind individual query parameters to UI
 * components:
 * <pre>{@code
 * var routeParams = new ValueSignal<>(RouteParameters.empty());
 * var queryParams = new ValueSignal<>(QueryParameters.empty());
 * SignalUtil.bindNavigationParameters(this,
 *         routeParams, routeParams::set,
 *         queryParams, queryParams::set);
 *
 * var sort = new QueryParamSignal("sort", queryParams, queryParams::set);
 * multiSelect.bindValue(sort.asSet(), sort::set);
 * }</pre>
 *
 * <p>Because HTTP query parameters can carry multiple values under the same name, this signal
 * natively exposes a {@link List}. For common cases where a parameter is expected to hold at
 * most a single value or is treated as a set, {@link #asSingleValue()} and {@link #asSet()}
 * provide narrower views.
 *
 * @see SignalUtil#bindNavigationParameters(com.vaadin.flow.component.Component, Signal,
 *      SerializableConsumer, Signal, SerializableConsumer)
 */
@NullMarked
public class QueryParamSignal implements Signal<List<String>> {

    private final String parameterName;
    private final Signal<QueryParameters> queryParametersSignal;
    private final SerializableConsumer<QueryParameters> writeCallback;

    /**
     * Creates a new signal for the given query parameter.
     *
     * @param parameterName          the name of the query parameter (e.g. {@code "sort"} for a
     *                               URL such as {@code ?sort=name})
     * @param queryParametersSignal  the signal holding the full set of query parameters,
     *                               typically bound to the URL via
     *                               {@link SignalUtil#bindNavigationParameters}
     * @param writeCallback          the callback invoked with updated query parameters when
     *                               {@link #set(Collection)} is called
     */
    public QueryParamSignal(String parameterName, Signal<QueryParameters> queryParametersSignal, SerializableConsumer<QueryParameters> writeCallback) {
        this.parameterName = parameterName;
        this.queryParametersSignal = queryParametersSignal;
        this.writeCallback = writeCallback;
    }

    /**
     * Returns the current values of this query parameter, or an empty list if the parameter is
     * absent from the current query parameters.
     */
    @Override
    public List<String> get() {
        return queryParametersSignal.get().getParameters(parameterName);
    }

    /**
     * Sets the values of this query parameter. An empty collection removes the parameter from
     * the query parameters. All other existing parameters are preserved.
     *
     * @param parameterValues the new values, or an empty collection to remove the parameter
     */
    public void set(Collection<String> parameterValues) {
        var existingParams = queryParametersSignal.peek();
        if (parameterValues.isEmpty()) {
            writeCallback.accept(existingParams.excluding(parameterName));
        } else {
            writeCallback.accept(existingParams.mergingAll(Map.of(parameterName, List.copyOf(parameterValues))));
        }
    }

    /**
     * Returns a read-only view of this signal as a single nullable value. Reading the returned
     * signal yields the first value of this parameter, or {@code null} if the parameter is
     * absent. Any additional values are ignored.
     *
     * <p>This is useful for binding a query parameter to a component that exposes a single
     * value, such as a plain input field or a {@link com.vaadin.flow.component.select.Select}.
     * To write back a single value, pair this with {@link #setSingleValue(String)}.
     *
     * @return a signal exposing the first value of this parameter, or {@code null} if absent
     */
    public Signal<@Nullable String> asSingleValue() {
        return () -> {
            var list = QueryParamSignal.this.get();
            return list.isEmpty() ? null : list.getFirst();
        };
    }

    /**
     * Returns a read-only view of this signal as a {@link Set} of values. Reading the returned
     * signal yields the distinct values currently associated with this parameter, or an empty
     * set if the parameter is absent.
     *
     * <p>This is useful for binding a query parameter to a component that works with a set of
     * selected values, such as a
     * {@link com.vaadin.flow.component.combobox.MultiSelectComboBox}. To write back a set of
     * values, pass the collection directly to {@link #set(Collection)}.
     *
     * @return a signal exposing the distinct values of this parameter as a set
     */
    public Signal<Set<String>> asSet() {
        return () -> {
            var list = QueryParamSignal.this.get();
            return Set.copyOf(list);
        };
    }

    /**
     * Sets the value of this query parameter to a single value. A {@code null} or blank value
     * removes the parameter from the query parameters. Any previously associated values are
     * replaced. All other existing parameters are preserved.
     *
     * <p>This is the write counterpart to {@link #asSingleValue()} for components that expose a
     * single nullable value.
     *
     * @param singleParameterValue the new single value, or {@code null} to remove the parameter
     */
    public void setSingleValue(@Nullable String singleParameterValue) {
        if (singleParameterValue == null || singleParameterValue.isBlank()) {
            set(List.of());
        } else {
            set(List.of(singleParameterValue));
        }
    }
}
