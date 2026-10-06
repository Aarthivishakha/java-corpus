package com.pramora.testable.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pramora.testable.model.Customer;
import com.pramora.testable.model.LoyaltyTier;
import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;
import org.junit.Test;

public class RiskScorerTest {

    private final RiskScorer scorer = new RiskScorer();

    @Test
    public void nullOrderScoresZero() {
        assertEquals(0, scorer.score(null));
    }

    @Test
    public void newCustomerLargeStandardOrderScoresHigh() {
        Order o = new Order("O-1", new Customer("C-1", "New", LoyaltyTier.STANDARD, 0));
        o.addLine(new OrderLine("SKU-1", 1, 60000L));
        assertEquals(70, scorer.score(o));
        assertTrue(scorer.requiresReview(o));
    }

    @Test
    public void establishedCustomerSmallOrderScoresLow() {
        Order o = new Order("O-2", new Customer("C-2", "Old", LoyaltyTier.GOLD, 40));
        o.addLine(new OrderLine("SKU-1", 1, 1000L));
        assertEquals(0, scorer.score(o));
        assertFalse(scorer.requiresReview(o));
    }

    @Test
    public void hugeQuantityAddsRisk() {
        Order o = new Order("O-3", new Customer("C-3", "Old", LoyaltyTier.GOLD, 40));
        o.addLine(new OrderLine("SKU-1", 60, 100L));
        assertEquals(10, scorer.score(o));
    }

    @Test
    public void scoreIsCappedAtOneHundred() {
        Order o = new Order("O-4", new Customer("C-4", "New", LoyaltyTier.STANDARD, 0));
        for (int i = 0; i < 6; i++) {
            o.addLine(new OrderLine("SKU-" + i, 60, 20000L));
        }
        assertEquals(100, scorer.score(o));
    }
}
