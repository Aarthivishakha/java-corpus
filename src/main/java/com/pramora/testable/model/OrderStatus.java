package com.pramora.testable.model;

/** Lifecycle states an order moves through. */
public enum OrderStatus {
    DRAFT,
    SUBMITTED,
    PRICED,
    REJECTED,
    FULFILLED;

    /** Default interface-style helper; Java 8 allows this on enums via the enum body. */
    public boolean isTerminal() {
        return this == REJECTED || this == FULFILLED;
    }
}
