package strongroomaccess;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StrongroomAccessTest {

    @Test
    public void acceptsMatchingToken() {
        StrongroomAccess access = new StrongroomAccess();
        assertTrue(access.accepts("secretVaultKey99"));
    }
}
