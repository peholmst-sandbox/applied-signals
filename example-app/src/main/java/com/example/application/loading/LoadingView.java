package com.example.application.loading;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ValueSignal;
import org.jspecify.annotations.Nullable;
import org.vaadin.flow.data.Loader;

import java.util.List;

import static org.vaadin.flow.component.grid.GridUtil.bindItems;
import static org.vaadin.flow.signals.SignalUtil.nullSafe;

@Route("loading")
class LoadingView extends VerticalLayout {

    LoadingView(BackendService backendService) {
        var loading = new ValueSignal<>(false);
        var failed = new ValueSignal<>(false);
        var products = new ValueSignal<@Nullable List<Product>>(null);

        var loader = new Loader.Builder<List<Product>>()
                .bindLoading(loading::set)
                .bindReady(products::set)
                .bindFailed(failed::set)
                .build();
        SerializableRunnable refresh = () -> Thread.startVirtualThread(() -> loader.load(backendService::getProducts));

        var loadingIndicator = new ProgressBar();
        loadingIndicator.setIndeterminate(true);

        var errorMessage = createErrorMessage("Error loading products. Try again later.");

        var refreshBtn = new Button("Refresh", _ -> refresh.run());

        var grid = new Grid<Product>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(Product::name).setHeader("Name");
        grid.addColumn(Product::price).setHeader("Price");
        grid.addColumn(Product::category).setHeader("Category");
        grid.bindEnabled(() -> !loading.get() && !failed.get());
        bindItems(grid, nullSafe(products, List.of()));
        loadingIndicator.bindVisible(loading);
        errorMessage.bindVisible(failed);

        grid.setSizeFull();
        setSizeFull();
        add(refreshBtn, loadingIndicator, errorMessage, grid);

        addAttachListener(_ -> refresh.run());
    }

    private Component createErrorMessage(String message) {
        var badge = new Badge(message, VaadinIcon.EXCLAMATION.create());
        badge.addThemeVariants(BadgeVariant.ERROR, BadgeVariant.FILLED);
        return badge;
    }
}
