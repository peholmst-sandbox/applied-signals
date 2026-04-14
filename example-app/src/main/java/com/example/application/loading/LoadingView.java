package com.example.application.loading;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import org.vaadin.flow.signals.Loadable;
import org.vaadin.flow.signals.LoadableSignal;

import java.util.Collections;
import java.util.List;

@Route("loading")
class LoadingView extends VerticalLayout {

    LoadingView(BackendService backendService) {
        var products = new LoadableSignal<List<Product>>(Loadable.loading());

        var grid = new Grid<Product>();
        grid.addColumn(Product::name).setHeader("Name");
        grid.addColumn(Product::price).setHeader("Price");
        grid.addColumn(Product::category).setHeader("Category");

        Signal.effect(this, () -> {
            switch (products.get()) {
                case Loadable.Loading<List<Product>> _ -> {
                    grid.setItems(Collections.emptyList());
                    grid.setEmptyStateText("Loading...");
                }
                case Loadable.Failed(Exception exception) -> {
                    grid.setItems(Collections.emptyList());
                    grid.setEmptyStateText(exception.getMessage());
                }
                case Loadable.Ready(List<Product> result) ->  {
                    grid.setItems(result);
                    grid.setEmptyStateText("No products found.");
                }
            }
        });

        grid.setSizeFull();
        setSizeFull();
        add(grid);

        addAttachListener(_ -> Thread.startVirtualThread(() -> Loadable.load(backendService::getProducts, products::set)));
    }
}
