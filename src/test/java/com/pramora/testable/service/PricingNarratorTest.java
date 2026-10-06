package com.pramora.testable.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pramora.testable.model.OrderStatus;
import com.pramora.testable.model.PricingEvent;
import org.junit.Test;

/** Covers the sealed hierarchy, instanceof patterns, switch expressions and text blocks. */
public class PricingNarratorTest {

    private final PricingNarrator narrator = new PricingNarrator();

    @Test
    public void describesADiscount() {
        var event = new PricingEvent.Discounted("O-1", 250L, 5);
        assertEquals("discount of 250c applied (5%)", narrator.describe(event));
    }

    @Test
    public void describesShipping() {
        assertEquals("shipping charged at 799c",
                narrator.describe(new PricingEvent.ShippingCharged("O-2", 799L)));
    }

    @Test
    public void describesARejection() {
        assertEquals("rejected: no stock",
                narrator.describe(new PricingEvent.Rejected("O-3", "no stock")));
    }

    @Test
    public void reportUsesTheTextBlockTemplate() {
        var report = narrator.report(new PricingEvent.Rejected("O-4", "fraud"), OrderStatus.REJECTED);
        assertEquals("order   : O-4\noutcome : rejected: fraud\nterminal: true\n", report);
    }

    @Test
    public void severityIsASwitchExpression() {
        assertEquals(0, narrator.severity(OrderStatus.DRAFT));
        assertEquals(0, narrator.severity(OrderStatus.SUBMITTED));
        assertEquals(1, narrator.severity(OrderStatus.PRICED));
        assertEquals(2, narrator.severity(OrderStatus.FULFILLED));
        assertEquals(3, narrator.severity(OrderStatus.REJECTED));
    }

    @Test
    public void favourableUsesInstanceofPatterns() {
        assertTrue(narrator.isFavourable(new PricingEvent.Discounted("O-5", 10L, 3)));
        assertTrue(narrator.isFavourable(new PricingEvent.ShippingCharged("O-6", 0L)));
        assertFalse(narrator.isFavourable(new PricingEvent.ShippingCharged("O-7", 799L)));
        assertFalse(narrator.isFavourable("not an event"));
    }

    @Test
    public void recordsGiveValueEquality() {
        assertEquals(new PricingEvent.Discounted("O-8", 5L, 1),
                new PricingEvent.Discounted("O-8", 5L, 1));
    }
}
