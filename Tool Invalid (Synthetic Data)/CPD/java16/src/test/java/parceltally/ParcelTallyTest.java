package parceltally;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParcelTallyTest {

    @Test
    public void summarizesEastWing() {
        ParcelTally tally = new ParcelTally();
        tally.receive("E1", 100);
        tally.receive("E2", 600);
        assertEquals("east:700:1", tally.summarizeEastWing());
    }
}
