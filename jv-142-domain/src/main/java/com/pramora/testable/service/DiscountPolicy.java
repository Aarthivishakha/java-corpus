package com.pramora.testable.service;

import com.pramora.testable.model.Customer;
import com.pramora.testable.model.LoyaltyTier;
import com.pramora.testable.model.Order;

/** Computes the discount applied to an order, in cents. */
public class DiscountPolicy {

    private static final long LOYALTY_ORDER_BONUS_THRESHOLD = 25L;

    public long discountCents(Order order) {
        if (order == null) {
            return 0L;
        }
        Customer customer = order.getCustomer();
        int percent = customer.getTier().getDiscountPercent();

        if (customer.getOrderCount() >= LOYALTY_ORDER_BONUS_THRESHOLD) {
            percent = percent + 2;
        }
        if (customer.getTier() == LoyaltyTier.STANDARD && customer.getOrderCount() < 3) {
            percent = 0;
        }
        if (percent > 20) {
            percent = 20;
        }
        if (percent <= 0) {
            return 0L;
        }
        return (order.subtotalCents() * percent) / 100L;
    }
}
