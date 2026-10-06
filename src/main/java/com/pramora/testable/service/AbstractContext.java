package com.pramora.testable.service;

/** Base for pricing contexts. Exists so its subclass can use a flexible constructor body. */
public abstract class AbstractContext {

    private final String orderId;
    private final int attempt;

    protected AbstractContext(String orderId, int attempt) {
        this.orderId = orderId;
        this.attempt = attempt;
    }

    public String orderId() {
        return orderId;
    }

    public int attempt() {
        return attempt;
    }
}
