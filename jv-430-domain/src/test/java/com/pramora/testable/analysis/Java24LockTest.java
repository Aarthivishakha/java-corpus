package com.pramora.testable.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.pramora.testable.model.ShipmentLeg;
import com.pramora.testable.util.ClassFileProbe;

/** Covers the Java 24 stream gatherers, Class-File API and Reader.of additions. */
public class Java24LockTest {

    private ShipmentLeg leg(String sku, int units, String carrier) {
        return new ShipmentLeg(sku, units, carrier);
    }

    private List<ShipmentLeg> legs() {
        return List.of(leg("A", 1, "GROUND"), leg("B", 2, "GROUND"),
                leg("C", 3, "AIR"), leg("D", 4, "AIR"), leg("E", 5, "SEA"));
    }

    // ---- Gatherers.windowFixed ----

    @Test
    public void fixedBatchesSplitEvenlyWithAShortTail() {
        List<List<ShipmentLeg>> batches = LegGatherer.batches(legs(), 2);
        assertEquals(3, batches.size());
        assertEquals(2, batches.get(0).size());
        assertEquals(2, batches.get(1).size());
        assertEquals(1, batches.get(2).size());
        assertEquals("E", batches.get(2).get(0).sku());
    }

    @Test
    public void aBatchSizeAboveTheInputGivesOneBatch() {
        assertEquals(1, LegGatherer.batches(legs(), 99).size());
    }

    @Test
    public void degenerateBatchInputsGiveNothing() {
        assertTrue(LegGatherer.batches(null, 2).isEmpty());
        assertTrue(LegGatherer.batches(List.of(), 2).isEmpty());
        assertTrue(LegGatherer.batches(legs(), 0).isEmpty());
        assertTrue(LegGatherer.batches(legs(), -1).isEmpty());
    }

    // ---- Gatherers.windowSliding ----

    @Test
    public void slidingPairsOverlapByOne() {
        List<List<ShipmentLeg>> pairs = LegGatherer.pairs(legs());
        assertEquals(4, pairs.size());
        assertEquals("A", pairs.get(0).get(0).sku());
        assertEquals("B", pairs.get(0).get(1).sku());
        assertEquals("B", pairs.get(1).get(0).sku());
    }

    @Test
    public void fewerThanTwoLegsHaveNoPairs() {
        assertTrue(LegGatherer.pairs(List.of(leg("A", 1, "GROUND"))).isEmpty());
        assertTrue(LegGatherer.pairs(null).isEmpty());
    }

    @Test
    public void carrierHandoffsAreTheBoundariesBetweenCarriers() {
        assertEquals(List.of("GROUND->AIR", "AIR->SEA"), LegGatherer.carrierHandoffs(legs()));
    }

    // ---- Gatherers.scan ----

    @Test
    public void runningUnitsAccumulateOnePerLeg() {
        assertEquals(List.of(1, 3, 6, 10, 15), LegGatherer.runningUnits(legs()));
    }

    @Test
    public void runningUnitsOfNothingIsEmpty() {
        assertTrue(LegGatherer.runningUnits(null).isEmpty());
        assertTrue(LegGatherer.runningUnits(List.of()).isEmpty());
    }

    // ---- Gatherers.fold ----

    @Test
    public void foldGivesTheSingleTotal() {
        assertEquals(15, LegGatherer.totalUnits(legs()));
        assertEquals(0, LegGatherer.totalUnits(null));
        assertEquals(0, LegGatherer.totalUnits(List.of()));
    }

    @Test
    public void foldAndScanAgreeOnTheFinalValue() {
        List<Integer> running = LegGatherer.runningUnits(legs());
        assertEquals(Integer.valueOf(LegGatherer.totalUnits(legs())),
                running.get(running.size() - 1));
    }

    @Test
    public void firstFullBatchStopsAtTheThreshold() {
        assertEquals(List.of("C", "D"), LegGatherer.firstFullBatch(legs(), 2, 7)
                .stream().map(ShipmentLeg::sku).toList());
        assertTrue(LegGatherer.firstFullBatch(legs(), 2, 999).isEmpty());
    }

