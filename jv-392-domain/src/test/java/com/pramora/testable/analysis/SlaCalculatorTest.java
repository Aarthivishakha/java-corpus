package com.pramora.testable.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Duration;
import java.util.List;

import org.junit.Test;

import com.pramora.testable.model.RouteEvent;
import com.pramora.testable.model.ShipmentLeg;

/** Covers the Java 18 Math and Duration additions, and the Java 17 sealed hierarchy. */
public class SlaCalculatorTest {

    private final SlaCalculator sla = new SlaCalculator();

    private List<ShipmentLeg> legs(int... units) {
        return java.util.Arrays.stream(units)
                .mapToObj(u -> new ShipmentLeg("SKU-1", u, "GROUND"))
                .toList();
    }

    // ---- Math.ceilDiv / ceilMod ----

    @Test
    public void trucksRoundUp() {
        assertEquals(1, sla.trucksNeeded(legs(1)));
        assertEquals(1, sla.trucksNeeded(legs(30)));
        assertEquals(2, sla.trucksNeeded(legs(31)));
        assertEquals(3, sla.trucksNeeded(legs(30, 30, 1)));
    }

    @Test
    public void emptyPlanNeedsNoTrucks() {
        assertEquals(0, sla.trucksNeeded(List.of()));
        assertEquals(0, sla.trucksNeeded(null));
        assertEquals(0, sla.slackUnits(List.of()));
    }

    @Test
    public void ceilDivAndCeilModAreCompanions() {
        // ceilDiv(a,b) * b + ceilMod(a,b) == a, which is the contract Java 18 added them under
        for (int total : new int[] {1, 7, 29, 30, 31, 59, 60, 61}) {
            int trucks = sla.trucksNeeded(legs(total));
            int slack = sla.slackUnits(legs(total));
            assertEquals("total " + total, total, trucks * SlaCalculator.UNITS_PER_TRUCK + slack);
        }
    }

    @Test
    public void slackIsZeroOnAnExactFit() {
        assertEquals(0, sla.slackUnits(legs(30)));
        assertEquals(0, sla.slackUnits(legs(30, 30)));
    }

    @Test
    public void customTruckSizeIsHonouredAndGuarded() {
        assertEquals(5, new SlaCalculator(2).trucksNeeded(legs(9)));
        assertEquals(1, new SlaCalculator(-4).trucksNeeded(legs(9)));
    }

    // ---- Math.divideExact / floorDivExact / ceilDivExact ----

    @Test
    public void costIsSplitAcrossTrucks() {
        assertEquals(500, sla.costPerTruck(1000, legs(31)));
        assertEquals(0, sla.costPerTruck(1000, List.of()));
    }

    @Test
    public void floorAndCeilShareDifferOnARemainder() {
        assertEquals(3, SlaCalculator.floorShare(10, 3));
        assertEquals(4, SlaCalculator.ceilShare(10, 3));
        assertEquals(3, SlaCalculator.floorShare(9, 3));
        assertEquals(3, SlaCalculator.ceilShare(9, 3));
    }

    @Test
    public void shareRoundsTowardsTheRightSideForNegatives() {
        assertEquals(-4, SlaCalculator.floorShare(-10, 3));
        assertEquals(-3, SlaCalculator.ceilShare(-10, 3));
    }

    @Test
    public void zeroPartsIsRejected() {
        try {
            SlaCalculator.floorShare(1, 0);
            fail("expected zero parts to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("parts must not be zero", expected.getMessage());
        }
        try {
            SlaCalculator.ceilShare(1, 0);
            fail("expected zero parts to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("parts must not be zero", expected.getMessage());
        }
    }

    @Test
    public void exactVariantsThrowOnTheOneOverflowingDivision() {
        // Integer.MIN_VALUE / -1 is the single int division that overflows. The pre-Java-18
        // operators wrap silently; the Exact variants are what turn that into a failure.
        try {
            SlaCalculator.floorShare(Integer.MIN_VALUE, -1);
            fail("expected floorDivExact to reject the overflowing division");
        } catch (ArithmeticException expected) {
            assertTrue(expected.getMessage().toLowerCase().contains("overflow"));
        }
        try {
            SlaCalculator.ceilShare(Integer.MIN_VALUE, -1);
            fail("expected ceilDivExact to reject the overflowing division");
        } catch (ArithmeticException expected) {
            assertTrue(expected.getMessage().toLowerCase().contains("overflow"));
        }
    }

    // ---- Duration.isPositive ----

    @Test
    public void withinWindowWhileTimeRemains() {
        assertTrue(sla.withinWindow(Duration.ofHours(1), Duration.ofHours(4)));
        assertFalse(sla.withinWindow(Duration.ofHours(4), Duration.ofHours(4)));
        assertFalse(sla.withinWindow(Duration.ofHours(5), Duration.ofHours(4)));
    }

    @Test
    public void withinWindowHandlesNulls() {
        assertFalse(sla.withinWindow(null, Duration.ofHours(4)));
        assertFalse(sla.withinWindow(Duration.ofHours(1), null));
    }

    @Test
    public void remainingIsFlooredAtZero() {
        assertEquals(Duration.ofHours(3), sla.remaining(Duration.ofHours(1), Duration.ofHours(4)));
        assertEquals(Duration.ZERO, sla.remaining(Duration.ofHours(9), Duration.ofHours(4)));
        assertEquals(Duration.ZERO, sla.remaining(null, null));
    }

    // ---- sealed hierarchy ----

    @Test
    public void everyEventKindSummarises() {
        ShipmentLeg leg = new ShipmentLeg("SKU-7", 3, "AIR");
        assertEquals("planned SKU-7", sla.summarise(new RouteEvent.Planned(leg)));
        assertEquals("in transit SKU-7", sla.summarise(new RouteEvent.Dispatched(leg, "T-1")));
        assertEquals("delivered SKU-7", sla.summarise(new RouteEvent.Delivered(leg, 90L)));
        assertEquals("failed SKU-7", sla.summarise(new RouteEvent.Failed(leg, "no capacity")));
        assertEquals("none", sla.summarise(null));
    }

    @Test
    public void terminalStatesAreDeliveredAndFailed() {
        ShipmentLeg leg = new ShipmentLeg("SKU-7", 3, "AIR");
        assertFalse(new RouteEvent.Planned(leg).isTerminal());
        assertFalse(new RouteEvent.Dispatched(leg, "T-1").isTerminal());
        assertTrue(new RouteEvent.Delivered(leg, 1L).isTerminal());
        assertTrue(new RouteEvent.Failed(leg, "x").isTerminal());
    }

    @Test
    public void eventCompactConstructorsNormaliseAndValidate() {
        ShipmentLeg leg = new ShipmentLeg("SKU-7", 3, "AIR");
        assertEquals("", new RouteEvent.Dispatched(leg, null).trackingRef());
        assertEquals("T-2", new RouteEvent.Dispatched(leg, "  T-2  ").trackingRef());
        assertEquals("unspecified", new RouteEvent.Failed(leg, "   ").reason());
        try {
            new RouteEvent.Planned(null);
            fail("expected a null leg to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("leg is required", expected.getMessage());
        }
        try {
            new RouteEvent.Delivered(leg, -1L);
            fail("expected negative transit to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("transit minutes must not be negative", expected.getMessage());
        }
    }

    @Test
    public void sealedHierarchyIsClosedToFourSubtypes() {
        assertTrue(RouteEvent.class.isSealed());
        assertEquals(4, RouteEvent.class.getPermittedSubclasses().length);
    }
}
