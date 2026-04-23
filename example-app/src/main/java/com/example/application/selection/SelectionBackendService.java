package com.example.application.selection;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@NullMarked
@Service
class SelectionBackendService {

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

    private final Map<UUID, Product> products;

    SelectionBackendService() {
        products = IntStream.range(0, 100)
                .mapToObj(i -> new Product(
                        UUID.randomUUID(),
                        NAMES[i % NAMES.length],
                        CATEGORIES[i % CATEGORIES.length],
                        PRICES[i % PRICES.length]
                ))
                .collect(Collectors.toMap(Product::id, p -> p));
    }

    public Collection<Product> getAllProducts() {
        return products.values();
    }

    public Optional<Product> getProduct(UUID id) {
        return Optional.ofNullable(products.get(id));
    }
}
