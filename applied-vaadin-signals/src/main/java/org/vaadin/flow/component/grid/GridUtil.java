package org.vaadin.flow.component.grid;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Utility methods for binding {@link Signal} instances to {@link Grid} components.
 */
@NullMarked
public final class GridUtil {

    private GridUtil() {
    }

    /**
     * Binds the items of a {@link Grid} to a signal containing a collection.
     * <p>
     * Creates an effect scoped to the grid's attach/detach lifecycle that
     * updates the grid's items whenever the signal value changes.
     *
     * @param grid   the grid to bind
     * @param signal a signal producing the collection of items to display
     * @param <T>    the item type of the grid
     * @param <C>    the collection type
     */
    public static <T, C extends Collection<T>> Registration bindItems(Grid<T> grid, Signal<C> signal) {
        return Signal.effect(grid, () -> grid.setItems(signal.get()));
    }

    public static <T> Registration bindSingleSelection(Grid<T> grid, Signal<@Nullable T> valueSignal, SerializableConsumer<@Nullable T> writeCallback) {
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        return Registration.combine(Signal.effect(grid, () -> {
                    var selection = valueSignal.get();
                    if (selection == null) {
                        grid.deselectAll();
                    } else {
                        grid.select(selection);
                    }
                }),
                grid.addSelectionListener(event -> {
                    writeCallback.accept(event.getFirstSelectedItem().orElse(null));
                }));
    }

    public static <T> Registration bindSort(Grid<T> grid, Signal<List<GridSortOrder<T>>> signal, SerializableConsumer<List<GridSortOrder<T>>> writeCallback) {
        return Registration.combine(Signal.effect(grid, () -> {
                    var sortOrders = signal.get();
                    grid.sort(sortOrders);
                }),
                grid.addSortListener(event -> {
                    writeCallback.accept(event.getSortOrder());
                }));
    }
}
