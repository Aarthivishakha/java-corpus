package com.pramora.testable.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;

/**
 * An immutable, serializable record of what an order was priced at.
 *
 * <p>This class carries the Java 14 <em>API</em> lock: {@link java.io.Serial} was added in
 * Java 14 (JDK-8202385). It marks the members that make up a class's serialization
 * contract - {@code serialVersionUID}, {@code writeObject}, {@code readObject},
 * {@code readResolve}, {@code writeReplace}, {@code serialPersistentFields} - so that a
 * compiler or IDE can check them. Those members are matched by *name and signature* at
 * runtime, with no interface to implement, so a typo in {@code readObject} silently
 * disables it; {@code @Serial} is what turns that into a compile-time error.
 *
 * <p>Together with {@code analysis/OrderClassifier.java} this gives the family both kinds
 * of lock the corpus distinguishes: a parse-time lock (switch expressions, which stop the
 * parser) and an attribution-time lock (this annotation, which only surfaces once the
 * parser is past).
 */
public final class PricingSnapshot implements Serializable {

    @Serial
    private static final long serialVersionUID = 20260901L;

    private final String orderId;
    private final long subtotalCents;
    private final long discountCents;
    private final long totalCents;
    private final int riskScore;

    public PricingSnapshot(String orderId, long subtotalCents, long discountCents,
                           long totalCents, int riskScore) {
        if (orderId == null || orderId.isEmpty()) {
            throw new IllegalArgumentException("order id is required");
        }
        this.orderId = orderId;
        this.subtotalCents = subtotalCents;
        this.discountCents = discountCents;
        this.totalCents = totalCents;
        this.riskScore = riskScore;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getSubtotalCents() {
        return subtotalCents;
    }

    public long getDiscountCents() {
        return discountCents;
    }

    public long getTotalCents() {
        return totalCents;
    }

    public int getRiskScore() {
        return riskScore;
    }

    /** Net of discount, floored at zero. */
    public long netCents() {
        long net = subtotalCents - discountCents;
        return net < 0L ? 0L : net;
    }

    /** Shipping is whatever the total carries above the net. */
    public long shippingCents() {
        long shipping = totalCents - netCents();
        return shipping < 0L ? 0L : shipping;
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (orderId == null || orderId.isEmpty()) {
            throw new java.io.InvalidObjectException("order id is required");
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PricingSnapshot)) {
            return false;
        }
        PricingSnapshot that = (PricingSnapshot) other;
        return subtotalCents == that.subtotalCents
                && discountCents == that.discountCents
                && totalCents == that.totalCents
                && riskScore == that.riskScore
                && orderId.equals(that.orderId);
    }

    @Override
    public int hashCode() {
        int result = orderId.hashCode();
        result = 31 * result + Long.hashCode(subtotalCents);
        result = 31 * result + Long.hashCode(discountCents);
        result = 31 * result + Long.hashCode(totalCents);
        result = 31 * result + riskScore;
        return result;
    }

    @Override
    public String toString() {
        return "PricingSnapshot[" + orderId + " total=" + totalCents + " risk=" + riskScore + "]";
    }
}
