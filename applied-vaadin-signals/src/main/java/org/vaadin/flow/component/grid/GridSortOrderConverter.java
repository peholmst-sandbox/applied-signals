package org.vaadin.flow.component.grid;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.provider.SortDirection;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * Converter between a {@link Grid}'s sort order and a list of string tokens suitable for use
 * as a repeated query parameter value. Each {@link GridSortOrder} is encoded as
 * {@code columnKey:direction}, where {@code direction} is {@code asc} or {@code desc} (the
 * short names of {@link SortDirection}). The column key must be set on every sortable column
 * via {@link com.vaadin.flow.component.grid.Grid.Column#setKey(String) Column.setKey(String)};
 * columns without a key cannot be converted to presentation.
 *
 * <p>Reading produces an error {@link Result} if a token references an unknown column key or
 * uses an unrecognized direction. An empty list round-trips to an empty list.
 *
 * <p>Typical use is to bind a grid's sort state to a {@link com.vaadin.flow.router.QueryParameters
 * QueryParameters}-backed signal so the current sort is reflected in the URL and restored on
 * navigation:
 * <pre>{@code
 * var grid = new Grid<Product>();
 * grid.addColumn(Product::name).setHeader("Name").setKey("name").setSortable(true);
 * // ...
 *
 * var queryParams = new ValueSignal<>(QueryParameters.empty());
 * SignalUtil.bindQueryParameters(this, queryParams, queryParams::set);
 * var sortParam = new QueryParamSignal("sort", queryParams, queryParams::set);
 * var sortOrder = SignalUtil.presentationBacked(new GridSortOrderConverter<>(grid), sortParam, sortParam::set);
 * var sortOrderDemux = SignalUtil.demuxResult(sortOrder);
 * GridUtil.bindSort(grid, sortOrderDemux.valueOrElse(List.of()), sortOrder::setModel);
 * }</pre>
 *
 * @param <T> the item type of the grid
 */
@NullMarked
public class GridSortOrderConverter<T> implements Converter<List<String>, List<GridSortOrder<T>>> {

    private static final String SEPARATOR = ":";
    private final Grid<T> grid;

    /**
     * Creates a new converter bound to the given grid. The grid is used to resolve column keys
     * to {@link com.vaadin.flow.component.grid.Grid.Column Column} instances when converting to
     * model, so columns must have their keys set before conversion is attempted.
     *
     * @param grid the grid whose columns this converter resolves
     */
    public GridSortOrderConverter(Grid<T> grid) {
        this.grid = grid;
    }

    @Override
    public Result<List<GridSortOrder<T>>> convertToModel(List<String> value, ValueContext context) {
        if (value.isEmpty()) {
            return Result.ok(List.of());
        }
        try {
            var result = value.stream().map(this::toGridSortOrder).toList();
            return Result.ok(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    private GridSortOrder<T> toGridSortOrder(String s) {
        var separator = s.lastIndexOf(SEPARATOR);
        var key = s.substring(0, separator);
        var direction = s.substring(separator + 1);
        var column = grid.getColumnByKey(key);
        if (column == null) {
            throw new IllegalArgumentException("Unknown column: " + key);
        }
        if (SortDirection.ASCENDING.getShortName().equals(direction)) {
            return new GridSortOrder<>(column, SortDirection.ASCENDING);
        } else if (SortDirection.DESCENDING.getShortName().equals(direction)) {
            return new GridSortOrder<>(column, SortDirection.DESCENDING);
        } else {
            throw new IllegalArgumentException("Unknown sort direction: " + direction);
        }
    }

    @Override
    public List<String> convertToPresentation(List<GridSortOrder<T>> value, ValueContext context) {
        return value.stream()
                .map(this::toString)
                .toList();
    }

    private String toString(GridSortOrder<T> order) {
        var key = order.getSorted().getKey();
        if (key == null) {
            throw new IllegalStateException("Column " + order.getSorted() + " does not have a key. Use the setKey() method to specify one.");
        }
        return order.getSorted().getKey() + SEPARATOR + order.getDirection().getShortName();
    }
}
