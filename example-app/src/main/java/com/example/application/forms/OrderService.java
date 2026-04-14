package com.example.application.forms;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
class OrderService {

    private final ConcurrentMap<UUID, Order> orders = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;

    OrderService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public Optional<Order> findOrderById(UUID id) {
        return Optional.ofNullable(orders.get(id)).map(Order::new);
    }

    public void saveOrder(Order order) {
        var copy = new Order(order);
        orders.put(order.getId(), copy);
        eventPublisher.publishEvent(new OrderUpdatedEvent(toOrderDTO(copy)));
    }

    public List<OrderDTO> findAllOrders() {
        return orders.values().stream().map(this::toOrderDTO)
                .sorted(Comparator.comparing(OrderDTO::orderDate))
                .toList();
    }

    private OrderDTO toOrderDTO(Order order) {
        return new OrderDTO(order.getId(), order.getCustomerFirstName() + " " + order.getCustomerLastName(), order.getOrderDate(), order.getDeliveryDate(), order.getStatus(), order.getPriority(), order.getTotal());
    }
}
