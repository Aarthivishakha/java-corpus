package com.pramora.testable.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import org.junit.Test;

/** Covers the Java 23 strict-parsing, Instant, Inet4Address and FFM additions. */
public class StrictFormatsTest {

    // ---- NumberFormat.setStrict / isStrict ----

    @Test
    public void strictAndLenientFormatsReportTheirMode() {
        assertTrue(StrictFormats.isStrict(StrictFormats.strictInteger(Locale.US)));
        assertFalse(StrictFormats.isStrict(StrictFormats.lenientInteger(Locale.US)));
        assertFalse(StrictFormats.isStrict(null));
        assertTrue(StrictFormats.strictDecimal().isStrict());
    }

    @Test
    public void wholeNumbersParseUnderBothModes() {
        assertEquals(12L, StrictFormats.parseQuantity("12", -1L));
        assertEquals(12L, StrictFormats.parseQuantityLeniently("12", -1L));
        assertEquals(1234L, StrictFormats.parseQuantity("1,234", -1L));
    }

    @Test
    public void strictModeRejectsTrailingGarbageThatLenientModeAccepts() {
        // this is the defect class the Java 23 API exists to close: the lenient default
        // stops at the first unusable character and returns what it already has
        assertEquals(12L, StrictFormats.parseQuantityLeniently("12abc", -1L));
        assertEquals(-1L, StrictFormats.parseQuantity("12abc", -1L));
    }

    @Test
    public void strictModeRejectsMisplacedGroupingSeparators() {
        assertEquals(-1L, StrictFormats.parseQuantity("1,2,3", -1L));
    }

    @Test
    public void nonNumbersFallBackUnderEitherMode() {
        assertEquals(-1L, StrictFormats.parseQuantity("abc", -1L));
        assertEquals(-1L, StrictFormats.parseQuantityLeniently("abc", -1L));
        assertEquals(-1L, StrictFormats.parseQuantity(null, -1L));
        assertEquals(-1L, StrictFormats.parseQuantity("   ", -1L));
        assertEquals(-1L, StrictFormats.parseQuantityLeniently(null, -1L));
    }

    @Test
    public void negativeQuantitiesFallBack() {
        assertEquals(-1L, StrictFormats.parseQuantity("-5", -1L));
    }

    @Test
    public void nullLocaleFallsBack() {
        assertTrue(StrictFormats.isStrict(StrictFormats.strictInteger(null)));
        assertFalse(StrictFormats.isStrict(StrictFormats.lenientInteger(null)));
    }

    // ---- Inet4Address.ofPosixLiteral ----

    @Test
    public void dottedDecimalResolves() {
        assertEquals("127.0.0.1", StrictFormats.normaliseAddress("127.0.0.1"));
        assertEquals("10.0.0.5", StrictFormats.normaliseAddress("10.0.0.5"));
    }

    @Test
    public void posixFormsAllDenoteTheSameLoopbackAddress() {
        // the reason the API exists: a blocklist on the literal "127.0.0.1" stops none of these
        assertTrue(StrictFormats.isLoopbackLiteral("127.0.0.1"));
        assertTrue(StrictFormats.isLoopbackLiteral("0177.0.0.1"));
        assertTrue(StrictFormats.isLoopbackLiteral("0x7f.0.0.1"));
        assertTrue(StrictFormats.isLoopbackLiteral("127.1"));
        assertTrue(StrictFormats.isLoopbackLiteral("2130706433"));
    }

    @Test
    public void everyLoopbackFormNormalisesToTheSameString() {
        for (String form : new String[] {"0177.0.0.1", "0x7f.0.0.1", "127.1", "2130706433"}) {
            assertEquals("form " + form, "127.0.0.1", StrictFormats.normaliseAddress(form));
        }
    }

    @Test
    public void nonLoopbackAddressesAreNotFlagged() {
        assertFalse(StrictFormats.isLoopbackLiteral("10.0.0.5"));
        assertFalse(StrictFormats.isLoopbackLiteral("0x0a.0.0.5"));
    }

    @Test
    public void invalidLiteralsResolveToNull() {
        assertNull(StrictFormats.resolvePosix(null));
        assertNull(StrictFormats.resolvePosix("   "));
        assertNull(StrictFormats.resolvePosix("not-an-address"));
        assertNull(StrictFormats.resolvePosix("999.999.999.999"));
        assertEquals("", StrictFormats.normaliseAddress("not-an-address"));
        assertFalse(StrictFormats.isLoopbackLiteral(null));
    }

    @Test
    public void surroundingWhitespaceIsTolerated() {
        assertEquals("127.0.0.1", StrictFormats.normaliseAddress("  127.0.0.1  "));
    }

    // ---- Instant.until ----

    @Test
    public void elapsedIsTheDurationBetweenInstants() {
        Instant start = Instant.parse("2026-01-15T10:00:00Z");
        Instant end = Instant.parse("2026-01-15T12:30:00Z");
        assertEquals(Duration.ofMinutes(150), StrictFormats.elapsed(start, end));
        assertEquals(150L, StrictFormats.elapsedMinutes(start, end));
    }

    @Test
    public void elapsedIsNegativeWhenTheOrderIsReversed() {
        Instant start = Instant.parse("2026-01-15T12:00:00Z");
        Instant end = Instant.parse("2026-01-15T10:00:00Z");
        assertTrue(StrictFormats.elapsed(start, end).isNegative());
        assertEquals(0L, StrictFormats.elapsedMinutes(start, end));
    }

    @Test
    public void elapsedOfNullIsZero() {
        Instant now = Instant.parse("2026-01-15T10:00:00Z");
        assertEquals(Duration.ZERO, StrictFormats.elapsed(null, now));
        assertEquals(Duration.ZERO, StrictFormats.elapsed(now, null));
        assertEquals(0L, StrictFormats.elapsedMinutes(null, null));
    }

    @Test
    public void identicalInstantsElapseZero() {
        Instant now = Instant.parse("2026-01-15T10:00:00Z");
        assertEquals(Duration.ZERO, StrictFormats.elapsed(now, now));
    }

    // ---- MemorySegment.maxByteAlignment ----

    @Test
    public void alignmentIsReportedForAnAllocatedSegment() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_LONG, 4);
            assertNotNull(segment);
            long alignment = StrictFormats.maxAlignment(segment);
            assertTrue("expected a positive power of two, got " + alignment, alignment > 0L);
            assertEquals("expected a power of two, got " + alignment,
                    0L, alignment & (alignment - 1L));
            assertTrue("a long array should be at least 8-byte aligned", alignment >= 8L);
        }
    }

    @Test
    public void alignmentOfNullIsZero() {
        assertEquals(0L, StrictFormats.maxAlignment(null));
    }
}
