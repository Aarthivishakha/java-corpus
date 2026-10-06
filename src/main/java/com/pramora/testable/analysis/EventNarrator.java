package com.pramora.testable.analysis;

import com.pramora.testable.model.RouteEvent;
import com.pramora.testable.model.ShipmentLeg;

/**
 * Describes a route event in one line.
 *
 * <p>Carries a <b>Java 21</b> parse-time lock, inherited because this family sits directly
 * above java20 in the cumulative ladder and Java 21 is the release in between:
 *
 * <ul>
 *   <li><b>pattern matching for {@code switch}</b> (JEP 441) - final in Java 21 after four
 *       rounds of preview in 17, 18, 19 and 20. The longest preview run in the language.</li>
 *   <li><b>record patterns</b> (JEP 440) - final in Java 21, destructuring a record directly
 *       in the case label.</li>
 *   <li><b>guarded patterns</b> ({@code when} clauses) and a {@code null} case label.</li>
 * </ul>
 *
 * <p>Because {@link RouteEvent} is a sealed interface, the switch below is <b>exhaustive
 * without a default</b> - the compiler knows the hierarchy is closed. Add a permitted subtype
 * and this file stops compiling, which is the point of pairing sealed types with pattern
 * switches.
 *
 * <p>This file holds only parse-time locks; the family's Java 21 API lock is in
 * {@code analysis/RouteLog.java}, and Java 22's two locks are in
 * {@code analysis/LegAuditor.java} and {@code util/NativeMetrics.java}. One lock kind per
 * file is a rule the corpus adopted at java16, because javac halts at the first syntax error
 * in a file.
 */
public final class EventNarrator {

    private EventNarrator() {
    }

    /**
     * Exhaustive pattern switch over the sealed hierarchy, with record deconstruction in
     * three of the four labels and a guard on the first.
     */
    public static String narrate(RouteEvent event) {
        return switch (event) {
            case null -> "no event";
            case RouteEvent.Delivered(ShipmentLeg leg, long minutes) when minutes > 240L ->
                    leg.sku() + " delivered late (" + minutes + " min)";
            case RouteEvent.Delivered(ShipmentLeg leg, long minutes) ->
                    leg.sku() + " delivered in " + minutes + " min";
            case RouteEvent.Failed(ShipmentLeg leg, String reason) ->
                    leg.sku() + " failed: " + reason;
            case RouteEvent.Dispatched(ShipmentLeg leg, String ref) when ref.isEmpty() ->
                    leg.sku() + " dispatched, no tracking";
            case RouteEvent.Dispatched(ShipmentLeg leg, String ref) ->
                    leg.sku() + " dispatched as " + ref;
            case RouteEvent.Planned(ShipmentLeg leg) ->
                    leg.sku() + " planned x" + leg.units();
        };
    }

    /** Priority for the review queue. A pattern switch returning an int. */
    public static int priority(RouteEvent event) {
        return switch (event) {
            case null -> 0;
            case RouteEvent.Failed _f -> 100;
            case RouteEvent.Dispatched(ShipmentLeg _l, String ref) when ref.isEmpty() -> 60;
            case RouteEvent.Delivered(ShipmentLeg _l, long minutes) when minutes > 240L -> 40;
            case RouteEvent.Dispatched _d -> 20;
            case RouteEvent.Delivered _d -> 10;
            case RouteEvent.Planned _p -> 5;
        };
    }

    /**
     * Nested record deconstruction: the leg inside the event is itself destructured, so the
     * sku and unit count are bound directly from the case label.
     */
    public static int unitsInFlight(RouteEvent event) {
        return switch (event) {
            case null -> 0;
            case RouteEvent.Dispatched(ShipmentLeg(String sku, int units, String carrier),
                                       String ref) -> units;
            case RouteEvent.Planned(ShipmentLeg(String sku, int units, String carrier)) -> units;
            case RouteEvent.Delivered _d -> 0;
            case RouteEvent.Failed _f -> 0;
        };
    }
}
