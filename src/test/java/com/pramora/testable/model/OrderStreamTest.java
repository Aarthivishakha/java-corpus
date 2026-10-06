package com.pramora.testable.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Test;

/** Covers the Java 8 surface: streams, Optional, java.time and default methods. */
public class OrderStreamTest {

    private Order order() {
        Order o = new Order("O-8", new Customer("C-8", "Eight", LoyaltyTier.SILVER, 6));
        o.addLine(new OrderLine("SKU-1", 2, 1000L));
        o.addLine(new OrderLine("SKU-2", 1, 7500L));
        return o;
    }

    @Test
    public void subtotalSumsViaStream() {
        assertEquals(9500L, order().subtotalCents());
    }

    @Test
    public void largestLineFound() {
        Optional<OrderLine> largest = order().largestLine();
        assertTrue(largest.isPresent());
        assertEquals("SKU-2", largest.get().getSku());
    }

    @Test
    public void emptyOrderHasNoLargestLine() {
        Order o = new Order("O-9", new Customer("C-9", "Nine", LoyaltyTier.STANDARD, 1));
        assertFalse(o.largestLine().isPresent());
    }

    @Test
    public void defaultPlacedOnDate() {
        assertEquals(LocalDate.of(2026, 1, 15), order().getPlacedOn());
    }

    @Test
    public void terminalStatusesIdentified() {
        assertTrue(OrderStatus.FULFILLED.isTerminal());
        assertTrue(OrderStatus.REJECTED.isTerminal());
        assertFalse(OrderStatus.DRAFT.isTerminal());
    }

    @Test
    public void priceableDefaultMethods() {
        Priceable free = () -> 0L;
        Priceable paid = () -> 250L;
        assertTrue(free.isFree());
        assertEquals("free", free.describe());
        assertEquals("250c", paid.describe());
    }
}
