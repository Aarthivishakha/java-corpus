package valvestation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ValveStationTest {

    @Test
    public void closeAndOpenCount() {
        ValveStation station = new ValveStation();
        station.openValve(0);
        station.openValve(2);
        assertEquals(2, station.openCount());
        station.Close(0);
        assertEquals(1, station.openCount());
    }

    @Test
    public void withinLimitChecksRange() {
        ValveStation station = new ValveStation();
        assertTrue(station.isWithinLimit(2, 0, 5));
    }
}
