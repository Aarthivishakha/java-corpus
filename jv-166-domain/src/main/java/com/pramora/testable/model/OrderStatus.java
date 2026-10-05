package com.pramora.testable.model;

import java.util.Set;

/** Lifecycle states an order moves through. */
public enum OrderStatus {
    DRAFT,
    SUBMITTED,
    PRICED,
    REJECTED,
    FULFILLED;

    private static final Set<OrderStatus> TERMINAL =
            Set.copyOf(Set.of(REJECTED, FULFILLED));

    /** Set.copyOf is Java 10, wrapping the Java 9 Set.of factory. */
    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }
}
