package beaconwatch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BeaconWatchTest {

    @Test
    public void withinBandIsSafe() {
        BeaconWatch watch = new BeaconWatch();
        assertTrue(watch.isWithinBand(50.0));
    }

    @Test
    public void belowBandIsNotSafe() {
        BeaconWatch watch = new BeaconWatch();
        assertFalse(watch.isWithinBand(5.0));
    }
}
