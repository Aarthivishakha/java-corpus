package refineryplan;

/**
 * Plans a refinery batch schedule from a declared throughput target.
 */
public final class RefineryPlan {

    private final double throughputTarget;

    /**
     * Creates a plan with the given throughput target.
     *
     * @param throughputTarget the target throughput, barrels per hour
     */
    public RefineryPlan(double throughputTarget) {
        this.throughputTarget = throughputTarget;
    }

    /**
     * Estimates the hours needed to process a given batch volume.
     *
     * @param batchVolume the batch volume in barrels
     * @return the estimated processing hours, or zero if the target is zero
     */
    public double hoursFor(double batchVolume) {
        if (throughputTarget == 0.0) {
            return 0.0;
        }
        return batchVolume / throughputTarget;
    }
}
