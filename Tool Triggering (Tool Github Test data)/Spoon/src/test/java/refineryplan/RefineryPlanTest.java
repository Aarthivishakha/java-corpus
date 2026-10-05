package refineryplan;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RefineryPlanTest {

    @Test
    public void hoursForComputesFromThroughput() {
        RefineryPlan plan = new RefineryPlan(50.0);
        assertEquals(4.0, plan.hoursFor(200.0), 0.0001);
    }
}
