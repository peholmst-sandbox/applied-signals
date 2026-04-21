package org.vaadin.flow.component.grid;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class GridSortOrderConverter<T> implements Converter<List<String>, List<GridSortOrder<T>>> {

    private static final String SEPARATOR = ":";
    private final Grid<T> grid;

    public GridSortOrderConverter(Grid<T> grid) {
        this.grid = grid;
    }

    @Override
    public Result<List<GridSortOrder<T>>> convertToModel(List<String> value, ValueContext context) {
        if (value.isEmpty()) {
            return Result.ok(List.of());
        }
        try {
            return Result.ok(value.stream().map(s -> toGridSortOrder(toQuerySortOrder(s))).toList());
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    private GridSortOrder<T> toGridSortOrder(QuerySortOrder sortOrder) {
        var column = grid.getColumnByKey(sortOrder.getSorted());
        if (column == null) {
            throw new IllegalArgumentException("Unknown column: " + sortOrder.getSorted());
        }
        return new GridSortOrder<>(column, sortOrder.getDirection());
    }

    @Override
    public List<String> convertToPresentation(List<GridSortOrder<T>> value, ValueContext context) {
        return value.stream()
                .flatMap(so -> so.getSorted().getSortOrder(so.getDirection()))
                .map(GridSortOrderConverter::toString)
                .toList();
    }

    private static String toString(QuerySortOrder order) {
        return order.getSorted() + SEPARATOR + order.getDirection().getShortName();
    }

    private static QuerySortOrder toQuerySortOrder(String s) {
        var separator = s.lastIndexOf(SEPARATOR);
        var key = s.substring(0, separator);
        var direction = s.substring(separator + 1);
        if (SortDirection.ASCENDING.getShortName().equals(direction)) {
            return new QuerySortOrder(key, SortDirection.ASCENDING);
        } else if (SortDirection.DESCENDING.getShortName().equals(direction)) {
            return new QuerySortOrder(key, SortDirection.DESCENDING);
        } else {
            throw new IllegalArgumentException("Unknown sort direction: " + direction);
        }
    }
}
