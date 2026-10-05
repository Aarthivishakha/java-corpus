package trailrouter;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class TrailRouterTest {

    private final TrailRouter router = new TrailRouter();

    @Test
    public void picksTheShorterTrail() {
        assertEquals("ridge", router.shorterOf("ridge", 4.5, "valley", 6.2));
        assertEquals("valley", router.shorterOf("ridge", 8.0, "valley", 6.2));
    }

    @Test
    public void sumsSegmentLengths() {
        assertEquals(12.0, router.totalLength(Arrays.asList(3.0, 4.0, 5.0)), 0.0001);
    }
}
