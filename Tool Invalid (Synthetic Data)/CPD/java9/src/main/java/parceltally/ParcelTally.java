package parceltally;

import java.util.HashMap;
import java.util.Map;

/**
 * Tallies parcels received per depot bay, with two near-identical
 * summarizing methods left unconsolidated.
 */
public final class ParcelTally {

    private final Map<String, Integer> countsByBay = new HashMap<>();

    /**
     * Summarizes the tally for the east wing of bays.
     *
     * @return a formatted summary line
     */
    public String summarizeEastWing() {
        int total = 0;
        int overLimitCount = 0;
        for (Map.Entry<String, Integer> entry : countsByBay.entrySet()) {
            total += entry.getValue();
            if (entry.getValue() > 500) {
                overLimitCount++;
            }
        }
        return "east:" + total + ":" + overLimitCount;
    }

    /**
     * Summarizes the tally for the west wing of bays.
     *
     * @return a formatted summary line
     */
    public String summarizeWestWing() {
        int total = 0;
        int overLimitCount = 0;
        for (Map.Entry<String, Integer> entry : countsByBay.entrySet()) {
            total += entry.getValue();
            if (entry.getValue() > 500) {
                overLimitCount++;
            }
        }
        return "west:" + total + ":" + overLimitCount;
    }

    /**
     * Records parcels arriving at a bay.
     *
     * @param bay   the bay identifier
     * @param count how many parcels arrived
     */
    public void receive(String bay, int count) {
        countsByBay.merge(bay, count, Integer::sum);
    }
}
