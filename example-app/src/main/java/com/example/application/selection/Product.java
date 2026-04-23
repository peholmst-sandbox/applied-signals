package com.example.application.selection;

import java.util.UUID;

public record Product(UUID id, String name, String category, double price) {
}
