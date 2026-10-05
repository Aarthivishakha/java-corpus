package yieldinspector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class YieldInspectorTest {

    @Test
    public void classifiesAcceptableYield() {
        YieldInspector inspector = new YieldInspector();
        assertEquals("acceptable", inspector.classify(0.6));
    }

    @Test
    public void passesAtLowThreshold() {
        YieldInspector inspector = new YieldInspector();
        assertTrue(inspector.passes(0.4));
    }
}
