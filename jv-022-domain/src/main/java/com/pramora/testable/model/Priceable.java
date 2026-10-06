package com.pramora.testable.model;

/**
 * Anything that carries a cents amount. The default method is a Java 8 feature and
 * will not compile under -source 7.
 */
public interface Priceable {

    long amountCents();

    default boolean isFree() {
        return amountCents() == 0L;
    }

    default String describe() {
        return isFree() ? "free" : amountCents() + "c";
    }
}
