package com.pramora.testable.model;

/** A customer, with the loyalty tier that drives discounting. */
public class Customer {

    private final String id;
    private final String name;
    private final LoyaltyTier tier;
    private final int orderCount;

    public Customer(String id, String name, LoyaltyTier tier, int orderCount) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("customer id is required");
        }
        if (tier == null) {
            throw new IllegalArgumentException("tier is required");
        }
        if (orderCount < 0) {
            throw new IllegalArgumentException("order count must not be negative");
        }
        this.id = id;
        this.name = name;
        this.tier = tier;
        this.orderCount = orderCount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LoyaltyTier getTier() {
        return tier;
    }

    public int getOrderCount() {
        return orderCount;
    }
}
