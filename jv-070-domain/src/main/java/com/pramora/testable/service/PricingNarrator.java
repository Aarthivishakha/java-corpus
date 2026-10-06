package com.pramora.testable.service;

import com.pramora.testable.model.OrderStatus;
import com.pramora.testable.model.PricingEvent;

/**
 * Renders a pricing event as text.
 *
 * Uses only features that are FINAL in Java 17: pattern matching for instanceof (16),
 * switch expressions with arrow labels (14), text blocks (15) and String.formatted (15).
 * Pattern matching for switch was still a preview in 17 and is deliberately not used
 * here - it belongs to the java21 family, where it is final.
 */
public class PricingNarrator {

    private static final String TEMPLATE = """
            order   : %s
            outcome : %s
            terminal: %s
            """;

    public String describe(PricingEvent event) {
        if (event instanceof PricingEvent.Discounted d) {
            return "discount of %dc applied (%d%%)".formatted(d.amountCents(), d.percent());
        }
        if (event instanceof PricingEvent.ShippingCharged s) {
            return "shipping charged at %dc".formatted(s.amountCents());
        }
        if (event instanceof PricingEvent.Rejected r) {
            return "rejected: " + r.reason();
        }
        throw new IllegalArgumentException("unknown event: " + event);
    }

    public String report(PricingEvent event, OrderStatus status) {
        return TEMPLATE.formatted(event.orderId(), describe(event), status.isTerminal());
    }

    /** Switch expression with arrow labels, final since Java 14. */
    public int severity(OrderStatus status) {
        return switch (status) {
            case DRAFT, SUBMITTED -> 0;
            case PRICED -> 1;
            case FULFILLED -> 2;
            case REJECTED -> 3;
        };
    }

    public boolean isFavourable(Object candidate) {
        if (candidate instanceof PricingEvent.Discounted d && d.percent() > 0) {
            return true;
        }
        return candidate instanceof PricingEvent.ShippingCharged s && s.amountCents() == 0L;
    }
}
