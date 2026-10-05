package coolantcycle;

/**
 * Tracks a coolant loop's cycle count and flags loops due for
 * maintenance.
 */
public final class CoolantCycle {

    private static final int MAINTENANCE_INTERVAL = 500;

    private int cycleCount;

    /**
     * Records one coolant cycle completing.
     */
    public void recordCycle() {
        cycleCount++;
    }

    /**
     * Reports whether the loop is due for maintenance.
     *
     * @return true if the cycle count has reached the maintenance interval
     */
    public boolean isDueForMaintenance() {
        return cycleCount >= MAINTENANCE_INTERVAL && cycleCount % MAINTENANCE_INTERVAL == 0;
    }

    /**
     * Reports the current cycle count.
     *
     * @return the cycle count
     */
    public int cycleCount() {
        return cycleCount;
    }
}
