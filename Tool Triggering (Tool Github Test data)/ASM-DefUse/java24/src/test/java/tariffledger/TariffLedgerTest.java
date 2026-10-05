package tariffledger;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TariffLedgerTest {

    @Test
    public void tier1Tariff() {
        assertEquals(25.0, TariffLedger.computeTariff(500.0), 0.0001);
    }

    @Test
    public void tier2Tariff() {
        assertEquals(120.0, TariffLedger.computeTariff(4000.0), 0.0001);
    }

    @Test
    public void tier3Tariff() {
        assertEquals(100.0, TariffLedger.computeTariff(10000.0), 0.0001);
    }
}
