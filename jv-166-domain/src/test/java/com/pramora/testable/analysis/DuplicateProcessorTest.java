package com.pramora.testable.analysis;

import static org.junit.Assert.assertEquals;

import com.pramora.testable.model.Customer;
import com.pramora.testable.model.LoyaltyTier;
import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;
import org.junit.Test;

public class DuplicateProcessorTest {

    private Order order(int qty, long unitCents) {
        Order o = new Order("O-1", new Customer("C-1", "Test", LoyaltyTier.SILVER, 5));
        o.addLine(new OrderLine("SKU-1", qty, unitCents));
        return o;
    }

    @Test
    public void bothProcessorsAgreeOnSmallOrders() {
        Order o = order(2, 333L);
        assertEquals(new DuplicateProcessorA().process(o), new DuplicateProcessorB().process(o));
    }

    @Test
    public void bulkReliefAppliedAboveOneThousandDollars() {
        Order o = order(2, 60000L);
        assertEquals(114000L, new DuplicateProcessorA().process(o));
    }

    @Test
    public void noReliefBelowThreshold() {
        Order o = order(2, 1000L);
        assertEquals(2000L, new DuplicateProcessorA().process(o));
    }

    @Test
    public void nullOrderReturnsZero() {
        assertEquals(0L, new DuplicateProcessorB().process(null));
    }
}
