package com.example.application.loading;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;
import org.vaadin.flow.signals.Loadable;
import org.vaadin.flow.signals.LoadableSignal;

import java.util.List;

import static org.vaadin.flow.component.grid.GridUtil.bindItems;
import static org.vaadin.flow.signals.SignalUtil.nullSafe;

@Route("loading")
class LoadingView extends VerticalLayout {

    LoadingView(BackendService backendService) {
        var products = new LoadableSignal<List<Product>>(Loadable.notLoaded());

        var loadingIndicator = new ProgressBar();
        loadingIndicator.setIndeterminate(true);

        var errorMessage = createErrorMessage("Error loading products. Try again later.");

        var grid = new Grid<Product>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(Product::name).setHeader("Name");
        grid.addColumn(Product::price).setHeader("Price");
        grid.addColumn(Product::category).setHeader("Category");

        bindItems(grid, nullSafe(products.finished(), List.of()));
        loadingIndicator.bindVisible(products.loading());
        errorMessage.bindVisible(products.failed());

        grid.setSizeFull();
        setSizeFull();
        add(loadingIndicator, errorMessage, grid);

        addAttachListener(_ -> Thread.startVirtualThread(() -> Loadable.load(backendService::getProducts, products::set)));
    }

    private Component createErrorMessage(String message) {
        var badge = new Badge(message, VaadinIcon.EXCLAMATION.create());
        badge.addThemeVariants(BadgeVariant.ERROR, BadgeVariant.FILLED);
        return badge;
    }
}
