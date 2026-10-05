package siloweigher;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SiloWeigherTest {

    private final SiloWeigher weigher = new SiloWeigher();

    @Test
    public void routesEmptyCode() {
        assertEquals("empty", weigher.routeCode(0));
    }

    @Test
    public void priorityForDryCoolGrain() {
        assertEquals("normal", weigher.priorityFor(10.0, 20.0, 10));
    }
}
