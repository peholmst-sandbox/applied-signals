package com.example.application.sorting;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@NullMarked
@Service
class SortingBackendService {

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

    private final List<Product> products;

    SortingBackendService() {
        products = IntStream.range(0, 100)
                .mapToObj(i -> new Product(
                        NAMES[i % NAMES.length],
                        CATEGORIES[i % CATEGORIES.length],
                        PRICES[i % PRICES.length]
                ))
                .toList();
    }

    public List<Product> getAllProducts() {
        return products;
    }

    public Stream<Product> fetch(Query<Product, Void> query) {
        return sort(products.stream(), query.getSortOrders()).skip(query.getOffset()).limit(query.getLimit());
    }

    private Stream<Product> sort(Stream<Product> products, List<QuerySortOrder> sortOrders) {
        var comparator = buildComparator(sortOrders);
        if (comparator.isEmpty()) {
            return products;
        } else {
            return products.sorted(comparator.get());
        }
    }

    private Optional<Comparator<Product>> buildComparator(List<QuerySortOrder> sortOrders) {
        if (sortOrders.isEmpty()) {
            return Optional.empty();
        }

        Comparator<Product> comparator = null;
        for (var sortOrder : sortOrders) {
            var fieldComparator = fieldComparator(sortOrder.getSorted());
            if (sortOrder.getDirection() == SortDirection.DESCENDING) {
                fieldComparator = fieldComparator.reversed();
            }
            comparator = comparator == null ? fieldComparator : comparator.thenComparing(fieldComparator);
        }
        return Optional.of(comparator);
    }

    private Comparator<Product> fieldComparator(String field) {
        return switch (field) {
            case "name" -> Comparator.comparing(Product::name, String.CASE_INSENSITIVE_ORDER);
            case "category" -> Comparator.comparing(Product::category, String.CASE_INSENSITIVE_ORDER);
            case "price" -> Comparator.comparingDouble(Product::price);
            default -> throw new IllegalArgumentException("Unknown sort field: " + field);
        };
    }

    public int count(Query<Product, Void> query) {
        return products.size();
    }
}
