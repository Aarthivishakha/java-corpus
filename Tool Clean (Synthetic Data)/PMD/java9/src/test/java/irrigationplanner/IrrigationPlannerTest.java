package irrigationplanner;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IrrigationPlannerTest {

    private final IrrigationPlanner planner = new IrrigationPlanner();

    @Test
    public void wateringDurationTracksMoistureDeficit() {
        double belowTarget = planner.minutesToWater(0.45, 10.0, 200.0);
        double atTarget = planner.minutesToWater(0.70, 10.0, 200.0);
        assertEquals(4.0, belowTarget, 0.0001);
        assertEquals(0.0, atTarget, 0.0001);
    }

    @Test
    public void needsWaterReflectsTheSameThreshold() {
        boolean below = planner.needsWater(0.5);
        boolean atTarget = planner.needsWater(0.65);
        if (!below || atTarget) {
            throw new AssertionError("threshold check failed: below=" + below + " atTarget=" + atTarget);
        }
    }
}
