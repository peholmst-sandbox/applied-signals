package com.example.application.sorting;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import org.vaadin.flow.component.grid.GridSortOrderConverter;
import org.vaadin.flow.component.grid.GridUtil;
import org.vaadin.flow.signals.QueryParamSignal;
import org.vaadin.flow.signals.SignalUtil;

import java.util.List;

@Route("sorting/in-memory")
class InMemorySortingView extends VerticalLayout {

    InMemorySortingView(SortingBackendService sortingBackendService) {
        var grid = new Grid<Product>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(Product::name).setHeader("Name").setKey("name").setSortable(true);
        grid.addColumn(Product::price).setHeader("Price").setKey("price").setSortable(true);
        grid.addColumn(Product::category).setHeader("Category").setKey("category").setSortable(true);
        grid.setItems(sortingBackendService.getAllProducts());

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
