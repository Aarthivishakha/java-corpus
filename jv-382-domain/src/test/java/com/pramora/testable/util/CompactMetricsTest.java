package com.pramora.testable.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Test;

/** Covers the Java 20 Float half-precision and URL.of additions. */
public class CompactMetricsTest {

    private static final float EXACT = 0.0f;

    // ---- Float.floatToFloat16 / float16ToFloat ----

    @Test
    public void exactlyRepresentableValuesRoundTripCleanly() {
        // powers of two and their halves are exact in binary16
        for (float v : new float[] {0.0f, 0.25f, 0.5f, 0.75f, 1.0f, 0.125f}) {
            assertEquals("value " + v, v, CompactMetrics.roundTrip(v), EXACT);
            assertEquals("value " + v, 0.0f, CompactMetrics.roundTripError(v), EXACT);
        }
    }

    @Test
    public void nonRepresentableValuesLoseALittlePrecision() {
        // binary16 carries about three decimal digits, so 0.1 does not survive exactly
        float error = CompactMetrics.roundTripError(0.1f);
        assertTrue("expected some loss, got " + error, error > 0.0f);
        assertTrue("expected the loss to be small, got " + error, error < 0.001f);
    }

    @Test
    public void roundTripStaysWithinHalfPrecisionTolerance() {
        for (int i = 0; i <= 100; i++) {
            float v = i / 100.0f;
            assertEquals("value " + v, v, CompactMetrics.roundTrip(v), 0.001f);
        }
    }

    @Test
    public void packedFormIsTwoBytes() {
        short packed = CompactMetrics.pack(0.5f);
        assertEquals(0.5f, CompactMetrics.unpack(packed), EXACT);
        assertEquals(Short.BYTES, 2);
    }

    @Test
    public void valuesAreClampedToTheUnitRange() {
        assertEquals(1.0f, CompactMetrics.roundTrip(4.2f), EXACT);
        assertEquals(0.0f, CompactMetrics.roundTrip(-3.0f), EXACT);
        assertEquals(0.0f, CompactMetrics.roundTripError(4.2f), EXACT);
    }

    @Test
    public void zeroPacksToTheZeroConstant() {
        assertEquals(CompactMetrics.ZERO, CompactMetrics.pack(0.0f));
        assertEquals(0.0f, CompactMetrics.unpack(CompactMetrics.ZERO), EXACT);
    }

    @Test
    public void utilisationIsUsedOverTotal() {
        assertEquals(0.5f, CompactMetrics.unpack(CompactMetrics.packUtilisation(15L, 30L)), EXACT);
        assertEquals(1.0f, CompactMetrics.unpack(CompactMetrics.packUtilisation(30L, 30L)), EXACT);
        assertEquals(0.25f, CompactMetrics.unpack(CompactMetrics.packUtilisation(10L, 40L)), EXACT);
    }

    @Test
    public void utilisationGuardsDegenerateInputs() {
        assertEquals(CompactMetrics.ZERO, CompactMetrics.packUtilisation(5L, 0L));
        assertEquals(CompactMetrics.ZERO, CompactMetrics.packUtilisation(0L, 30L));
        assertEquals(CompactMetrics.ZERO, CompactMetrics.packUtilisation(-1L, 30L));
        assertEquals(CompactMetrics.ZERO, CompactMetrics.packUtilisation(5L, -30L));
    }

    @Test
    public void overCapacityUtilisationClampsToOne() {
        assertEquals(1.0f, CompactMetrics.unpack(CompactMetrics.packUtilisation(45L, 30L)), EXACT);
    }

    // ---- URL.of ----

    @Test
    public void trackingUrlIsBuiltFromCarrierAndReference() {
        URL url = CompactMetrics.trackingUrl("GROUND", "T-1234");
        assertNotNull(url);
        assertEquals("https", url.getProtocol());
        assertEquals("tracking.example.com", url.getHost());
        assertEquals("/ground/T-1234", url.getPath());
    }

    @Test
    public void trackingLinkIsTheFullUrl() {
        assertEquals("https://tracking.example.com/ground/T-1234",
                CompactMetrics.trackingLink("GROUND", "T-1234"));
    }

    @Test
    public void inputsAreSanitisedBeforeTheUrlIsBuilt() {
        // sanitize strips the unsafe characters rather than truncating at them, so
        // "AIR<script>" becomes "AIRscript" and "T 99" becomes "T99"
        assertEquals("https://tracking.example.com/airscript/T99",
                CompactMetrics.trackingLink("AIR<script>", "T 99"));
    }

    @Test
    public void blankInputsProduceNoUrl() {
        assertNull(CompactMetrics.trackingUrl(null, "T-1"));
        assertNull(CompactMetrics.trackingUrl("GROUND", null));
        assertNull(CompactMetrics.trackingUrl("", "T-1"));
        assertNull(CompactMetrics.trackingUrl("<<<>>>", "T-1"));
        assertEquals("", CompactMetrics.trackingLink(null, null));
    }
}
