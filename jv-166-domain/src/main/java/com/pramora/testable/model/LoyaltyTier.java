package com.pramora.testable.model;

/** Loyalty tiers, ordered from lowest to highest benefit. */
public enum LoyaltyTier {
    STANDARD(0),
    SILVER(3),
    GOLD(7),
    PLATINUM(12);

    private final int discountPercent;

    LoyaltyTier(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }
}
