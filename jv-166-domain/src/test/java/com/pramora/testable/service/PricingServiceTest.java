package com.pramora.testable.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pramora.testable.model.Customer;
import com.pramora.testable.model.LoyaltyTier;
import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;
import org.junit.Before;
import org.junit.Test;

public class PricingServiceTest {

    private PricingService service;

    @Before
    public void setUp() {
        service = new PricingService(new DiscountPolicy());
    }

    private Order order(LoyaltyTier tier, int orderCount, long unitCents, int qty) {
        Order o = new Order("O-1", new Customer("C-1", "Test", tier, orderCount));
        o.addLine(new OrderLine("SKU-1", qty, unitCents));
        return o;
    }

    @Test
    public void emptyOrderCostsNothing() {
        Order o = new Order("O-0", new Customer("C-0", "Test", LoyaltyTier.STANDARD, 0));
        assertEquals(0L, service.priceCents(o));
    }

    @Test
    public void newStandardCustomerGetsNoDiscountAndPaysShipping() {
        Order o = order(LoyaltyTier.STANDARD, 1, 1000L, 2);
        assertEquals(2000L + 799L, service.priceCents(o));
    }

    @Test
    public void platinumShipsFreeBelowThreshold() {
        Order o = order(LoyaltyTier.PLATINUM, 1, 1000L, 1);
        assertEquals(880L, service.priceCents(o));
    }

    @Test
    public void goldShipsFreeAboveTwentyFiveDollars() {
        Order o = order(LoyaltyTier.GOLD, 1, 3000L, 1);
        assertEquals(0L, service.shippingCents(o, 2790L));
    }

    @Test
    public void freeShippingAboveFiftyDollars() {
        Order o = order(LoyaltyTier.STANDARD, 5, 6000L, 1);
        assertEquals(0L, service.shippingCents(o, 6000L));
    }

    @Test
    public void bulkOrderDetected() {
        Order o = order(LoyaltyTier.SILVER, 5, 100L, 12);
        assertTrue(service.isBulkOrder(o));
    }

    @Test
    public void smallOrderIsNotBulk() {
        Order o = order(LoyaltyTier.SILVER, 5, 100L, 2);
        assertFalse(service.isBulkOrder(o));
    }
}
