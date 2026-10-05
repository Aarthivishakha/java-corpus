package coalsorter;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class CoalSorterTest {

    @Test
    public void sortsIntoTheMatchingBin() {
        CoalSorter sorter = new CoalSorter(Arrays.asList("low", "mid", "high"));
        assertEquals("low", sorter.sort(500));
        assertEquals("mid", sorter.sort(1500));
        assertEquals("high", sorter.sort(2500));
    }

    @Test
    public void clampsValuesAboveTheTopBin() {
        CoalSorter sorter = new CoalSorter(Arrays.asList("low", "mid", "high"));
        assertEquals("high", sorter.sort(9000));
    }

    @Test
    public void tracksTalliesPerBin() {
        CoalSorter sorter = new CoalSorter(Arrays.asList("low", "mid", "high"));
        sorter.sort(500);
        sorter.sort(600);
        sorter.sort(1500);
        assertEquals(2, sorter.tallyFor("low"));
        assertEquals(1, sorter.tallyFor("mid"));
        assertEquals(0, sorter.tallyFor("unknown"));
    }
}
