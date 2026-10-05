package feeledger;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FeeLedgerTest {

    @Test
    public void tier1Fee() {
        assertEquals(25.0, FeeLedger.computeFee(500.0), 0.0001);
    }

    @Test
    public void tier2Fee() {
        assertEquals(120.0, FeeLedger.computeFee(4000.0), 0.0001);
    }

    @Test
    public void tier3Fee() {
        assertEquals(100.0, FeeLedger.computeFee(10000.0), 0.0001);
    }
}
