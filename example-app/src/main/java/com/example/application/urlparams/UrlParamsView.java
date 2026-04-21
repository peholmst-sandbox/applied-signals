package com.example.application.urlparams;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.signals.local.ValueSignal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.vaadin.flow.signals.QueryParamSignal;
import org.vaadin.flow.signals.RouteParamSignal;
import org.vaadin.flow.signals.SignalUtil;

import java.util.Objects;
import java.util.UUID;

import static org.vaadin.flow.signals.SignalUtil.nullSafe;

@Route("urlparams/:id/:action?")
@NullMarked
class UrlParamsView extends VerticalLayout {

    UrlParamsView() {
        var routeParams = new ValueSignal<>(RouteParameters.empty());
        var queryParams = new ValueSignal<>(QueryParameters.empty());
        SignalUtil.bindNavigationParameters(this, routeParams, routeParams::set, queryParams, queryParams::set);

        var id = new RouteParamSignal("id", routeParams, routeParams::set);
        var action = new RouteParamSignal("action", routeParams, routeParams::set);
        var sort = new QueryParamSignal("sort", queryParams, queryParams::set);

        var idModel = SignalUtil.presentationBacked(new UUIDConverter(), id, id::set);
        var idDemux = SignalUtil.demuxResult(idModel);
        var actionModel = SignalUtil.presentationBacked(new ActionConverter(), action, action::set);
        var actionDemux = SignalUtil.demuxResult(actionModel);

        // This field edits the route param directly
        var idField = new TextField("id");
        idField.setValueChangeMode(ValueChangeMode.LAZY);
        idField.bindValue(nullSafe(id, ""), id::set);
        SignalUtil.bindValidation(idField, idDemux);

        var idModelSpan = new Span();
        idModelSpan.bindText(idModel.map(Objects::toString));

        // This button edits the route param through its model signal (goes through the converter)
        var generateNewIdBtn = new Button("Generate ID", _ -> idModel.setModel(UUID.randomUUID()));

        var actionField = new TextField("action");
        actionField.setValueChangeMode(ValueChangeMode.LAZY);
        actionField.bindValue(nullSafe(action, ""), action::set);
        SignalUtil.bindValidation(actionField, actionDemux);

        var actionModelSpan = new Span();
        actionModelSpan.bindText(actionModel.map(Objects::toString));

        var nextActionBtn = new Button("Next Action", _ -> actionModel.peek().handle(ok -> actionModel.setModel(ok == null ? Action.EDIT : ok.next()), _ -> actionModel.setModel(Action.EDIT)));

        var sortSelect = new Select<@Nullable String>("Sort (single)");
        sortSelect.setItems("hello", "world", "foo", "bar");
        sortSelect.setEmptySelectionAllowed(true);
        sortSelect.setEmptySelectionCaption("(none)");
        sortSelect.bindValue(sort.asSingleValue(), sort::setSingleValue);

        var sortMultiselect = new MultiSelectComboBox<String>("Sort (multiple)");
        sortMultiselect.setItems("hello", "world", "foo", "bar");
        sortMultiselect.setClearButtonVisible(true);
        sortMultiselect.bindValue(sort.asSet(), sort::set);

        add(idField, idModelSpan, generateNewIdBtn, actionField, actionModelSpan, nextActionBtn, sortSelect, sortMultiselect);
    }

    enum Action {
        EDIT, VIEW, PRINT;

        private Action next() {
            var nextIndex = (ordinal() + 1) % values().length;
            return values()[nextIndex];
        }
    }

    static class UUIDConverter implements Converter<@Nullable String, @Nullable UUID> {

        @Override
        public Result<@Nullable UUID> convertToModel(@Nullable String value, ValueContext context) {
            try {
                return value == null || value.isBlank() ? Result.ok(null) : Result.ok(UUID.fromString(value));
            } catch (IllegalArgumentException e) {
                return Result.error(e.getMessage());
            }
        }

        @Override
        public String convertToPresentation(@Nullable UUID value, ValueContext context) {
            return value == null ? "" : value.toString();
        }
    }

    static class ActionConverter implements Converter<@Nullable String, @Nullable Action> {

        @Override
        public Result<@Nullable Action> convertToModel(@Nullable String value, ValueContext context) {
            try {
                return (value == null || value.isBlank()) ? Result.ok(null) : Result.ok(Action.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Result.error(e.getMessage());
            }
        }

        @Override
        public String convertToPresentation(@Nullable Action value, ValueContext context) {
            return value == null ? "" : value.toString().toLowerCase();
        }
    }
}
