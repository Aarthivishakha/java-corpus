package com.pramora.testable.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.pramora.testable.model.Customer;
import com.pramora.testable.model.LoyaltyTier;
import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;
import com.pramora.testable.model.OrderStatus;

/** Covers the Java 14 switch expressions carried by {@link OrderClassifier}. */
public class OrderClassifierTest {

    private final OrderClassifier classifier = new OrderClassifier();

    private Order order(LoyaltyTier tier, int orderCount, OrderStatus status, long unitCents, int qty) {
        Order o = new Order("O-14", new Customer("C-14", "Fourteen", tier, orderCount));
        o.addLine(new OrderLine("SKU-1", qty, unitCents));
        o.setStatus(status);
        return o;
    }

    private Order lowRiskSubmitted() {
        return order(LoyaltyTier.GOLD, 20, OrderStatus.SUBMITTED, 1000L, 2);
    }

    private Order highRiskSubmitted() {
        // first-time customer, large subtotal, standard tier - the RiskScorer nesting fixture
        return order(LoyaltyTier.STANDARD, 0, OrderStatus.SUBMITTED, 90000L, 1);
    }

    @Test
    public void draftIsHeld() {
        assertEquals(OrderClassifier.Queue.HOLD,
                classifier.classify(order(LoyaltyTier.GOLD, 5, OrderStatus.DRAFT, 1000L, 1)));
    }

    @Test
    public void terminalStatusesShareOneRule() {
        assertEquals(OrderClassifier.Queue.CLOSED,
                classifier.classify(order(LoyaltyTier.GOLD, 5, OrderStatus.REJECTED, 1000L, 1)));
        assertEquals(OrderClassifier.Queue.CLOSED,
                classifier.classify(order(LoyaltyTier.GOLD, 5, OrderStatus.FULFILLED, 1000L, 1)));
    }

    @Test
    public void lowRiskSmallOrderIsAutoApproved() {
        assertEquals(OrderClassifier.Queue.AUTO, classifier.classify(lowRiskSubmitted()));
    }

    @Test
    public void lowRiskLargeOrderIsPrioritised() {
        Order big = order(LoyaltyTier.PLATINUM, 40, OrderStatus.PRICED, 60000L, 2);
        assertEquals(OrderClassifier.Queue.PRIORITY, classifier.classify(big));
    }

    @Test
    public void highRiskGoesToReviewViaTheYieldBranch() {
        assertEquals(OrderClassifier.Queue.REVIEW, classifier.classify(highRiskSubmitted()));
    }

    @Test
    public void nullOrderIsHeld() {
        assertEquals(OrderClassifier.Queue.HOLD, classifier.classify(null));
        assertEquals(72, classifier.slaHours(null));
    }

    @Test
    public void slaFollowsTheTierThenTheQueue() {
        assertEquals(24, classifier.slaHours(order(LoyaltyTier.SILVER, 20, OrderStatus.SUBMITTED, 1000L, 2)));
        assertEquals(48, classifier.slaHours(order(LoyaltyTier.STANDARD, 20, OrderStatus.SUBMITTED, 1000L, 2)));
        assertEquals(12, classifier.slaHours(lowRiskSubmitted()));
    }

    @Test
    public void prioritySlaIsHalvedAndFlooredAtOne() {
        Order platinumBig = order(LoyaltyTier.PLATINUM, 40, OrderStatus.PRICED, 60000L, 2);
        assertEquals(2, classifier.slaHours(platinumBig));
    }

    @Test
    public void closedOrdersHaveNoSla() {
        assertEquals(0, classifier.slaHours(order(LoyaltyTier.GOLD, 5, OrderStatus.FULFILLED, 1000L, 1)));
    }

    @Test
    public void reviewSlaIsDoubled() {
        assertEquals(96, classifier.slaHours(highRiskSubmitted()));
    }

    @Test
    public void everyQueueHasAReason() {
        for (OrderStatus status : OrderStatus.values()) {
            String reason = classifier.reason(order(LoyaltyTier.GOLD, 5, status, 1000L, 1));
            assertFalse("empty reason for " + status, reason.isEmpty());
        }
    }

    @Test
    public void needsAttentionCoversReviewAndHold() {
        assertTrue(classifier.needsAttention(highRiskSubmitted()));
        assertTrue(classifier.needsAttention(order(LoyaltyTier.GOLD, 5, OrderStatus.DRAFT, 1000L, 1)));
        assertFalse(classifier.needsAttention(lowRiskSubmitted()));
        assertFalse(classifier.needsAttention(order(LoyaltyTier.GOLD, 5, OrderStatus.FULFILLED, 1000L, 1)));
    }

    @Test
    public void staticQueueForCoversEveryStatus() {
        assertEquals(OrderClassifier.Queue.HOLD,
                OrderClassifier.queueFor(OrderStatus.DRAFT, LoyaltyTier.GOLD));
        assertEquals(OrderClassifier.Queue.CLOSED,
                OrderClassifier.queueFor(OrderStatus.REJECTED, LoyaltyTier.GOLD));
        assertEquals(OrderClassifier.Queue.PRIORITY,
                OrderClassifier.queueFor(OrderStatus.SUBMITTED, LoyaltyTier.PLATINUM));
        assertEquals(OrderClassifier.Queue.AUTO,
                OrderClassifier.queueFor(OrderStatus.PRICED, LoyaltyTier.SILVER));
    }

    @Test
    public void nullScorerFallsBackRatherThanThrowing() {
        assertEquals(OrderClassifier.Queue.AUTO, new OrderClassifier(null).classify(lowRiskSubmitted()));
    }
}
