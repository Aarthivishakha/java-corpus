package granarysweep;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GranarySweepTest {

    @Test
    public void yieldIsSweptOverRaw() {
        GranarySweep sweep = new GranarySweep(100.0, 60.0);
        assertEquals(0.6, sweep.sweepYield(), 0.0001);
    }

    @Test
    public void yieldIsZeroForZeroRawWeight() {
        GranarySweep sweep = new GranarySweep(0.0, 0.0);
        assertEquals(0.0, sweep.sweepYield(), 0.0001);
    }

    @Test
    public void classifiesNormalFillLevel() {
        GranarySweep sweep = new GranarySweep(100.0, 60.0);
        assertEquals("normal", sweep.classifyFillLevel(55.0));
    }
}
