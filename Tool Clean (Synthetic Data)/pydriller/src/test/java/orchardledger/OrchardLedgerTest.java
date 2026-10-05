package orchardledger;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OrchardLedgerTest {

    @Test
    public void totalsByVariety() {
        OrchardLedger ledger = new OrchardLedger();
        ledger.record("gala", 40.0);
        ledger.record("fuji", 25.0);
        ledger.record("gala", 15.0);

        assertEquals(55.0, ledger.totalFor("gala"), 0.0001);
        assertEquals(25.0, ledger.totalFor("fuji"), 0.0001);
        assertEquals(3, ledger.entryCount());
    }

    @Test
    public void unknownVarietyTotalsZero() {
        OrchardLedger ledger = new OrchardLedger();
        ledger.record("gala", 10.0);
        assertEquals(0.0, ledger.totalFor("plum"), 0.0001);
    }

    @Test
    public void newLedgerIsEmpty() {
        assertEquals(true, new OrchardLedger().isEmpty());
    }

    @Test
    public void ledgerWithEntriesIsNotEmpty() {
        OrchardLedger ledger = new OrchardLedger();
        ledger.record("gala", 1.0);
        assertEquals(false, ledger.isEmpty());
    }
}
