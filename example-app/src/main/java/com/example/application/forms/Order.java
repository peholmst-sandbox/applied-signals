package com.example.application.forms;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@NullMarked
class Order implements Serializable {

    enum OrderStatus {
        DRAFT, PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }

    enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, PAYPAL, INVOICE
    }

    enum Priority {
        LOW, NORMAL, HIGH, EXPRESS
    }

    private final UUID id;
    private final List<OrderItem> items;
    private String customerFirstName = "";
    private String customerLastName = "";
    private String customerEmail = "";
    private String customerPhone = "";
    private String shippingStreet = "";
    private String shippingCity = "";
    private String shippingZipCode = "";
    private String shippingCountry = "";
    private LocalDate orderDate = LocalDate.now();
    private @Nullable LocalDate deliveryDate;
    private OrderStatus status = OrderStatus.DRAFT;
    private PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
    private Priority priority = Priority.NORMAL;
    private @Nullable String notes;


    public Order(UUID id) {
        this.id = id;
        this.items = new ArrayList<>();
    }

    public Order(Order source) {
        this.id = source.id;
        this.items = new ArrayList<>();
        for (OrderItem item : source.items) {
            this.items.add(new OrderItem(item));
        }
        this.customerFirstName = source.customerFirstName;
        this.customerLastName = source.customerLastName;
        this.customerEmail = source.customerEmail;
        this.customerPhone = source.customerPhone;
        this.shippingStreet = source.shippingStreet;
        this.shippingCity = source.shippingCity;
        this.shippingZipCode = source.shippingZipCode;
        this.shippingCountry = source.shippingCountry;
        this.orderDate = source.orderDate;
        this.deliveryDate = source.deliveryDate;
        this.status = source.status;
        this.paymentMethod = source.paymentMethod;
        this.priority = source.priority;
        this.notes = source.notes;
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public void setCustomerFirstName(String customerFirstName) {
        this.customerFirstName = customerFirstName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getShippingStreet() {
        return shippingStreet;
    }

    public void setShippingStreet(String shippingStreet) {
        this.shippingStreet = shippingStreet;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }

    public String getShippingZipCode() {
        return shippingZipCode;
    }

    public void setShippingZipCode(String shippingZipCode) {
        this.shippingZipCode = shippingZipCode;
    }

    public String getShippingCountry() {
        return shippingCountry;
    }

    public void setShippingCountry(String shippingCountry) {
        this.shippingCountry = shippingCountry;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public @Nullable LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(@Nullable LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public @Nullable String getNotes() {
        return notes;
    }

    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return items.stream().map(OrderItem::getSubTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
