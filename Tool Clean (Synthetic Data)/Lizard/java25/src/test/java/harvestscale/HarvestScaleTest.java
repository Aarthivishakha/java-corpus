package harvestscale;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HarvestScaleTest {

    private final HarvestScale scale = new HarvestScale();

    @Test
    public void weightGrades() {
        assertEquals("light", scale.gradeByWeight(5.0));
        assertEquals("standard", scale.gradeByWeight(15.0));
        assertEquals("heavy", scale.gradeByWeight(25.0));
    }

    @Test
    public void blemishGrades() {
        assertEquals("premium", scale.gradeByBlemishes(0));
        assertEquals("standard", scale.gradeByBlemishes(2));
        assertEquals("seconds", scale.gradeByBlemishes(5));
    }

    @Test
    public void combinedLabelPrefersSeconds() {
        assertEquals("seconds", scale.combinedLabel("light", "seconds"));
    }

    @Test
    public void combinedLabelHandlesHeavy() {
        assertEquals("heavy-standard", scale.combinedLabel("heavy", "standard"));
    }

    @Test
    public void combinedLabelDefault() {
        assertEquals("light-premium", scale.combinedLabel("light", "premium"));
    }

    @Test
    public void averageWeightOfEmptyBatchIsZero() {
        assertEquals(0.0, scale.averageWeight(new double[0]), 0.0001);
    }

    @Test
    public void averageWeightOfBatch() {
        assertEquals(10.0, scale.averageWeight(new double[]{5.0, 10.0, 15.0}), 0.0001);
    }
}
