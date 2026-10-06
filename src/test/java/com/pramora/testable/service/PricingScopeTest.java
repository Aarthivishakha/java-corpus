package com.pramora.testable.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Covers flexible constructor bodies, scoped values and module imports. */
public class PricingScopeTest {

    @Test
    public void flexibleConstructorNormalisesBeforeSuper() {
        var ctx = new PricingContext("  or'der-1;  ", 2);
        assertEquals("order-1", ctx.orderId());
        assertEquals(2, ctx.attempt());
        assertTrue(ctx.isRetry());
    }

    @Test
    public void flexibleConstructorFloorsNegativeAttempts() {
        var ctx = new PricingContext("O-2", -5);
        assertEquals(0, ctx.attempt());
        assertFalse(ctx.isRetry());
    }

    @Test
    public void blankOrderIdFallsBackToPlaceholder() {
        assertEquals("O-UNKNOWN", new PricingContext("   ", 0).orderId());
    }

    @Test
    public void scopedValueIsVisibleInsideTheScope() {
        var ctx = new PricingContext("O-3", 1);
        var seen = PricingScope.with(ctx, PricingScope::currentOrderId);
        assertEquals("O-3", seen);
        assertTrue(PricingScope.with(ctx, PricingScope::currentIsRetry));
    }

    @Test
    public void scopedValueIsUnboundOutsideTheScope() {
        assertEquals("O-NONE", PricingScope.currentOrderId());
        assertFalse(PricingScope.currentIsRetry());
    }
}
