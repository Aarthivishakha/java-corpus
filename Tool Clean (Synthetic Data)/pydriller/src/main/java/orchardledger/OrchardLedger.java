package orchardledger;

import java.util.ArrayList;
import java.util.List;

/**
 * Records harvest entries for an orchard block and totals them by
 * variety.
 */
public final class OrchardLedger {

    private final List<Entry> entries = new ArrayList<>();

    /**
     * Records a harvest entry.
     *
     * @param variety   the fruit variety
     * @param kilograms the harvested weight in kilograms
     */
    public void record(String variety, double kilograms) {
        entries.add(new Entry(variety, kilograms));
    }

    /**
     * Totals the harvested weight for a single variety.
     *
     * @param variety the fruit variety
     * @return the total kilograms recorded for that variety
     */
    public double totalFor(String variety) {
        double total = 0.0;
        for (Entry e : entries) {
            if (e.variety.equals(variety)) {
                total += e.kilograms;
            }
        }
        return total;
    }

    /**
     * Counts how many entries have been recorded in total.
     *
     * @return the entry count
     */
    public int entryCount() {
        return entries.size();
    }

    /**
     * Reports whether no entries have been recorded yet.
     *
     * @return true if the ledger is empty
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    private static final class Entry {
        private final String variety;
        private final double kilograms;

        Entry(String variety, double kilograms) {
            this.variety = variety;
            this.kilograms = kilograms;
        }
    }
}
