package com.pramora.testable.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Test;

/** Covers the Java 25 surface: stream gatherers, on top of Java 21 sequenced collections. */
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
        assertEquals("SKU-2", largest.get().sku());
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

    @Test
    public void emptyOrderReportsEmptyViaOptionalIsEmpty() {
        var empty = new Order("O-E", new Customer("C-E", "E", LoyaltyTier.STANDARD, 1));
        assertTrue(empty.isEmpty());
        assertFalse(order().isEmpty());
    }

    @Test
    public void sequencedCollectionAccessors() {
        var o = order();
        assertEquals("SKU-1", o.firstLine().orElseThrow().sku());
        assertEquals("SKU-2", o.lastLine().orElseThrow().sku());
        assertEquals("SKU-2", o.linesReversed().get(0).sku());
    }

    @Test
    public void gatherersWindowTheLines() {
        var o = order();
        o.addLine(new OrderLine("SKU-3", 1, 100L));
        var pairs = o.linePairs();
        assertEquals(2, pairs.size());
        assertEquals(2, pairs.get(0).size());
        assertEquals(1, pairs.get(1).size());
    }

    @Test
    public void gatherersScanRunningTotals() {
        assertEquals(java.util.List.of(2000L, 9500L), order().runningTotals());
    }

    @Test
    public void emptyOrderHasNoFirstOrLast() {
        var empty = new Order("O-Z", new Customer("C-Z", "Z", LoyaltyTier.STANDARD, 1));
        assertFalse(empty.firstLine().isPresent());
        assertFalse(empty.lastLine().isPresent());
    }

    @Test
    public void getLinesReturnsAnUnmodifiableList() {
        var lines = order().getLines();
        try {
            lines.add(new OrderLine("SKU-X", 1, 1L));
            org.junit.Assert.fail("expected the list to be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            assertEquals(2, lines.size());
        }
    }
}
