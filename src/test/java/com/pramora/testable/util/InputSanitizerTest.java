package com.pramora.testable.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InputSanitizerTest {

    @Test
    public void nullBecomesEmpty() {
        assertEquals("", InputSanitizer.sanitize(null));
    }

    @Test
    public void punctuationIsStripped() {
        assertEquals("ordr123", InputSanitizer.sanitize("or'dr;123"));
    }

    @Test
    public void safeValueRoundTrips() {
        assertTrue(InputSanitizer.isSafe("order-123_A"));
    }

    @Test
    public void unsafeValueIsRejected() {
        assertFalse(InputSanitizer.isSafe("DROP TABLE orders"));
    }

    @Test
    public void blankInputBecomesEmpty() {
        assertEquals("", InputSanitizer.sanitize("   "));
    }

    @Test
    public void maskUsesStringRepeat() {
        assertEquals("o**********", InputSanitizer.mask("order-12345"));
        assertEquals("", InputSanitizer.mask("   "));
    }
}
