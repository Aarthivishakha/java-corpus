package com.pramora.testable.service;

import java.util.function.LongUnaryOperator;

import com.pramora.testable.model.Customer;
import com.pramora.testable.model.LoyaltyTier;
import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;

/**
 * Prices an order. Deliberately branch-heavy: this is the class the cyclomatic and
 * cognitive complexity metrics are measured against.
 */
public class PricingService {

    private static final long FREE_SHIPPING_THRESHOLD_CENTS = 5000L;
    private static final long SHIPPING_FLAT_CENTS = 799L;
    private static final int BULK_LINE_THRESHOLD = 10;

    private final DiscountPolicy policy;
    private final LongUnaryOperator floorAtZero = cents -> cents < 0L ? 0L : cents;

    public PricingService(DiscountPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("policy is required");
        }
        this.policy = policy;
    }

    public long priceCents(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order is required");
        }
        if (order.lineCount() == 0) {
            return 0L;
        }

        var subtotal = order.subtotalCents();
        var discount = policy.discountCents(order);
        long net = floorAtZero.applyAsLong(subtotal - discount);

        return net + shippingCents(order, net);
    }

    public long shippingCents(Order order, long netCents) {
        if (netCents >= FREE_SHIPPING_THRESHOLD_CENTS) {
            return 0L;
        }
        var customer = order.getCustomer();
        if (customer.getTier() == LoyaltyTier.PLATINUM) {
            return 0L;
        }
        if (customer.getTier() == LoyaltyTier.GOLD && netCents >= 2500L) {
            return 0L;
        }
        return SHIPPING_FLAT_CENTS;
    }

    public boolean isBulkOrder(Order order) {
        int units = 0;
        for (OrderLine line : order.getLines()) {
            units += line.getQuantity();
            if (units >= BULK_LINE_THRESHOLD) {
                return true;
            }
        }
        return false;
    }
}
