package manifestaudit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ManifestAuditTest {

    @Test
    public void declareAndQuery() {
        ManifestAudit audit = new ManifestAudit();
        audit.declare("libfoo", "1.2.3");
        assertEquals("1.2.3", audit.versionOf("libfoo"));
        assertEquals(1, audit.packageCount());
    }
}
