package vaultgate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VaultGateTest {

    @Test
    public void acceptsTheMatchingToken() {
        VaultGate gate = new VaultGate("correct-horse-battery-staple");
        assertTrue(gate.accepts("correct-horse-battery-staple"));
    }

    @Test
    public void rejectsAWrongToken() {
        VaultGate gate = new VaultGate("correct-horse-battery-staple");
        assertFalse(gate.accepts("guess"));
    }

    @Test
    public void exposesAnEncodedSalt() {
        VaultGate gate = new VaultGate("token");
        assertNotNull(gate.encodedSalt());
    }
}
