package irrigationplanner;

/**
 * Plans watering duration for a greenhouse zone based on soil
 * moisture and the zone's flow rate.
 */
public final class IrrigationPlanner {

    private static final double TARGET_MOISTURE = 0.65;

    /**
     * Computes how many minutes a zone should be watered.
     *
     * @param currentMoisture the zone's current soil moisture fraction
     * @param litersPerMinute the zone's irrigation flow rate
     * @param zoneVolumeLiters the zone's soil water-holding volume in liters
     * @return the watering duration in minutes, zero if already at target
     */
    public double minutesToWater(double currentMoisture, double litersPerMinute,
            double zoneVolumeLiters) {
        if (currentMoisture >= TARGET_MOISTURE) {
            return 0.0;
        }
        double deficitFraction = TARGET_MOISTURE - currentMoisture;
        double litersNeeded = deficitFraction * zoneVolumeLiters;
        return litersNeeded / litersPerMinute;
    }

    /**
     * Reports whether a zone needs watering at all.
     *
     * @param currentMoisture the zone's current soil moisture fraction
     * @return true if moisture is below the target
     */
    public boolean needsWater(double currentMoisture) {
        return currentMoisture < TARGET_MOISTURE;
    }
}
