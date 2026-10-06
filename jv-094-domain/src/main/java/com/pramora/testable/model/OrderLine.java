package com.pramora.testable.model;

/**
 * A single priced line on an order. A record: Java 16 syntax, so this file alone
 * makes the branch uncompilable under --release 11.
 */
public record OrderLine(String sku, int quantity, long unitPriceCents) {

    public OrderLine {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitPriceCents < 0L) {
            throw new IllegalArgumentException("unit price must not be negative");
        }
    }

    public long extendedCents() {
        return unitPriceCents * quantity;
    }
}
