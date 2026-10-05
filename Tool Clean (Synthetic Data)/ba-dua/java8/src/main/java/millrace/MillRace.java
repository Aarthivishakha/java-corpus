package millrace;

/**
 * Models the flow through a mill race, converting a measured flow
 * rate into an estimated wheel speed.
 */
public final class MillRace {

    private static final double WHEEL_RADIUS_METERS = 1.5;
    private static final double EFFICIENCY = 0.72;

    /**
     * Estimates the mill wheel's rotational speed for a given flow rate.
     *
     * @param flowCubicMetersPerSecond the measured water flow rate
     * @return the estimated wheel speed in revolutions per minute
     */
    public double wheelSpeedRpm(double flowCubicMetersPerSecond) {
        double linearSpeed = flowCubicMetersPerSecond * EFFICIENCY;
        double circumference = 2 * Math.PI * WHEEL_RADIUS_METERS;
        double revolutionsPerSecond = linearSpeed / circumference;
        return revolutionsPerSecond * 60.0;
    }

    /**
     * Reports whether the given flow rate would over-speed the wheel
     * past its rated maximum.
     *
     * @param flowCubicMetersPerSecond the measured water flow rate
     * @param maxRatedRpm              the wheel's maximum rated speed
     * @return true if the estimated speed exceeds the rated maximum
     */
    public boolean exceedsRating(double flowCubicMetersPerSecond, double maxRatedRpm) {
        return wheelSpeedRpm(flowCubicMetersPerSecond) > maxRatedRpm;
    }
}
