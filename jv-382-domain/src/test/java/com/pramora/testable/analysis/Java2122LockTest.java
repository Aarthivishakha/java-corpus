package com.pramora.testable.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;

import org.junit.Test;

import com.pramora.testable.model.RouteEvent;
import com.pramora.testable.model.ShipmentLeg;
import com.pramora.testable.util.NativeMetrics;

/** Covers the Java 21 and Java 22 locks carried by this family. */
public class Java2122LockTest {

    private ShipmentLeg leg(String sku, int units) {
        return new ShipmentLeg(sku, units, "GROUND");
    }

    // ---- Java 21: pattern switch, record patterns, guards ----

    @Test
    public void narrateCoversEveryArmOfTheSealedHierarchy() {
        assertEquals("no event", EventNarrator.narrate(null));
        assertEquals("SKU-1 planned x3",
                EventNarrator.narrate(new RouteEvent.Planned(leg("SKU-1", 3))));
        assertEquals("SKU-1 dispatched as T-9",
                EventNarrator.narrate(new RouteEvent.Dispatched(leg("SKU-1", 3), "T-9")));
        assertEquals("SKU-1 dispatched, no tracking",
                EventNarrator.narrate(new RouteEvent.Dispatched(leg("SKU-1", 3), null)));
        assertEquals("SKU-1 delivered in 90 min",
                EventNarrator.narrate(new RouteEvent.Delivered(leg("SKU-1", 3), 90L)));
        assertEquals("SKU-1 failed: no capacity",
                EventNarrator.narrate(new RouteEvent.Failed(leg("SKU-1", 3), "no capacity")));
    }

    @Test
    public void theGuardSplitsLateDeliveriesFromOnTimeOnes() {
        assertEquals("SKU-1 delivered late (300 min)",
                EventNarrator.narrate(new RouteEvent.Delivered(leg("SKU-1", 1), 300L)));
        assertEquals("SKU-1 delivered in 240 min",
                EventNarrator.narrate(new RouteEvent.Delivered(leg("SKU-1", 1), 240L)));
    }

    @Test
    public void priorityOrdersTheReviewQueue() {
        assertEquals(0, EventNarrator.priority(null));
        assertEquals(100, EventNarrator.priority(new RouteEvent.Failed(leg("A", 1), "x")));
        assertEquals(60, EventNarrator.priority(new RouteEvent.Dispatched(leg("A", 1), "")));
        assertEquals(40, EventNarrator.priority(new RouteEvent.Delivered(leg("A", 1), 300L)));
        assertEquals(20, EventNarrator.priority(new RouteEvent.Dispatched(leg("A", 1), "T-1")));
        assertEquals(10, EventNarrator.priority(new RouteEvent.Delivered(leg("A", 1), 10L)));
        assertEquals(5, EventNarrator.priority(new RouteEvent.Planned(leg("A", 1))));
    }

    @Test
    public void nestedRecordDeconstructionReachesTheLegComponents() {
        assertEquals(7, EventNarrator.unitsInFlight(new RouteEvent.Planned(leg("A", 7))));
        assertEquals(4, EventNarrator.unitsInFlight(new RouteEvent.Dispatched(leg("A", 4), "T")));
        assertEquals(0, EventNarrator.unitsInFlight(new RouteEvent.Delivered(leg("A", 4), 1L)));
        assertEquals(0, EventNarrator.unitsInFlight(null));
    }

    // ---- Java 21: sequenced collections ----

    private RouteLog log() {
        return new RouteLog()
                .record(new RouteEvent.Planned(leg("SKU-1", 2)))
                .record(new RouteEvent.Dispatched(leg("SKU-2", 25), "T-1"))
                .record(new RouteEvent.Delivered(leg("SKU-1", 2), 60L));
    }

    @Test
    public void firstAndLastAreReadWithoutIndexing() {
        RouteLog l = log();
        assertEquals("PLANNED", l.first().kind());
        assertEquals("DELIVERED", l.latest().kind());
        assertEquals(3, l.size());
        assertFalse(l.isEmpty());
    }

    @Test
    public void emptyLogHasNoEnds() {
        RouteLog empty = new RouteLog();
        assertNull(empty.first());
        assertNull(empty.latest());
        assertNull(empty.dropOldest());
        assertNull(empty.dropNewest());
        assertTrue(empty.isEmpty());
    }

    @Test
    public void reversedIsAViewNotACopy() {
        RouteLog l = log();
        SequencedCollection<RouteEvent> view = l.mostRecentFirst();
        assertEquals(3, view.size());
        assertEquals("DELIVERED", view.getFirst().kind());
        // the view sees a write made after it was taken - this is what makes it a view
        l.record(new RouteEvent.Failed(leg("SKU-3", 1), "late"));
        assertEquals(4, view.size());
        assertEquals("FAILED", view.getFirst().kind());
    }

    @Test
    public void endsCanBeAddedAndRemoved() {
        RouteLog l = log();
        assertEquals("PLANNED", l.dropOldest().kind());
        assertEquals("DELIVERED", l.dropNewest().kind());
        assertEquals(1, l.size());
        l.recordFirst(new RouteEvent.Planned(leg("SKU-9", 1)));
        assertEquals("PLANNED", l.first().kind());
        assertEquals(2, l.size());
        l.recordFirst(null);
        assertEquals(2, l.size());
    }

    @Test
    public void nullEventsAreIgnored() {
        assertEquals(0, new RouteLog().record(null).size());
    }

    @Test
    public void sequencedSetKeepsFirstSeenOrder() {
        SequencedSet<String> skus = log().skusSeen();
        assertEquals(2, skus.size());
        assertEquals("SKU-1", skus.getFirst());
        assertEquals("SKU-2", skus.getLast());
        assertEquals("SKU-1", log().firstSku());
        assertEquals("", new RouteLog().firstSku());
    }

