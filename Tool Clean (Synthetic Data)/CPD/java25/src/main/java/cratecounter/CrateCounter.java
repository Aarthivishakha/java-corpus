package cratecounter;

import java.util.HashMap;
import java.util.Map;

/**
 * Counts crates received per warehouse bay and flags bays that are
 * over their storage limit.
 */
public final class CrateCounter {

    private final Map<String, Integer> countsByBay = new HashMap<>();
    private final int limitPerBay;

    /**
     * Creates a counter with the given per-bay storage limit.
     *
     * @param limitPerBay the maximum crates a single bay may hold
     */
    public CrateCounter(int limitPerBay) {
        this.limitPerBay = limitPerBay;
    }

    /**
     * Records crates arriving at a bay.
     *
     * @param bay   the bay identifier
     * @param count how many crates arrived
     */
    public void receive(String bay, int count) {
        countsByBay.merge(bay, count, Integer::sum);
    }

    /**
     * Reports the current crate count for a bay.
     *
     * @param bay the bay identifier
     * @return the crate count, zero if the bay has never received any
     */
    public int countFor(String bay) {
        return countsByBay.getOrDefault(bay, 0);
    }

    /**
     * Reports whether a bay is currently over its storage limit.
     *
     * @param bay the bay identifier
     * @return true if the bay's count exceeds the configured limit
     */
    public boolean isOverLimit(String bay) {
        return countFor(bay) > limitPerBay;
    }
}
