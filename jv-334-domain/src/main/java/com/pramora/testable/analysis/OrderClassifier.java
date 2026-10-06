package com.pramora.testable.analysis;

import com.pramora.testable.model.Customer;
import com.pramora.testable.model.LoyaltyTier;
import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderStatus;

/**
 * Routes an order to a handling queue and describes why.
 *
 * <p>This class carries the Java 14 <em>syntax</em> lock. Switch expressions (JEP 361)
 * became final in Java 14 after two rounds of preview - they were preview in Java 12
 * (JEP 325) and again in Java 13 (JEP 354), which is why neither of those families could
 * use them under the corpus's preview-off rule. Everything below fails to parse under
 * {@code --release 13}:
 *
 * <ul>
 *   <li>arrow-form switch rules ({@code case X -> ...})</li>
 *   <li>multiple case labels on one rule ({@code case X, Y -> ...})</li>
 *   <li>{@code yield} from a block-bodied rule</li>
 *   <li>switch used as an expression at all</li>
 * </ul>
 *
 * <p>Java 14's other headline features are deliberately absent: records, pattern matching
 * for {@code instanceof} and text blocks were all still preview in 14 (final in 16, 16 and
 * 15 respectively). They belong to later families.
 */
public final class OrderClassifier {

    /** Handling queues an order can be routed to. */
    public enum Queue {
        AUTO,
        REVIEW,
        PRIORITY,
        HOLD,
        CLOSED
    }

    private static final long PRIORITY_THRESHOLD_CENTS = 100000L;

    private final RiskScorer scorer;

    public OrderClassifier() {
        this(new RiskScorer());
    }

    public OrderClassifier(RiskScorer scorer) {
        this.scorer = scorer == null ? new RiskScorer() : scorer;
    }

    /**
     * Picks the queue for an order. The outer switch is an expression with a block-bodied
     * rule that {@code yield}s - both Java 14.
     */
    public Queue classify(Order order) {
        if (order == null) {
            return Queue.HOLD;
        }
        return switch (order.getStatus()) {
            case DRAFT -> Queue.HOLD;
            case REJECTED, FULFILLED -> Queue.CLOSED;
            case SUBMITTED, PRICED -> {
                int risk = scorer.score(order);
                if (risk >= 50) {
                    yield Queue.REVIEW;
                }
                yield order.subtotalCents() >= PRIORITY_THRESHOLD_CENTS
                        ? Queue.PRIORITY
                        : Queue.AUTO;
            }
        };
    }

    /** Service-level target in hours. An arrow switch over the loyalty tier. */
    public int slaHours(Order order) {
        if (order == null) {
            return 72;
        }
        Customer customer = order.getCustomer();
        int base = switch (customer.getTier()) {
            case PLATINUM -> 4;
            case GOLD -> 12;
            case SILVER -> 24;
            case STANDARD -> 48;
        };
        return switch (classify(order)) {
            case PRIORITY -> Math.max(1, base / 2);
            case REVIEW, HOLD -> base * 2;
            case CLOSED -> 0;
            case AUTO -> base;
        };
    }

    /** Human-readable reason. Multiple case labels sharing one rule. */
    public String reason(Order order) {
        Queue queue = classify(order);
        return switch (queue) {
            case AUTO -> "auto-approved";
            case PRIORITY -> "high value, expedited";
            case REVIEW -> "risk score at or above fifty";
            case HOLD -> "not yet submitted";
            case CLOSED -> "already terminal";
        };
    }

    /** True when the order still needs someone to look at it. */
    public boolean needsAttention(Order order) {
        return switch (classify(order)) {
            case REVIEW, HOLD -> true;
            case AUTO, PRIORITY, CLOSED -> false;
        };
    }

    /** Convenience overload used by the entry points. */
    public static Queue queueFor(OrderStatus status, LoyaltyTier tier) {
        return switch (status) {
            case DRAFT -> Queue.HOLD;
            case REJECTED, FULFILLED -> Queue.CLOSED;
            case SUBMITTED, PRICED -> tier == LoyaltyTier.PLATINUM ? Queue.PRIORITY : Queue.AUTO;
        };
    }
}
