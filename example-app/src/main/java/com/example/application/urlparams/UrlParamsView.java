package com.example.application.urlparams;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.signals.local.ValueSignal;
import org.jspecify.annotations.NonNull;
import org.vaadin.flow.signals.RouteParamSignal;
import org.vaadin.flow.signals.SignalUtil;

import static org.vaadin.flow.signals.SignalUtil.nullSafe;

@Route("urlparams/:id/:action?")
class UrlParamsView extends VerticalLayout {

    UrlParamsView() {
        var routeParams = new ValueSignal<@NonNull RouteParameters>(RouteParameters.empty());
        SignalUtil.bindRouteParameters(this, routeParams, routeParams::set);

        var id = new RouteParamSignal("id", routeParams, routeParams::set);
        var action = new RouteParamSignal("action", routeParams, routeParams::set);

        var idField = new TextField("id");
        idField.setValueChangeMode(ValueChangeMode.LAZY);
        idField.bindValue(nullSafe(id, ""), id::set);

        var actionField = new TextField("action");
        actionField.setValueChangeMode(ValueChangeMode.LAZY);
        actionField.bindValue(nullSafe(action, ""), action::set);

        add(idField, actionField);
    }
}
