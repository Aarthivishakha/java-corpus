package com.pramora.testable.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.pramora.testable.analysis.RiskScorer;
import com.pramora.testable.util.InputSanitizer;
import org.junit.Test;

/** Covers the Java 9 surface: Optional.or/stream, Stream.takeWhile, Set.of, private
 *  interface methods, try-with-resources on an effectively-final variable, and the
 *  diamond operator on an anonymous class. */
public class Java9FeatureTest {

    private Order order() {
        Order o = new Order("O-9", new Customer("C-9", "Nine", LoyaltyTier.SILVER, 6));
        o.addLine(new OrderLine("SKU-1", 2, 1000L));
        o.addLine(new OrderLine("SKU-2", 1, 7500L));
        return o;
    }

    @Test
    public void optionalOrFallsBack() {
        Order empty = new Order("O-E", new Customer("C-E", "E", LoyaltyTier.STANDARD, 1));
        assertFalse(empty.largestLineOrFirst().isPresent());
        assertEquals("SKU-2", order().largestLineOrFirst().get().getSku());
    }

    @Test
    public void optionalStreamFlattens() {
        assertEquals(List.of("SKU-2"), order().largestSku());
    }

    @Test
    public void takeWhileStopsAtTheFirstFreeLine() {
        Order o = order();
        o.addLine(new OrderLine("SKU-FREE", 1, 0L));
        o.addLine(new OrderLine("SKU-3", 1, 100L));
        assertEquals(2, o.linesUntilFree().size());
    }

    @Test
    public void setOfBacksTheTerminalCheck() {
        assertTrue(OrderStatus.FULFILLED.isTerminal());
        assertTrue(OrderStatus.REJECTED.isTerminal());
        assertFalse(OrderStatus.DRAFT.isTerminal());
    }

    @Test
    public void privateInterfaceMethodBacksDescribe() {
        Priceable free = () -> 0L;
        Priceable paid = () -> 250L;
        assertEquals("free", free.describe());
        assertEquals("250c", paid.describe());
    }

    @Test
    public void tryWithResourcesOnEffectivelyFinalVariable() throws IOException {
        assertEquals("ordr123", InputSanitizer.sanitizeStream("or'dr;123"));
        assertEquals("", InputSanitizer.sanitizeStream(null));
    }

    @Test
    public void diamondOnAnonymousClassBacksRiskiestLine() {
        assertEquals("SKU-2", new RiskScorer().riskiestLine(order()).get().getSku());
        assertFalse(new RiskScorer().riskiestLine(null).isPresent());
    }
}
