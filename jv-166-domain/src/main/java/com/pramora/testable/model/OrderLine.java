package com.pramora.testable.model;

/** A single priced line on an order. */
public class OrderLine {

    private final String sku;
    private final int quantity;
    private final long unitPriceCents;

    public OrderLine(String sku, int quantity, long unitPriceCents) {
        if (sku == null || sku.isEmpty()) {
            throw new IllegalArgumentException("sku is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitPriceCents < 0L) {
            throw new IllegalArgumentException("unit price must not be negative");
        }
        this.sku = sku;
        this.quantity = quantity;
        this.unitPriceCents = unitPriceCents;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getUnitPriceCents() {
        return unitPriceCents;
    }

    public long extendedCents() {
        return unitPriceCents * quantity;
    }
}
