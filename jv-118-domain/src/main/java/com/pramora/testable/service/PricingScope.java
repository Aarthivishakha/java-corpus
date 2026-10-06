package com.pramora.testable.service;

import java.util.function.Supplier;

/// Carries the current pricing context down a call chain without threading it through
/// every signature.
///
/// Uses **scoped values** (JEP 506, final in Java 25). They were a preview API in Java
/// 21, so `--release 21` refuses this file with *"ScopedValue is a preview API and is
/// disabled by default"* — an API lock rather than a syntax one.
public final class PricingScope {

    private static final ScopedValue<PricingContext> CURRENT = ScopedValue.newInstance();

    private PricingScope() {
    }

    /// Runs `body` with `context` bound for the duration of the call.
    public static <T> T with(PricingContext context, Supplier<T> body) {
        return ScopedValue.where(CURRENT, context).call(body::get);
    }

    /// The order id of the context currently in scope, or `O-NONE` outside any scope.
    public static String currentOrderId() {
        return CURRENT.isBound() ? CURRENT.get().orderId() : "O-NONE";
    }

    public static boolean currentIsRetry() {
        return CURRENT.isBound() && CURRENT.get().isRetry();
    }
}
