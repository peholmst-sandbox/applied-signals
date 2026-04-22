package com.example.application.sorting;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ValueSignal;
import org.vaadin.flow.component.grid.GridSortOrderConverter;
import org.vaadin.flow.component.grid.GridUtil;
import org.vaadin.flow.signals.QueryParamSignal;
import org.vaadin.flow.signals.SignalUtil;

import java.util.List;

@Route("sorting/backend")
class BackendSortingView extends VerticalLayout {

    BackendSortingView(SortingBackendService sortingBackendService) {
        var grid = new Grid<Product>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        // You must set a key to bind sorting to a query parameter
        grid.addColumn(Product::name).setHeader("Name").setSortProperty("name").setKey("name");
        grid.addColumn(Product::price).setHeader("Price").setSortProperty("price").setKey("price");
        grid.addColumn(Product::category).setHeader("Category").setSortProperty("category").setKey("category");
        grid.setItems(DataProvider.fromCallbacks(sortingBackendService::fetch, sortingBackendService::count));

        // TODO Is it possible to simplify this with some utility methods or abstractions?
        var queryParams = new ValueSignal<>(QueryParameters.empty());
        SignalUtil.bindQueryParameters(this, queryParams, queryParams::set);
        var sortParam = new QueryParamSignal("sort", queryParams, queryParams::set);
        var sortOrder = SignalUtil.presentationBacked(new GridSortOrderConverter<>(grid), sortParam, sortParam::set);
        var sortOrderDemux = SignalUtil.demuxResult(sortOrder);
        GridUtil.bindSort(grid, sortOrderDemux.valueOrElse(List.of()), sortOrder::setModel);

        grid.setSizeFull();
        setSizeFull();
        add(grid);
    }
}
