package coolantcycle;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class CoolantCycleTest {

    @Test
    public void freshLoopNotDueForMaintenance() {
        CoolantCycle cycle = new CoolantCycle();
        cycle.recordCycle();
        assertFalse(cycle.isDueForMaintenance());
    }
}
