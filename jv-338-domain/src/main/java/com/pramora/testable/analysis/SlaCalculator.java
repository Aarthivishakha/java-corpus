package com.pramora.testable.analysis;

import java.time.Duration;
import java.util.List;

import com.pramora.testable.model.RouteEvent;
import com.pramora.testable.model.ShipmentLeg;

/**
 * Capacity and service-window arithmetic for a shipping plan.
 *
 * <p>This file carries the family's <b>attribution-time</b> lock, and it is the whole of the
 * Java 18 lock, because <b>Java 18 added no final language syntax at all</b>. Its JEPs were
 * UTF-8 by default (400), the Simple Web Server (408), Javadoc snippets (413), a reflection
 * reimplementation (416), the Vector API's third incubator (417), an address-resolution SPI
 * (418), FFM's second incubator (419) and the deprecation of finalization (421) - none of
 * which change the language - plus a <em>second preview</em> of pattern matching for
 * {@code switch} (420), which the corpus's preview-off rule excludes. Java 18 is therefore
 * the third API-only family, after Java 12 and Java 13.
 *
 * <p>The APIs below all shipped final in Java 18:
 *
 * <ul>
 *   <li>{@code Math.ceilDiv}, {@code Math.ceilMod} - the ceiling counterparts of
 *       {@code floorDiv}/{@code floorMod}, which had existed since Java 8. Rounding a
 *       division up is what capacity planning actually needs, and before Java 18 it was
 *       written by hand as {@code (a + b - 1) / b} - which overflows silently.</li>
 *   <li>{@code Math.divideExact}, {@code Math.floorDivExact}, {@code Math.ceilDivExact} -
 *       the same operations that throw {@code ArithmeticException} on overflow instead of
 *       wrapping. There is exactly one pair of ints where integer division overflows:
 *       {@code Integer.MIN_VALUE / -1}.</li>
 *   <li>{@code Duration.isPositive} - previously written as
 *       {@code !d.isNegative() && !d.isZero()}.</li>
 * </ul>
 */
public final class SlaCalculator {

    /** Units one truck can carry. */
    public static final int UNITS_PER_TRUCK = 30;

    private final int unitsPerTruck;

    public SlaCalculator() {
        this(UNITS_PER_TRUCK);
    }

    public SlaCalculator(int unitsPerTruck) {
        this.unitsPerTruck = unitsPerTruck <= 0 ? UNITS_PER_TRUCK : unitsPerTruck;
    }

    /**
     * Trucks needed to move a plan. {@code Math.ceilDiv} is Java 18; the hand-written
     * {@code (total + cap - 1) / cap} it replaces overflows for large totals.
     */
    public int trucksNeeded(List<ShipmentLeg> legs) {
        int total = totalUnits(legs);
        return total == 0 ? 0 : Math.ceilDiv(total, unitsPerTruck);
    }

    /**
     * Units left over once the full trucks are loaded, as a negative offset from a full
     * truck. {@code Math.ceilMod} is Java 18 and is the exact companion of
     * {@code ceilDiv}: {@code ceilDiv(a,b) * b + ceilMod(a,b) == a}.
     */
    public int slackUnits(List<ShipmentLeg> legs) {
        int total = totalUnits(legs);
        return total == 0 ? 0 : Math.ceilMod(total, unitsPerTruck);
    }

    /**
     * Splits a total cost evenly across trucks, refusing to lose money to rounding.
     * {@code Math.divideExact} is Java 18 and throws on the one overflowing int division
     * rather than wrapping.
     */
    public int costPerTruck(int totalCostCents, List<ShipmentLeg> legs) {
        int trucks = trucksNeeded(legs);
        if (trucks == 0) {
            return 0;
        }
        return Math.divideExact(totalCostCents, trucks);
    }

    /** Overflow-checked floor division. {@code Math.floorDivExact} is Java 18. */
    public static int floorShare(int amount, int parts) {
        if (parts == 0) {
            throw new IllegalArgumentException("parts must not be zero");
        }
        return Math.floorDivExact(amount, parts);
    }

    /** Overflow-checked ceiling division. {@code Math.ceilDivExact} is Java 18. */
    public static int ceilShare(int amount, int parts) {
        if (parts == 0) {
            throw new IllegalArgumentException("parts must not be zero");
        }
        return Math.ceilDivExact(amount, parts);
    }

    /**
     * Time left in the service window. {@code Duration.isPositive} is Java 18 and replaces
     * {@code !d.isNegative() && !d.isZero()}.
     */
    public boolean withinWindow(Duration elapsed, Duration window) {
        if (elapsed == null || window == null) {
            return false;
        }
        return window.minus(elapsed).isPositive();
    }

    /** Remaining window, floored at zero. */
    public Duration remaining(Duration elapsed, Duration window) {
        if (elapsed == null || window == null) {
            return Duration.ZERO;
        }
        Duration left = window.minus(elapsed);
        return left.isPositive() ? left : Duration.ZERO;
    }

    /**
     * Summarises a route event. A plain arrow switch over the event's stable kind - not a
     * pattern switch, which was still preview in Java 18.
     */
    public String summarise(RouteEvent event) {
        if (event == null) {
            return "none";
        }
        return switch (event.kind()) {
            case "PLANNED" -> "planned " + event.leg().sku();
            case "DISPATCHED" -> "in transit " + event.leg().sku();
            case "DELIVERED" -> "delivered " + event.leg().sku();
            case "FAILED" -> "failed " + event.leg().sku();
            default -> "unknown";
        };
    }

    private int totalUnits(List<ShipmentLeg> legs) {
        if (legs == null || legs.isEmpty()) {
            return 0;
        }
        return legs.stream().mapToInt(ShipmentLeg::units).sum();
    }
}
