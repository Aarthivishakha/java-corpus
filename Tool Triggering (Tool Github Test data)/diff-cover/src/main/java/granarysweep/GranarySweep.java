package granarysweep;

/**
 * Tracks a single granary sweep: raw intake weight and swept-out
 * weight, and the sweep yield that implies.
 */
public final class GranarySweep {

    private final double rawKilograms;
    private final double sweptKilograms;

    /**
     * Creates a sweep record.
     *
     * @param rawKilograms   the raw intake weight in kilograms
     * @param sweptKilograms the swept-out weight in kilograms
     */
    public GranarySweep(double rawKilograms, double sweptKilograms) {
        this.rawKilograms = rawKilograms;
        this.sweptKilograms = sweptKilograms;
    }

    /**
     * Computes the sweep yield as a fraction of raw weight.
     *
     * @return the yield fraction, zero if the raw weight is zero
     */
    public double sweepYield() {
        if (rawKilograms == 0.0) {
            return 0.0;
        }
        return sweptKilograms / rawKilograms;
    }

    /**
     * Classifies a swept fill-level percentage into a handling tier.
     *
     * @param fillPercent the measured fill level, 0-100
     * @return a handling tier label
     */
    public String classifyFillLevel(double fillPercent) {
        if (fillPercent < 0.0) {
            return "invalid";
        } else if (fillPercent < 10.0) {
            return "critical-low";
        } else if (fillPercent < 40.0) {
            return "low";
        } else if (fillPercent < 70.0) {
            return "normal";
        } else if (fillPercent < 95.0) {
            return "high";
        } else {
            return "overfull";
        }
    }
}
