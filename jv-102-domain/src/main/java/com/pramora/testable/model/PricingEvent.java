package com.pramora.testable.model;

/**
 * Events emitted while pricing an order. A sealed interface with record
 * implementations: Java 17 syntax, exhaustively matched by a pattern switch in
 * PricingNarrator. Neither the seal nor the pattern switch compiles under Java 11.
 */
public sealed interface PricingEvent
        permits PricingEvent.Discounted, PricingEvent.ShippingCharged, PricingEvent.Rejected {

    String orderId();

    record Discounted(String orderId, long amountCents, int percent) implements PricingEvent {
    }

    record ShippingCharged(String orderId, long amountCents) implements PricingEvent {
    }

    record Rejected(String orderId, String reason) implements PricingEvent {
    }
}
