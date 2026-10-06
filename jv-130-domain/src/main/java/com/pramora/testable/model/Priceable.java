package com.pramora.testable.model;

/**
 * Anything that carries a cents amount. The default methods are Java 8; the private
 * interface method below is Java 9 and will not compile under -source 8.
 */
public interface Priceable {

    long amountCents();

    default boolean isFree() {
        return amountCents() == 0L;
    }

    default String describe() {
        return isFree() ? "free" : suffixed();
    }

    private String suffixed() {
        return amountCents() + "c";
    }
}
