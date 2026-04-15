package com.example.application.forms;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
class OrderService {

    private final ConcurrentMap<UUID, Order> orders = new ConcurrentHashMap<>();

    @PostConstruct
    void populateTestData() {
        createOrder("Alice", "Johnson", "alice.johnson@example.com", "+1-555-0101",
                "123 Elm Street", "Springfield", "62701", "US",
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 18),
                Order.OrderStatus.DELIVERED, Order.PaymentMethod.CREDIT_CARD, Order.Priority.NORMAL,
                null,
                item("Mechanical Keyboard", 1, "89.99"),
                item("USB-C Cable", 3, "12.50"),
                item("Monitor Stand", 1, "45.00"));

        createOrder("Bob", "Smith", "bob.smith@example.com", "+1-555-0102",
                "456 Oak Avenue", "Portland", "97201", "US",
                LocalDate.of(2026, 3, 25), null,
                Order.OrderStatus.SHIPPED, Order.PaymentMethod.PAYPAL, Order.Priority.HIGH,
                "Leave package at the back door",
                item("Running Shoes", 1, "129.95"),
                item("Sports Socks (6-pack)", 2, "14.99"));

        createOrder("Clara", "Müller", "clara.mueller@example.com", "+49-170-5550103",
                "Berliner Str. 78", "Munich", "80331", "DE",
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 10),
                Order.OrderStatus.CONFIRMED, Order.PaymentMethod.BANK_TRANSFER, Order.Priority.NORMAL,
                null,
                item("Espresso Machine", 1, "349.00"),
                item("Coffee Beans (1kg)", 4, "18.75"));

        createOrder("David", "Tanaka", "d.tanaka@example.com", "+81-90-5550104",
                "2-8-1 Nishi-Shinjuku", "Tokyo", "160-0023", "JP",
                LocalDate.of(2026, 4, 5), null,
                Order.OrderStatus.PLACED, Order.PaymentMethod.CREDIT_CARD, Order.Priority.EXPRESS,
                "Gift wrapping requested",
                item("Wireless Headphones", 1, "249.00"),
                item("Headphone Case", 1, "29.99"),
                item("Audio Cable (3.5mm)", 2, "8.50"));

        createOrder("Emma", "Wilson", "emma.w@example.com", "+44-7700-550105",
                "15 Baker Street", "London", "NW1 6XE", "GB",
                LocalDate.of(2026, 4, 8), LocalDate.of(2026, 4, 20),
                Order.OrderStatus.CONFIRMED, Order.PaymentMethod.DEBIT_CARD, Order.Priority.LOW,
                null,
                item("Yoga Mat", 1, "35.00"),
                item("Resistance Bands Set", 1, "22.50"),
                item("Water Bottle", 2, "15.00"));

        createOrder("Finn", "Larsson", "finn.larsson@example.com", "+46-70-5550106",
                "Kungsgatan 44", "Stockholm", "111 35", "SE",
                LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 22),
                Order.OrderStatus.DELIVERED, Order.PaymentMethod.INVOICE, Order.Priority.NORMAL,
                null,
                item("Standing Desk Frame", 1, "399.00"),
                item("Desk Top (140x70)", 1, "149.00"));

        createOrder("Grace", "Chen", "grace.chen@example.com", "+1-555-0107",
                "789 Maple Drive", "San Francisco", "94102", "US",
                LocalDate.of(2026, 4, 12), null,
                Order.OrderStatus.DRAFT, Order.PaymentMethod.CREDIT_CARD, Order.Priority.NORMAL,
                "Considering adding more items",
                item("Laptop Sleeve (15\")", 1, "39.99"));

        createOrder("Henrik", "Pedersen", "henrik.p@example.com", "+45-20-550108",
                "Vesterbrogade 12", "Copenhagen", "1620", "DK",
                LocalDate.of(2026, 4, 3), null,
                Order.OrderStatus.CANCELLED, Order.PaymentMethod.PAYPAL, Order.Priority.NORMAL,
                "Customer requested cancellation — found a better price",
                item("Bluetooth Speaker", 1, "79.95"),
                item("Portable Charger", 1, "44.99"));

        createOrder("Isabel", "Garcia", "isabel.garcia@example.com", "+34-600-550109",
                "Calle Mayor 22", "Madrid", "28013", "ES",
                LocalDate.of(2026, 3, 28), LocalDate.of(2026, 4, 5),
                Order.OrderStatus.SHIPPED, Order.PaymentMethod.CREDIT_CARD, Order.Priority.HIGH,
                null,
                item("Ceramic Plant Pot (Large)", 3, "28.00"),
                item("Indoor Plant Soil (10L)", 2, "9.99"),
                item("Plant Mister", 1, "14.50"));

        createOrder("James", "O'Brien", "james.obrien@example.com", "+353-87-5550110",
                "42 Grafton Street", "Dublin", "D02 Y098", "IE",
                LocalDate.of(2026, 4, 14), null,
                Order.OrderStatus.PLACED, Order.PaymentMethod.BANK_TRANSFER, Order.Priority.NORMAL,
                null,
                item("Wool Throw Blanket", 1, "65.00"),
                item("Scented Candle Set", 1, "32.00"),
                item("Cushion Cover (45x45)", 4, "18.50"));
    }

    private void createOrder(String firstName, String lastName, String email, String phone,
                             String street, String city, String zip, String country,
                             LocalDate orderDate, LocalDate deliveryDate,
                             Order.OrderStatus status, Order.PaymentMethod payment, Order.Priority priority,
                             String notes,
                             OrderItemData... items) {
        var order = new Order(UUID.randomUUID());
        order.setCustomerFirstName(firstName);
        order.setCustomerLastName(lastName);
        order.setCustomerEmail(email);
        order.setCustomerPhone(phone);
        order.setShippingStreet(street);
        order.setShippingCity(city);
        order.setShippingZipCode(zip);
        order.setShippingCountry(country);
        order.setOrderDate(orderDate);
        order.setDeliveryDate(deliveryDate);
        order.setStatus(status);
        order.setPaymentMethod(payment);
        order.setPriority(priority);
        order.setNotes(notes);
        for (var itemData : items) {
            var orderItem = new OrderItem(UUID.randomUUID());
            orderItem.setDescription(itemData.description);
            orderItem.setQuantity(itemData.quantity);
            orderItem.setUnitPrice(itemData.unitPrice);
            order.getItems().add(orderItem);
        }
        orders.put(order.getId(), order);
    }

    private static OrderItemData item(String description, int quantity, String unitPrice) {
        return new OrderItemData(description, new BigDecimal(quantity), new BigDecimal(unitPrice));
    }

    private record OrderItemData(String description, BigDecimal quantity, BigDecimal unitPrice) {
    }

    public Optional<Order> findOrderById(UUID id) {
        return Optional.ofNullable(orders.get(id)).map(Order::new);
    }

    public void saveOrder(Order order) {
        var copy = new Order(order);
        orders.put(order.getId(), copy);
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
