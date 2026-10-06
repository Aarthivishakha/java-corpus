package com.pramora.testable.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

import com.pramora.testable.model.Customer;
import com.pramora.testable.model.LoyaltyTier;
import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;

/**
 * Covers the Java 12 surface carried by {@link OrderDigest}: Collectors.teeing,
 * String.transform, String.indent and CompactNumberFormat.
 *
 * <p>The compact-number assertions deliberately check shape rather than an exact
 * string. CompactNumberFormat draws its patterns from CLDR, and CLDR is updated with
 * every JDK release, so an exact-match assertion here would pass on the JDK it was
 * written against and fail on the next one - a version-sensitive test masquerading as
 * a behavioural one.
 */
public class OrderDigestTest {

    private final OrderDigest digest = new OrderDigest(Locale.US);

    private Order order() {
        Order o = new Order("O-12", new Customer("C-12", "Twelve", LoyaltyTier.SILVER, 9));
        o.addLine(new OrderLine("SKU-1", 2, 1000L));
        o.addLine(new OrderLine("SKU-2", 1, 7500L));
        o.addLine(new OrderLine("SKU-3", 5, 400L));
        return o;
    }

    @Test
    public void teeingCountsAndSumsInOnePass() {
        OrderDigest.LineStats stats = digest.stats(order());
        assertEquals(3L, stats.getCount());
        assertEquals(11500L, stats.getTotalCents());
    }

    @Test
    public void averageIsDerivedFromTheTeedFigures() {
        assertEquals(3833L, digest.stats(order()).averageCents());
    }

    @Test
    public void statsOfNullOrderAreZero() {
        OrderDigest.LineStats stats = digest.stats(null);
        assertEquals(0L, stats.getCount());
        assertEquals(0L, stats.getTotalCents());
        assertEquals(0L, stats.averageCents());
    }

    @Test
    public void emptyOrderAveragesZeroRatherThanDividingByZero() {
        Order empty = new Order("O-0", new Customer("C-0", "Zero", LoyaltyTier.STANDARD, 1));
        assertEquals(0L, digest.stats(empty).averageCents());
    }

    @Test
    public void compactFormIsNoLongerThanThePlainForm() {
        String compact = digest.compactAmount(1234500L);
        String plain = digest.plainAmount(1234500L);
        assertFalse(compact.isEmpty());
        assertTrue("compact '" + compact + "' should not exceed plain '" + plain + "'",
                compact.length() <= plain.length());
    }

    @Test
    public void smallAmountsStillFormat() {
        assertFalse(digest.compactAmount(500L).isEmpty());
    }

    @Test
    public void renderIndentsTheBodyButNotTheHeader() {
        String[] lines = digest.render(order()).split("\n");
        assertEquals(4, lines.length);
        assertTrue(lines[0].startsWith("order O-12"));
        for (int i = 1; i < lines.length; i++) {
            assertTrue("line " + i + " should be indented: '" + lines[i] + "'",
                    lines[i].startsWith("  "));
        }
    }

    @Test
    public void renderSanitisesTheOrderIdThroughTransform() {
        Order o = new Order("O 12<script>", new Customer("C-1", "One", LoyaltyTier.GOLD, 4));
        o.addLine(new OrderLine("SKU-1", 1, 100L));
        assertTrue(digest.render(o).startsWith("order O12script"));
    }

    @Test
    public void renderOfNullOrderIsEmpty() {
        assertEquals("", digest.render(null));
    }

    @Test
    public void oneLineRenderCollapsesTheNewlines() {
        String oneLine = digest.renderOneLine(order());
        assertFalse(oneLine.contains("\n"));
        assertTrue(oneLine.startsWith("order O-12"));
        assertEquals(oneLine, oneLine.strip());
    }

    @Test
    public void nullLocaleFallsBackRatherThanThrowing() {
        assertFalse(new OrderDigest(null).compactAmount(250000L).isEmpty());
    }
}
