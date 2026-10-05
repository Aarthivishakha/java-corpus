package kilnmonitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class KilnMonitorTest {

    private final KilnMonitor monitor = new KilnMonitor();

    @Test
    public void warmupBelowLowBand() {
        assertEquals("warmup", monitor.stageFor(100));
    }

    @Test
    public void bisqueBetweenLowAndBisqueBand() {
        assertEquals("bisque", monitor.stageFor(500));
    }

    @Test
    public void glazeBetweenBisqueAndGlazeBand() {
        assertEquals("glaze", monitor.stageFor(1100));
    }

    @Test
    public void peakAtOrAboveGlazeBand() {
        assertEquals("peak", monitor.stageFor(1320));
    }

    @Test(expected = IllegalStateException.class)
    public void overTemperatureThrows() {
        monitor.stageFor(1351);
    }

    @Test
    public void safeAtMaximum() {
        assertTrue(monitor.isSafe(1350));
    }

    @Test
    public void unsafeAboveMaximum() {
        assertFalse(monitor.isSafe(1351));
    }

    @Test
    public void headroomComputesRemainingDegrees() {
        assertEquals(50, monitor.headroom(1300));
        assertEquals(-1, monitor.headroom(1351));
    }
}
