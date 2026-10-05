package beaconwatch;

/**
 * Watches a coastal beacon's signal strength and flags readings
 * outside the expected band.
 */
public final class BeaconWatch {

    private static final double MIN_STRENGTH = 10.0;
    private static final double MAX_STRENGTH = 95.0;

    /**
     * Reports whether a signal strength reading is within the
     * expected band.
     *
     * @param strength the measured signal strength
     * @return true if the reading is within the expected band
     */
    public boolean isWithinBand(double strength) {
        return strength >= MIN_STRENGTH && strength <= MAX_STRENGTH;
    }

    /**
     * Computes how far a reading is from the nearest band edge.
     *
     * @param strength the measured signal strength
     * @return the distance to the nearest edge, zero if within band
     */
    public double distanceFromBand(double strength) {
        if (strength < MIN_STRENGTH) {
            return MIN_STRENGTH - strength;
        }
        if (strength > MAX_STRENGTH) {
            return strength - MAX_STRENGTH;
        }
        return 0.0;
    }
}
