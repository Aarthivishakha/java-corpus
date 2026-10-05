package conveyoraudit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConveyorAuditTest {

    @Test
    public void flagsOverSpeedSegment() {
        ConveyorAudit audit = new ConveyorAudit(2.0);
        audit.record("belt-1", 3.5);
        assertTrue(audit.isOverSpeed("belt-1"));
    }

    @Test
    public void doesNotFlagNormalSegment() {
        ConveyorAudit audit = new ConveyorAudit(2.0);
        audit.record("belt-2", 1.0);
        assertFalse(audit.isOverSpeed("belt-2"));
    }
}
