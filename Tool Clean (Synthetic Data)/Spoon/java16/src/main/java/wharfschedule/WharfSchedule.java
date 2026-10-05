package wharfschedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Schedules ship loading slots at a wharf, one berth at a time, in
 * arrival order.
 */
public final class WharfSchedule {

    private final List<String> berthQueue = new ArrayList<>();

    /**
     * Queues a ship for the next available loading slot.
     *
     * @param shipName the arriving ship's name
     */
    public void queue(String shipName) {
        berthQueue.add(shipName);
    }

    /**
     * Assigns the next ship in the queue to a berth and removes it
     * from the queue.
     *
     * @return the ship assigned, or null if the queue is empty
     */
    public String assignNext() {
        if (berthQueue.isEmpty()) {
            return null;
        }
        return berthQueue.remove(0);
    }

    /**
     * Reports how many ships are still waiting.
     *
     * @return the number of queued ships
     */
    public int waitingCount() {
        return berthQueue.size();
    }
}
