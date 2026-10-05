package yieldinspector;

/**
 * Inspects a batch yield fraction and classifies it for a mutation
 * testing exercise.
 */
public final class YieldInspector {

    private static final double LOW_THRESHOLD = 0.4;
    private static final double HIGH_THRESHOLD = 0.8;

    /**
     * Classifies a batch yield fraction.
     *
     * @param yieldFraction the batch yield fraction, 0-1
     * @return a classification label
     */
    public String classify(double yieldFraction) {
        if (yieldFraction < LOW_THRESHOLD) {
            return "poor";
        } else if (yieldFraction < HIGH_THRESHOLD) {
            return "acceptable";
        } else {
            return "excellent";
        }
    }

    /**
     * Reports whether a yield fraction passes the acceptance bar.
     *
     * @param yieldFraction the batch yield fraction, 0-1
     * @return true if at or above the low threshold
     */
    public boolean passes(double yieldFraction) {
        return yieldFraction >= LOW_THRESHOLD;
    }
}
