package org.vaadin.flow.component.grid;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public class GridUtil {

    public static <T, C extends Collection<T>> void bindItems(Grid<T> grid, Signal<@NonNull C> signal) {
        Signal.effect(grid, () -> grid.setItems(signal.get()));
    }
}
