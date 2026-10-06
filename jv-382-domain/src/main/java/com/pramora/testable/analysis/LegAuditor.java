package com.pramora.testable.analysis;

import java.util.ArrayList;
import java.util.List;

import com.pramora.testable.model.RouteEvent;
import com.pramora.testable.model.ShipmentLeg;

/**
 * Counts and checks shipment legs without naming what it does not use.
 *
 * <p>Carries the <b>Java 22</b> parse-time lock: <b>unnamed variables and unnamed patterns</b>
 * (JEP 456), final in Java 22 after a single preview in 21. The underscore {@code _} stands
 * where a name is syntactically required but the value is never read, and the compiler
 * enforces that - referring to {@code _} is an error, and declaring two named variables with
 * the same name in one scope is an error while two {@code _} are not.
 *
 * <p>All four positions it is legal in appear below:
 *
 * <ul>
 *   <li>an enhanced-for variable - {@code for (var _ : legs)}</li>
 *   <li>a catch parameter - {@code catch (NumberFormatException _)}</li>
 *   <li>a local variable in a try-with-resources or a plain declaration</li>
 *   <li>a record component in a pattern - {@code case Delivered(ShipmentLeg leg, _)}</li>
 * </ul>
 *
 * <p>The last is the one that changes what can be expressed rather than only how it reads:
 * before Java 22 a record pattern had to bind <em>every</em> component, so destructuring one
 * field of a four-field record meant naming three variables the compiler would then warn were
 * unused.
 *
 * <p>This file holds only parse-time locks; the family's Java 22 API lock is in
 * {@code util/NativeMetrics.java}.
 */
public final class LegAuditor {

    private LegAuditor() {
    }

    /** Counts legs without reading them. The enhanced-for variable is unnamed. */
    public static int count(List<ShipmentLeg> legs) {
        if (legs == null) {
            return 0;
        }
        int n = 0;
        for (var _ : legs) {
            n++;
        }
        return n;
    }

    /**
     * Parses a unit count, returning the fallback on anything malformed.
     * The catch parameter is unnamed because the exception itself is never inspected.
     */
    public static int parseUnits(String raw, int fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(raw.strip());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException _) {
            return fallback;
        }
    }

    /**
     * The SKU carried by an event, ignoring every other component.
     * Each record pattern binds only the component it needs; the rest are {@code _}.
     */
    public static String skuOf(RouteEvent event) {
        return switch (event) {
            case null -> "";
            case RouteEvent.Delivered(ShipmentLeg(String sku, _, _), _) -> sku;
            case RouteEvent.Dispatched(ShipmentLeg(String sku, _, _), _) -> sku;
            case RouteEvent.Failed(ShipmentLeg(String sku, _, _), _) -> sku;
            case RouteEvent.Planned(ShipmentLeg(String sku, _, _)) -> sku;
        };
    }

    /** The carrier of an event, again binding one component out of several. */
    public static String carrierOf(RouteEvent event) {
        return switch (event) {
            case null -> "";
            case RouteEvent.Delivered(ShipmentLeg(_, _, String carrier), _) -> carrier;
            case RouteEvent.Dispatched(ShipmentLeg(_, _, String carrier), _) -> carrier;
            case RouteEvent.Failed(ShipmentLeg(_, _, String carrier), _) -> carrier;
            case RouteEvent.Planned(ShipmentLeg(_, _, String carrier)) -> carrier;
        };
    }

    /**
     * Total units across the legs of a batch of events, discarding the event-specific
     * payload in every case.
     */
    public static int totalUnits(List<RouteEvent> events) {
        if (events == null) {
            return 0;
        }
        int total = 0;
        for (RouteEvent e : events) {
            total += switch (e) {
                case null -> 0;
                case RouteEvent.Delivered(ShipmentLeg(_, int units, _), _) -> units;
                case RouteEvent.Dispatched(ShipmentLeg(_, int units, _), _) -> units;
                case RouteEvent.Failed(ShipmentLeg(_, int units, _), _) -> units;
                case RouteEvent.Planned(ShipmentLeg(_, int units, _)) -> units;
            };
        }
        return total;
    }

    /**
     * SKUs of every event whose leg is bulk. The unnamed local below stands for a value the
     * side effect of computing is wanted but the value is not.
     */
    public static List<String> bulkSkus(List<RouteEvent> events) {
        List<String> out = new ArrayList<>();
        if (events == null) {
            return List.of();
        }
        for (RouteEvent e : events) {
            if (e == null) {
                continue;
            }
            var _ = e.kind();
            if (e.leg().isBulk()) {
                out.add(e.leg().sku());
            }
        }
        return List.copyOf(out);
    }
}
