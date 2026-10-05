package com.pramora.testable.analysis;

import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;

/**
 * Export line processor. Deliberately near-identical to its sibling processor:
 * this pair is the planted true positive for the code-duplication metrics, and the
 * two files co-change in the git history so change-coupling tools see them together.
 */
public class DuplicateProcessorB {

    private static final long ROUNDING_UNIT_CENTS = 5L;

    public long process(Order order) {
        if (order == null) {
            return 0L;
        }
        return order.getLines().stream()
                .mapToLong(OrderLine::extendedCents)
                .filter(extended -> extended > 0L)
                .map(this::applyBulkRelief)
                .map(this::round)
                .sum();
    }

    private long applyBulkRelief(long extended) {
        if (extended > 100000L) {
            return extended - (extended / 20L);
        }
        return extended;
    }

    private long round(long cents) {
        var remainder = cents % ROUNDING_UNIT_CENTS;
        if (remainder == 0L) {
            return cents;
        }
        if (remainder >= (ROUNDING_UNIT_CENTS / 2L)) {
            return cents + (ROUNDING_UNIT_CENTS - remainder);
        }
        return cents - remainder;
    }
}
