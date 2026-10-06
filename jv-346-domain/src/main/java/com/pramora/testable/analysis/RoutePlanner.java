package com.pramora.testable.analysis;

import java.util.List;

import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;
import com.pramora.testable.model.ShipmentLeg;

/**
 * Turns an order into a shipping plan.
 *
 * <p>Carries the Java 16 attribution-time locks:
 *
 * <ul>
 *   <li>{@code Stream.toList}, added in Java 16</li>
 *   <li>{@code Stream.mapMulti}, added in Java 16</li>
 * </ul>
 *
 * <p>This file holds <b>only</b> attribution-time locks. The family's parse-time locks live
 * in {@code model/ShipmentLeg.java} (a record) and {@code analysis/RouteDescriber.java}
 * (pattern matching for {@code instanceof}). Keeping the two kinds in separate files is
 * deliberate: javac halts at the first syntax error in a file, so an API lock sharing a file
 * with a syntax lock is invisible to any per-file check.
 *
 * <p>{@code mapMulti} is the interesting one. {@code flatMap} makes a whole {@code Stream}
 * per element; {@code mapMulti} hands each element a sink and lets it push zero or more
 * results, which is cheaper when most elements produce exactly one result and only a few
 * fan out - precisely the shape of splitting oversized shipment legs below.
 */
public final class RoutePlanner {

    /** Maximum units a single leg may carry before it is split. */
    public static final int MAX_UNITS_PER_LEG = 20;

    private final int maxUnitsPerLeg;

    public RoutePlanner() {
        this(MAX_UNITS_PER_LEG);
    }

    public RoutePlanner(int maxUnitsPerLeg) {
        this.maxUnitsPerLeg = maxUnitsPerLeg <= 0 ? MAX_UNITS_PER_LEG : maxUnitsPerLeg;
    }

    /**
     * Plans every leg for an order, splitting any line that exceeds the per-leg cap.
     * {@code mapMulti} and {@code toList} are both Java 16.
     */
    public List<ShipmentLeg> plan(Order order) {
        if (order == null) {
            return List.of();
        }
        return order.getLines().stream()
                .<ShipmentLeg>mapMulti((line, sink) ->
                        ShipmentLeg.of(line).split(maxUnitsPerLeg).forEach(sink))
                .toList();
    }

    /** Legs that are large enough to route as bulk freight. */
    public List<ShipmentLeg> bulkLegs(Order order) {
        return plan(order).stream().filter(ShipmentLeg::isBulk).toList();
    }

    /** Total units across the plan. */
    public int totalUnits(Order order) {
        return plan(order).stream().mapToInt(ShipmentLeg::units).sum();
    }

    /** Distinct carriers used by the plan, in first-seen order. */
    public List<String> carriers(Order order) {
        return plan(order).stream().map(ShipmentLeg::carrier).distinct().toList();
    }
}
