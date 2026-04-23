package com.example.application.selection;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.signals.local.ValueSignal;
import org.vaadin.flow.component.grid.GridUtil;
import org.vaadin.flow.signals.RouteParamSignal;
import org.vaadin.flow.signals.SignalUtil;

import java.util.UUID;

@Route("selection/:id?")
class SelectionView extends VerticalLayout {

    SelectionView(SelectionBackendService service) {
        var routeParams = new ValueSignal<>(RouteParameters.empty());
        SignalUtil.bindRouteParameters(this, routeParams, routeParams::set);

        var selection = new RouteParamSignal("id", routeParams, routeParams::set);
        var selectionModel = SignalUtil.presentationBacked(GridUtil.<String, Product>idToSelectionConverter(p -> p.id().toString(), id -> service.getProduct(UUID.fromString(id))), selection, selection::set);
        var selectionDemux = SignalUtil.demuxResult(selectionModel);

        var grid = new Grid<Product>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(Product::name).setHeader("Name");
        grid.addColumn(Product::price).setHeader("Price");
        grid.addColumn(Product::category).setHeader("Category");
        grid.setItems(service.getAllProducts());
        GridUtil.bindSingleSelection(grid, selectionDemux.valueOrElse(null), selectionModel::setModel);

        setSizeFull();
        grid.setSizeFull();
        add(grid);
    }
}
