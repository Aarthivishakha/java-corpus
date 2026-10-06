package com.pramora.testable.analysis;

import com.pramora.testable.model.Customer;
import com.pramora.testable.model.LoyaltyTier;
import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;

/**
 * Scores an order for manual-review risk. Nested conditionals here are intentional -
 * this is the fixture for cognitive complexity, which penalises nesting where
 * cyclomatic complexity does not.
 */
public class RiskScorer {

    /** Diamond on an anonymous class - Java 9 syntax, rejected under -source 8. */
    private static final java.util.Comparator<OrderLine> BY_VALUE = new java.util.Comparator<>() {
        @Override
        public int compare(OrderLine a, OrderLine b) {
            return Long.compare(a.extendedCents(), b.extendedCents());
        }
    };

    /** The riskiest line is the largest one. */
    public java.util.Optional<OrderLine> riskiestLine(Order order) {
        if (order == null) {
            return java.util.Optional.empty();
        }
        return order.getLines().stream().max(BY_VALUE);
    }

    public int score(Order order) {
        if (order == null) {
            return 0;
        }
        int score = 0;
        Customer customer = order.getCustomer();

        if (customer.getOrderCount() < 2) {
            score += 30;
            if (order.subtotalCents() > 50000L) {
                score += 25;
                if (customer.getTier() == LoyaltyTier.STANDARD) {
                    score += 15;
                }
            }
        } else {
            if (order.subtotalCents() > 200000L) {
                score += 20;
            }
        }

        for (OrderLine line : order.getLines()) {
            if (line.getQuantity() > 50) {
                score += 10;
            }
            if (line.getUnitPriceCents() == 0L) {
                score += 5;
            }
        }

        return Math.min(score, 100);
    }

    public boolean requiresReview(Order order) {
        return score(order) >= 50;
    }
}
