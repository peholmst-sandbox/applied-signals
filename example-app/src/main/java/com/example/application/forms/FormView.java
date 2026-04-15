package com.example.application.forms;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import org.jspecify.annotations.Nullable;
import org.vaadin.flow.component.grid.GridUtil;
import org.vaadin.flow.data.Loader;
import org.vaadin.flow.signals.SignalUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Objects;

@Route("form")
class FormView extends MasterDetailLayout {

    private final DateTimeFormatter dateFormat =DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(getLocale());
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(getLocale());


    FormView(OrderService orderService) {
        var orders = new ListSignal<OrderDTO>();
        var loader = new Loader.Builder<List<OrderDTO>>()
                .bindReady(result -> SignalUtil.matchItems(orders, Objects.requireNonNullElse(result, List.of()), OrderDTO::id))
                .build();
        SerializableRunnable refresh = () -> Thread.startVirtualThread(() -> loader.load(orderService::findAllOrders));

        var refreshBtn = new Button("Refresh", _ -> refresh.run());

        var grid = new Grid<ValueSignal<OrderDTO>>(); // TODO Should work with only Signal instead of ValueSignal
        grid.addClassName("order-grid");
        grid.addComponentColumn(this::createOrderPanel);
        grid.addThemeVariants(GridVariant.NO_ROW_BORDERS);
        GridUtil.bindItems(grid, orders);

        var masterLayout = new VerticalLayout(refreshBtn, grid);
        masterLayout.expand(grid);
        masterLayout.setSizeFull();

        setMaster(masterLayout);
        setMasterSize(400, Unit.PIXELS);
        setExpandingArea(ExpandingArea.DETAIL);

        setDetailPlaceholder(new Div("Select an order"));

        addAttachListener(_ -> refresh.run());
    }

    private Component createOrderPanel(Signal<OrderDTO> orderSignal) {
        var panel = new Div();
        panel.addClassName("order-panel");

        var header = new Div();
        {
            header.addClassName("order-header");

            var eyebrow = new Paragraph("Order");
            eyebrow.addClassName("order-eyebrow");

            var customer = new H1();
            customer.addClassName("order-customer");
            customer.bindText(() -> orderSignal.get().customerName());

            var status = new Badge();
            status.bindText(() -> orderSignal.get().status().name());
            status.bindClassNames(() -> List.of("order-status", orderSignal.get().status().name().toLowerCase()));

            var priority = new Badge();
            priority.bindClassNames(() -> List.of("order-priority", orderSignal.get().priority().name().toLowerCase()));
            priority.bindText(() -> orderSignal.get().priority().name());

            header.add(new Div(eyebrow, customer));
            header.add(new Div(status, priority));
        }
        var details = new Div();
        {
            details.addClassName("order-details");
            details.add(createDetailRow("Order date", () -> formatDate(orderSignal.get().orderDate())));
            details.add(createDetailRow("Delivery Date", () -> formatDate(orderSignal.get().deliveryDate())));
            details.add(createDetailRow("Order ID", () -> orderSignal.get().id().toString().substring(0, 8) + "…"));
        }
        var footer = new Div();
        {
            footer.addClassName("order-footer");

            var label = new Span("Total");
            label.addClassName("order-footer-label");

            var total = new Span();
            total.addClassName("order-footer-total");
            total.bindText(() -> formatMoney(orderSignal.get().total()));

            footer.add(label, total);
        }

        panel.add(header, details, footer);
        return panel;
    }

    private String formatDate(@Nullable LocalDate date) {
        if (date == null) {
            return "";
        } else {
            return date.format(dateFormat);
        }
    }

    private String formatMoney(BigDecimal money) {
        return currencyFormat.format(money);
    }

    private Component createDetailRow(String label, Signal<String> valueSignal) {
        var div = new Div();
        div.addClassName("detail-row");

        var detailLabel = new Span(label);
        detailLabel.addClassName("detail-label");

        var detailValue = new Span();
        detailValue.addClassName("detail-value");
        detailValue.bindText(valueSignal);

        div.add(detailLabel, detailValue);
        return div;
    }
}
