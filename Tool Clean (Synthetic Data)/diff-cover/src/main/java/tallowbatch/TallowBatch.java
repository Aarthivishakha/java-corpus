package tallowbatch;

/**
 * Tracks a single tallow-rendering batch: raw weight in, rendered
 * weight out, and the render yield that implies.
 */
public final class TallowBatch {

    private final double rawKilograms;
    private final double renderedKilograms;

    /**
     * Creates a batch record.
     *
     * @param rawKilograms      the raw material weight in kilograms
     * @param renderedKilograms the rendered output weight in kilograms
     */
    public TallowBatch(double rawKilograms, double renderedKilograms) {
        this.rawKilograms = rawKilograms;
        this.renderedKilograms = renderedKilograms;
    }

    /**
     * Computes the rendering yield as a fraction of raw weight.
     *
     * @return the yield fraction, zero if the raw weight is zero
     */
    public double renderYield() {
        if (rawKilograms == 0.0) {
            return 0.0;
        }
        return renderedKilograms / rawKilograms;
    }

    /**
     * Reports whether this batch's yield falls below an acceptable
     * threshold and should be flagged for review.
     *
     * @param minimumYield the minimum acceptable yield fraction
     * @return true if this batch's yield is below the threshold
     */
    public boolean needsReview(double minimumYield) {
        return renderYield() < minimumYield;
    }
}