    @Test
    public void sequencedMapCountsKindsInOrder() {
        SequencedMap<String, Integer> counts = log().kindCounts();
        assertEquals(3, counts.size());
        assertEquals("PLANNED", counts.firstEntry().getKey());
        assertEquals("DELIVERED", counts.lastEntry().getKey());
        assertEquals("PLANNED", log().firstKind().getKey());
        assertNull(new RouteLog().firstKind());
    }

    // ---- Java 22: unnamed variables and patterns ----

    @Test
    public void countUsesAnUnnamedLoopVariable() {
        assertEquals(3, LegAuditor.count(List.of(leg("A", 1), leg("B", 1), leg("C", 1))));
        assertEquals(0, LegAuditor.count(List.of()));
        assertEquals(0, LegAuditor.count(null));
    }

    @Test
    public void parseUnitsSwallowsTheExceptionWithAnUnnamedCatch() {
        assertEquals(7, LegAuditor.parseUnits(" 7 ", 1));
        assertEquals(1, LegAuditor.parseUnits("seven", 1));
        assertEquals(1, LegAuditor.parseUnits("0", 1));
        assertEquals(1, LegAuditor.parseUnits("-3", 1));
        assertEquals(1, LegAuditor.parseUnits(null, 1));
    }

    @Test
    public void unnamedRecordComponentsBindOnlyWhatIsNeeded() {
        assertEquals("SKU-1", LegAuditor.skuOf(new RouteEvent.Planned(leg("SKU-1", 1))));
        assertEquals("SKU-2", LegAuditor.skuOf(new RouteEvent.Failed(leg("SKU-2", 1), "x")));
        assertEquals("SKU-3", LegAuditor.skuOf(new RouteEvent.Delivered(leg("SKU-3", 1), 5L)));
        assertEquals("SKU-4", LegAuditor.skuOf(new RouteEvent.Dispatched(leg("SKU-4", 1), "T")));
        assertEquals("", LegAuditor.skuOf(null));
    }

    @Test
    public void carrierIsBoundFromTheThirdComponentAlone() {
        assertEquals("AIR", LegAuditor.carrierOf(
                new RouteEvent.Planned(new ShipmentLeg("SKU-1", 1, "AIR"))));
        assertEquals("", LegAuditor.carrierOf(null));
    }

    @Test
    public void totalUnitsSumsAcrossEveryEventKind() {
        assertEquals(11, LegAuditor.totalUnits(List.of(
                new RouteEvent.Planned(leg("A", 2)),
                new RouteEvent.Dispatched(leg("B", 4), "T"),
                new RouteEvent.Delivered(leg("C", 5), 1L))));
        assertEquals(0, LegAuditor.totalUnits(List.of()));
        assertEquals(0, LegAuditor.totalUnits(null));
    }

    @Test
    public void bulkSkusFiltersOnTheLeg() {
        assertEquals(List.of("B"), LegAuditor.bulkSkus(List.of(
                new RouteEvent.Planned(leg("A", 2)),
                new RouteEvent.Dispatched(leg("B", 25), "T"))));
        assertTrue(LegAuditor.bulkSkus(null).isEmpty());
    }

    // ---- Java 22: Foreign Function and Memory ----

    @Test
    public void countersLiveOffHeapAndRoundTrip() {
        try (NativeMetrics m = new NativeMetrics(4)) {
            assertEquals(4, m.capacity());
            assertEquals(32L, m.byteSize());
            m.set(0, 10L);
            m.set(3, 5L);
            assertEquals(10L, m.get(0));
            assertEquals(5L, m.get(3));
            assertEquals(0L, m.get(1));
            assertEquals(15L, m.total());
        }
    }

    @Test
    public void addAccumulates() {
        try (NativeMetrics m = new NativeMetrics(2)) {
            assertEquals(3L, m.add(0, 3L));
            assertEquals(8L, m.add(0, 5L));
            assertEquals(8L, m.total());
        }
    }

    @Test
    public void resetAndToArray() {
        try (NativeMetrics m = new NativeMetrics(3)) {
            m.set(0, 1L);
            m.set(1, 2L);
            m.set(2, 3L);
            assertArrayEquals(new long[] {1L, 2L, 3L}, m.toArray());
            m.reset();
            assertEquals(0L, m.total());
        }
    }

    private static void assertArrayEquals(long[] expected, long[] actual) {
        org.junit.Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void boundsAreChecked() {
        try (NativeMetrics m = new NativeMetrics(2)) {
            try {
                m.get(2);
                fail("expected an out-of-bounds read to be rejected");
            } catch (IndexOutOfBoundsException expected) {
                assertTrue(expected.getMessage().contains("outside 0..1"));
            }
            try {
                m.set(-1, 0L);
                fail("expected an out-of-bounds write to be rejected");
            } catch (IndexOutOfBoundsException expected) {
                assertTrue(expected.getMessage().contains("outside"));
            }
        }
    }

    @Test
    public void capacityMustBePositive() {
        try {
            new NativeMetrics(0);
            fail("expected a zero capacity to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("capacity must be positive", expected.getMessage());
        }
    }

    @Test
    public void closingReleasesTheArenaDeterministically() {
        NativeMetrics m = new NativeMetrics(2);
        m.set(0, 1L);
        assertFalse(m.isClosed());
        m.close();
        assertTrue(m.isClosed());
        // the segment's scope is dead, so any further access is an error rather than
        // undefined behaviour - the guarantee a direct ByteBuffer cannot give
        try {
            m.get(0);
            fail("expected access after close to be rejected");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().toLowerCase().contains("closed"));
        }
    }
}
