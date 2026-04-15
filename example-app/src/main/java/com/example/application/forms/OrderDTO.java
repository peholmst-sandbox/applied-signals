package com.example.application.forms;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

record OrderDTO(
        UUID id,
        String customerName,
        LocalDate orderDate,
        @Nullable LocalDate deliveryDate,
        Order.OrderStatus status,
        Order.Priority priority,
        BigDecimal total
) {
}
