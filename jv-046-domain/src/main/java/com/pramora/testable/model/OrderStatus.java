package com.pramora.testable.model;

import java.util.Set;

/** Lifecycle states an order moves through. */
public enum OrderStatus {
    DRAFT,
    SUBMITTED,
    PRICED,
    REJECTED,
    FULFILLED;

    private static final Set<OrderStatus> TERMINAL = Set.of(REJECTED, FULFILLED);

    /** Set.of is a Java 9 factory method; it does not exist in Java 8. */
    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }
}
