package conveyoraudit;

import java.util.HashMap;
import java.util.Map;

/**
 * Audits conveyor belt segment speeds against a configured safe range.
 */
public final class ConveyorAudit {

    private final Map<String, Double> speedsBySegment = new HashMap<>();
    private final double maxSafeSpeed;

    /**
     * Creates an audit with the given maximum safe speed.
     *
     * @param maxSafeSpeed the maximum safe belt speed, meters per second
     */
    public ConveyorAudit(double maxSafeSpeed) {
        this.maxSafeSpeed = maxSafeSpeed;
    }

    /**
     * Records a segment's measured speed.
     *
     * @param segment the segment identifier
     * @param speed   the measured speed, meters per second
     */
    public void record(String segment, double speed) {
        speedsBySegment.put(segment, speed);
    }

    /**
     * Reports whether a segment is running over the configured safe speed.
     *
     * @param segment the segment identifier
     * @return true if the recorded speed exceeds the safe maximum
     */
    public boolean isOverSpeed(String segment) {
        Double speed = speedsBySegment.get(segment);
        return speed != null && speed > maxSafeSpeed;
    }
}
