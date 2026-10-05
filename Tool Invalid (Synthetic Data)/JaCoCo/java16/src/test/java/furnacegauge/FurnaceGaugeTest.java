package furnacegauge;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FurnaceGaugeTest {

    private final FurnaceGauge gauge = new FurnaceGauge();

    @Test
    public void idleBelowLowBand() {
        assertEquals("idle", gauge.stageFor(100));
    }
}
