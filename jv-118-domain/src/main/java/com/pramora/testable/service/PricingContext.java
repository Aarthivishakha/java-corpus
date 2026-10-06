package com.pramora.testable.service;

import com.pramora.testable.util.InputSanitizer;

/// The context a pricing run happens in.
///
/// This is a **flexible constructor body** (JEP 513, final in Java 25): statements run
/// *before* the `super(...)` call, so arguments can be validated and normalised without
/// a static helper method. Under `--release 21` javac rejects it outright.
///
/// The doc comment itself is Markdown (JEP 467, final in Java 23) rather than HTML.
public class PricingContext extends AbstractContext {

    private final boolean retry;

    public PricingContext(String rawOrderId, int rawAttempt) {
        String normalised = InputSanitizer.sanitize(rawOrderId);
        if (normalised.isEmpty()) {
            normalised = "O-UNKNOWN";
        }
        int attempt = Math.max(rawAttempt, 0);
        super(normalised, attempt);
        this.retry = attempt > 0;
    }

    /// True when this is not the first pricing attempt for the order.
    public boolean isRetry() {
        return retry;
    }
}
