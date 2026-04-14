package org.vaadin.flow.component.grid;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

/**
 * Utility methods for binding {@link Signal} instances to {@link Grid} components.
 */
public class GridUtil {

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
    public static <T, C extends Collection<T>> void bindItems(Grid<T> grid, Signal<@NonNull C> signal) {
        Signal.effect(grid, () -> grid.setItems(signal.get()));
    }
}
