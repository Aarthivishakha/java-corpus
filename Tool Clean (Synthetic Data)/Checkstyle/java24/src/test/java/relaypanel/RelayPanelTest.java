package relaypanel;

import static org.junit.Assert.*;

import org.junit.Test;

public class RelayPanelTest {

    @Test
    public void startsWithAllRelaysOpen() {
        RelayPanel panel = new RelayPanel();
        assertEquals(0, panel.closedCount());
    }

    @Test
    public void closeMarksRelayClosed() {
        RelayPanel panel = new RelayPanel();
        panel.close(3);
        assertTrue(panel.isClosed(3));
        assertEquals(1, panel.closedCount());
    }

    @Test
    public void openMarksRelayOpenAgain() {
        RelayPanel panel = new RelayPanel();
        panel.close(2);
        panel.open(2);
        assertFalse(panel.isClosed(2));
        assertEquals(0, panel.closedCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeIndex() {
        new RelayPanel().close(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOutOfRangeIndex() {
        new RelayPanel().close(8);
    }
}
