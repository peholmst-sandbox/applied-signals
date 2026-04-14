package com.example.application.loading;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
class BackendService {

    private static final String[] NAMES = {
            "Laptop", "Keyboard", "Mouse", "Monitor", "Headphones",
            "Webcam", "USB Hub", "Desk Lamp", "Chair", "Standing Desk"
    };

    private static final String[] CATEGORIES = {
            "Electronics", "Peripherals", "Furniture", "Accessories", "Audio"
    };

    private static final double[] PRICES = {
            999.99, 79.99, 49.99, 399.99, 149.99,
            89.99, 39.99, 59.99, 499.99, 699.99
    };

    public List<Product> getProducts() {
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (System.currentTimeMillis() % 10 == 0) {
            throw new RuntimeException("Something went wrong. Try again.");
        }
        return IntStream.range(0, 100)
                .mapToObj(i -> new Product(
                        NAMES[i % NAMES.length],
                        CATEGORIES[i % CATEGORIES.length],
                        PRICES[i % PRICES.length]
                ))
                .toList();
    }
}
