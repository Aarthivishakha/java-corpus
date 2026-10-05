package cratecounter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CrateCounterTest {

    @Test
    public void accumulatesArrivalsPerBay() {
        CrateCounter counter = new CrateCounter(50);
        counter.receive("A1", 10);
        counter.receive("A1", 15);
        assertEquals(25, counter.countFor("A1"));
    }

    @Test
    public void unknownBayCountsZero() {
        CrateCounter counter = new CrateCounter(50);
        assertEquals(0, counter.countFor("Z9"));
    }

    @Test
    public void flagsBayOverLimit() {
        CrateCounter counter = new CrateCounter(20);
        counter.receive("B2", 25);
        assertTrue(counter.isOverLimit("B2"));
    }

    @Test
    public void doesNotFlagBayAtLimit() {
        CrateCounter counter = new CrateCounter(20);
        counter.receive("B3", 20);
        assertFalse(counter.isOverLimit("B3"));
    }
}
