package com.pramora.testable.service;

import com.pramora.testable.model.OrderStatus;
import com.pramora.testable.model.PricingEvent;

/**
 * Renders a pricing event as text.
 *
 * Java 21 source level. Pattern matching for switch and record patterns were still
 * PREVIEW in Java 17 and are final in 21, so this file cannot compile under
 * --release 17 - which is the point.
 */
public class PricingNarrator {

    private static final String TEMPLATE = """
            order   : %s
            outcome : %s
            terminal: %s
            """;

    /** Pattern matching for switch over a sealed type: exhaustive with no default. */
    public String describe(PricingEvent event) {
        return switch (event) {
            case PricingEvent.Discounted d ->
                    "discount of %dc applied (%d%%)".formatted(d.amountCents(), d.percent());
            case PricingEvent.ShippingCharged s ->
                    "shipping charged at %dc".formatted(s.amountCents());
            case PricingEvent.Rejected r ->
                    "rejected: " + r.reason();
        };
    }

    /** Record deconstruction patterns, plus a guarded pattern with `when`. */
    public String classify(PricingEvent event) {
        return switch (event) {
            case PricingEvent.Discounted(String id, long cents, int pct) when pct >= 10 ->
                    "deep discount on " + id + " (" + cents + "c)";
            case PricingEvent.Discounted(String id, long cents, int ignored) ->
                    "modest discount on " + id + " (" + cents + "c)";
            case PricingEvent.ShippingCharged(String id, long cents) when cents == 0L ->
                    "free shipping on " + id;
            case PricingEvent.ShippingCharged(String id, long cents) ->
                    "shipping " + cents + "c on " + id;
            case PricingEvent.Rejected(String id, String reason) ->
                    id + " rejected: " + reason;
        };
    }

    public String report(PricingEvent event, OrderStatus status) {
        return TEMPLATE.formatted(event.orderId(), describe(event), status.isTerminal());
    }

    public int severity(OrderStatus status) {
        return switch (status) {
            case DRAFT, SUBMITTED -> 0;
            case PRICED -> 1;
            case FULFILLED -> 2;
            case REJECTED -> 3;
        };
    }

    public boolean isFavourable(Object candidate) {
        return switch (candidate) {
            case PricingEvent.Discounted d when d.percent() > 0 -> true;
            case PricingEvent.ShippingCharged s -> s.amountCents() == 0L;
            case null, default -> false;
        };
    }
}