    @Test
    public void windowSkusWorksOnAnArbitraryStream() {
        assertEquals(List.of(List.of("A", "B"), List.of("C", "D"), List.of("E")),
                LegGatherer.windowSkus(legs().stream(), 2));
        assertTrue(LegGatherer.windowSkus(null, 2).isEmpty());
        assertTrue(LegGatherer.windowSkus(legs().stream(), 0).isEmpty());
    }

    // ---- Class-File API ----

    @Test
    public void aClassFileReportsItsOwnMajorVersion() throws Exception {
        byte[] bytes = ClassFileProbe.class.getResourceAsStream(
                "/com/pramora/testable/util/ClassFileProbe.class").readAllBytes();
        int major = ClassFileProbe.majorVersionOf(bytes);
        assertEquals("this family compiles at --release 24, so major version 68", 68, major);
    }

    @Test
    public void aClassFileReportsItsNameSuperclassAndMembers() throws Exception {
        byte[] bytes = ShipmentLeg.class.getResourceAsStream(
                "/com/pramora/testable/model/ShipmentLeg.class").readAllBytes();
        assertEquals("com/pramora/testable/model/ShipmentLeg", ClassFileProbe.nameOf(bytes));
        assertEquals("Ljava/lang/Record;", ClassFileProbe.superclassOf(bytes));
        // 5 = the 3 record components plus the 2 static constants DEFAULT_CARRIER and
        // BULK_UNITS. The Class-File API counts every declared field, static included -
        // unlike a record's component list, which is what a reader might expect.
        assertEquals(5, ClassFileProbe.fieldCount(bytes));
        assertTrue(ClassFileProbe.methodCount(bytes) > 3);
        assertTrue(ClassFileProbe.methodNames(bytes).contains("sku"));
        assertTrue(ClassFileProbe.methodNames(bytes).contains("isBulk"));
    }

    @Test
    public void nonClassFileBytesAreRejectedCleanly() {
        assertEquals(0, ClassFileProbe.majorVersionOf(null));
        assertEquals(0, ClassFileProbe.majorVersionOf(new byte[] {1, 2, 3}));
        assertEquals(0, ClassFileProbe.majorVersionOf("not a class file at all".getBytes()));
        assertEquals("", ClassFileProbe.nameOf(new byte[0]));
        assertEquals("", ClassFileProbe.superclassOf(new byte[0]));
        assertEquals(0, ClassFileProbe.methodCount(null));
        assertEquals(0, ClassFileProbe.fieldCount(null));
        assertTrue(ClassFileProbe.methodNames(null).isEmpty());
    }

    @Test
    public void theApiWritesAsWellAsReads() {
        byte[] emitted = ClassFileProbe.emitMinimal("Demo");
        assertTrue(emitted.length > 0);
        assertEquals("Demo", ClassFileProbe.nameOf(emitted));
        assertEquals(0, ClassFileProbe.methodCount(emitted));
    }

    @Test
    public void emittingRejectsABlankName() {
        assertEquals(0, ClassFileProbe.emitMinimal("").length);
        assertEquals(0, ClassFileProbe.emitMinimal(null).length);
        assertEquals(0, ClassFileProbe.emitMinimal("<<<>>>").length);
    }

    // ---- Reader.of ----

    @Test
    public void readerOfDrainsAnyCharSequence() {
        assertEquals("hello", ClassFileProbe.drain("hello"));
        assertEquals("hello", ClassFileProbe.drain(new StringBuilder("hello")));
        assertEquals(5, ClassFileProbe.drainedLength("hello"));
    }

    @Test
    public void readerOfHandlesLongInputAcrossBufferBoundaries() {
        String long1 = "x".repeat(200);
        assertEquals(200, ClassFileProbe.drainedLength(long1));
        assertEquals(long1, ClassFileProbe.drain(long1));
    }

    @Test
    public void readerOfHandlesEmptyAndNull() {
        assertEquals("", ClassFileProbe.drain(""));
        assertEquals("", ClassFileProbe.drain(null));
        assertEquals(0, ClassFileProbe.drainedLength(null));
        assertFalse(ClassFileProbe.drain("a").isEmpty());
    }
}
