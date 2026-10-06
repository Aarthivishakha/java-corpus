package com.pramora.testable.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pramora.testable.model.OrderStatus;
import com.pramora.testable.model.PricingEvent;
import org.junit.Test;

/** Covers pattern-matching switch, record patterns, guarded patterns and null labels. */
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
    public void classifyUsesRecordDeconstructionPatterns() {
        assertEquals("deep discount on O-9 (900c)",
                narrator.classify(new PricingEvent.Discounted("O-9", 900L, 12)));
        assertEquals("modest discount on O-10 (50c)",
                narrator.classify(new PricingEvent.Discounted("O-10", 50L, 3)));
        assertEquals("free shipping on O-11",
                narrator.classify(new PricingEvent.ShippingCharged("O-11", 0L)));
        assertEquals("shipping 799c on O-12",
                narrator.classify(new PricingEvent.ShippingCharged("O-12", 799L)));
        assertEquals("O-13 rejected: fraud",
                narrator.classify(new PricingEvent.Rejected("O-13", "fraud")));
    }

    @Test
    public void nullCaseLabelIsHandled() {
        assertFalse(narrator.isFavourable(null));
    }

    @Test
    public void recordsGiveValueEquality() {
        assertEquals(new PricingEvent.Discounted("O-8", 5L, 1),
                new PricingEvent.Discounted("O-8", 5L, 1));
    }
}
