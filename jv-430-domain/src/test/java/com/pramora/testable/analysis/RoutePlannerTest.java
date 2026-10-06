package com.pramora.testable.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.pramora.testable.model.Customer;
import com.pramora.testable.model.LoyaltyTier;
import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;
import com.pramora.testable.model.ShipmentLeg;

/** Covers the Java 16 records, instanceof patterns and Stream additions. */
public class RoutePlannerTest {

    private final RoutePlanner planner = new RoutePlanner();

    private Order order(int... quantities) {
        Order o = new Order("O-16", new Customer("C-16", "Sixteen", LoyaltyTier.GOLD, 8));
        int n = 1;
        for (int q : quantities) {
            o.addLine(new OrderLine("SKU-" + n++, q, 500L));
        }
        return o;
    }

    // ---- record: canonical accessors, equality, compact constructor ----

    @Test
    public void recordAccessorsAreComponentNamed() {
        ShipmentLeg leg = new ShipmentLeg("SKU-1", 3, "air");
        assertEquals("SKU-1", leg.sku());
        assertEquals(3, leg.units());
        assertEquals("AIR", leg.carrier());
    }

    @Test
    public void recordEqualityAndHashAreComponentwise() {
        assertEquals(new ShipmentLeg("SKU-1", 3, "AIR"), new ShipmentLeg("SKU-1", 3, "air"));
        assertEquals(new ShipmentLeg("SKU-1", 3, "AIR").hashCode(),
                new ShipmentLeg("SKU-1", 3, "air").hashCode());
        assertFalse(new ShipmentLeg("SKU-1", 3, "AIR").equals(new ShipmentLeg("SKU-1", 4, "AIR")));
    }

    @Test
    public void recordToStringNamesTheComponents() {
        String s = new ShipmentLeg("SKU-1", 3, "AIR").toString();
        assertTrue(s.contains("sku=SKU-1"));
        assertTrue(s.contains("units=3"));
    }

    @Test
    public void compactConstructorSanitisesTheSku() {
        assertEquals("SKU1script", new ShipmentLeg("SKU 1<script>", 1, null).sku());
    }

    @Test
    public void compactConstructorDefaultsTheCarrier() {
        assertEquals(ShipmentLeg.DEFAULT_CARRIER, new ShipmentLeg("SKU-1", 1, null).carrier());
        assertEquals(ShipmentLeg.DEFAULT_CARRIER, new ShipmentLeg("SKU-1", 1, "   ").carrier());
    }

    @Test
    public void compactConstructorRejectsBadInput() {
        try {
            new ShipmentLeg("SKU-1", 0, "AIR");
            fail("expected non-positive units to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("units must be positive", expected.getMessage());
        }
        try {
            new ShipmentLeg("<<<>>>", 1, "AIR");
            fail("expected an empty sanitised sku to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("sku is required", expected.getMessage());
        }
    }

    @Test
    public void ofBuildsALegFromALine() {
        ShipmentLeg leg = ShipmentLeg.of(new OrderLine("SKU-9", 4, 100L));
        assertEquals("SKU-9", leg.sku());
        assertEquals(4, leg.units());
        assertEquals(ShipmentLeg.DEFAULT_CARRIER, leg.carrier());
        assertEquals("AIR", ShipmentLeg.of(new OrderLine("SKU-9", 4, 100L), "air").carrier());
    }

    @Test
    public void ofRejectsNull() {
        try {
            ShipmentLeg.of(null);
            fail("expected null to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("line is required", expected.getMessage());
        }
    }

    @Test
    public void splitLeavesSmallLegsAlone() {
        ShipmentLeg leg = new ShipmentLeg("SKU-1", 5, "AIR");
        assertEquals(List.of(leg), leg.split(20));
        assertEquals(List.of(leg), leg.split(0));
    }

    @Test
    public void splitChunksLargeLegs() {
        List<ShipmentLeg> parts = new ShipmentLeg("SKU-1", 45, "AIR").split(20);
        assertEquals(3, parts.size());
        assertEquals(20, parts.get(0).units());
        assertEquals(20, parts.get(1).units());
        assertEquals(5, parts.get(2).units());
    }

    // ---- mapMulti / toList ----

    @Test
    public void planKeepsOneLegPerSmallLine() {
        assertEquals(3, planner.plan(order(1, 2, 3)).size());
    }

    @Test
    public void planFansOutOversizedLines() {
        List<ShipmentLeg> legs = planner.plan(order(45, 2));
        assertEquals(4, legs.size());
        assertEquals(47, legs.stream().mapToInt(ShipmentLeg::units).sum());
    }

    @Test
    public void planOfNullOrEmptyIsEmpty() {
        assertTrue(planner.plan(null).isEmpty());
        assertTrue(planner.plan(order()).isEmpty());
    }

    @Test
    public void planIsUnmodifiable() {
        List<ShipmentLeg> legs = planner.plan(order(1));
        try {
            legs.add(new ShipmentLeg("SKU-X", 1, "AIR"));
            fail("toList should return an unmodifiable list");
        } catch (UnsupportedOperationException expected) {
            assertEquals(1, legs.size());
        }
    }

    @Test
    public void totalUnitsMatchesTheOrder() {
        assertEquals(47, planner.totalUnits(order(45, 2)));
        assertEquals(0, planner.totalUnits(null));
    }

    @Test
    public void bulkLegsAreTheFullSizedOnes() {
        assertEquals(2, planner.bulkLegs(order(45, 2)).size());
        assertTrue(planner.bulkLegs(order(1, 2)).isEmpty());
    }

    @Test
    public void carriersAreDistinct() {
        assertEquals(List.of(ShipmentLeg.DEFAULT_CARRIER), planner.carriers(order(45, 2)));
    }

    @Test
    public void customCapIsHonouredAndGuarded() {
        assertEquals(5, new RoutePlanner(1).plan(order(5)).size());
        assertEquals(1, new RoutePlanner(-3).plan(order(5)).size());
    }

    // ---- instanceof patterns ----

    @Test
    public void describeSeparatesBulkFromOrdinaryLegs() {
        assertTrue(RouteDescriber.describe(new ShipmentLeg("SKU-1", 25, "AIR")).startsWith("bulk leg"));
        assertTrue(RouteDescriber.describe(new ShipmentLeg("SKU-1", 2, "AIR")).startsWith("leg "));
    }

    @Test
    public void describeSeparatesOversizedLines() {
        assertEquals("oversized line SKU-1", RouteDescriber.describe(new OrderLine("SKU-1", 30, 1L)));
        assertEquals("line SKU-1", RouteDescriber.describe(new OrderLine("SKU-1", 2, 1L)));
    }

    @Test
    public void describeSeparatesEmptyOrders() {
        assertEquals("empty order O-16", RouteDescriber.describe(order()));
        assertEquals("order O-16 with 2 lines", RouteDescriber.describe(order(1, 1)));
    }

    @Test
    public void describeAcceptsAnyNonBlankCharSequence() {
        assertEquals("note hello", RouteDescriber.describe("hello"));
        assertEquals("note hi", RouteDescriber.describe(new StringBuilder("hi")));
    }

    @Test
    public void describeFallsThroughForBlankAndUnknown() {
        assertEquals("unknown", RouteDescriber.describe("   "));
        assertEquals("unknown", RouteDescriber.describe(null));
        assertEquals("unknown", RouteDescriber.describe(42));
    }
}
