package supplyregistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SupplyRegistryTest {

    @Test
    public void registerAndQuery() {
        SupplyRegistry registry = new SupplyRegistry();
        registry.register("bolt-supplier");
        assertTrue(registry.isRegistered("bolt-supplier"));
        assertEquals(1, registry.registeredCount());
    }
}
