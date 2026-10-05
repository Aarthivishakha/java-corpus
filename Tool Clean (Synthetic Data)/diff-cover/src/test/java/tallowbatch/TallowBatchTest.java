package tallowbatch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TallowBatchTest {

    @Test
    public void yieldIsRenderedOverRaw() {
        TallowBatch batch = new TallowBatch(100.0, 60.0);
        assertEquals(0.6, batch.renderYield(), 0.0001);
    }

    @Test
    public void yieldIsZeroForZeroRawWeight() {
        TallowBatch batch = new TallowBatch(0.0, 0.0);
        assertEquals(0.0, batch.renderYield(), 0.0001);
    }

    @Test
    public void flagsBatchBelowThreshold() {
        TallowBatch batch = new TallowBatch(100.0, 40.0);
        org.junit.Assert.assertTrue(batch.needsReview(0.5));
    }

    @Test
    public void doesNotFlagBatchAtOrAboveThreshold() {
        TallowBatch batch = new TallowBatch(100.0, 60.0);
        org.junit.Assert.assertFalse(batch.needsReview(0.5));
    }
}
