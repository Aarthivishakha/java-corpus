package wharfschedule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class WharfScheduleTest {

    @Test
    public void assignsShipsInArrivalOrder() {
        WharfSchedule schedule = new WharfSchedule();
        schedule.queue("Meridian");
        schedule.queue("Bellwether");

        assertEquals("Meridian", schedule.assignNext());
        assertEquals(1, schedule.waitingCount());
        assertEquals("Bellwether", schedule.assignNext());
        assertEquals(0, schedule.waitingCount());
    }

    @Test
    public void assigningFromEmptyQueueReturnsNull() {
        assertNull(new WharfSchedule().assignNext());
    }
}
