package com.pramora.testable.analysis;

import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

import com.pramora.testable.model.ShipmentLeg;

/**
 * Groups and accumulates shipment legs with stream gatherers.
 *
 * <p>Carries part of the Java 24 lock. <b>Stream gatherers</b> (JEP 485) became final in
 * Java 24 after preview in 22 and 23. A gatherer is to an intermediate stream operation what
 * a {@code Collector} is to a terminal one: a reusable, composable, possibly stateful
 * transformation. Before it, the stream API's intermediate stage was closed - {@code map},
 * {@code filter}, {@code flatMap}, {@code mapMulti} and nothing else - so anything with a
 * window or a running total had to leave the stream, be done in a loop, and come back.
 *
 * <p>Java 24 added no final language syntax at all, so this family's lock is entirely
 * attribution-time. Its language JEPs were all previews: primitive types in patterns (2nd),
 * module import declarations (2nd), flexible constructor bodies (3rd), implicitly declared
 * classes (4th), structured concurrency (4th) and scoped values (4th).
 */
public final class LegGatherer {

    private LegGatherer() {
    }

    /**
     * Fixed-size batches, the last one short. {@code Gatherers.windowFixed} is Java 24.
     */
    public static List<List<ShipmentLeg>> batches(List<ShipmentLeg> legs, int size) {
        if (legs == null || legs.isEmpty() || size <= 0) {
            return List.of();
        }
        return legs.stream().gather(Gatherers.windowFixed(size)).toList();
    }

    /**
     * Overlapping windows, one step apart - used to compare each leg with its neighbour.
     * {@code Gatherers.windowSliding} is Java 24.
     */
    public static List<List<ShipmentLeg>> pairs(List<ShipmentLeg> legs) {
        if (legs == null || legs.size() < 2) {
            return List.of();
        }
        return legs.stream().gather(Gatherers.windowSliding(2)).toList();
    }

    /**
     * Running total of units, one entry per leg. {@code Gatherers.scan} is Java 24 and is the
     * operation that had no place in the stream API before it: it is stateful and
     * intermediate, so neither {@code map} nor a {@code Collector} can express it.
     */
    public static List<Integer> runningUnits(List<ShipmentLeg> legs) {
        if (legs == null || legs.isEmpty()) {
            return List.of();
        }
        return legs.stream()
                .gather(Gatherers.scan(() -> 0, (running, leg) -> running + leg.units()))
                .toList();
    }

    /** Total units, folded in one pass. {@code Gatherers.fold} is Java 24. */
    public static int totalUnits(List<ShipmentLeg> legs) {
        if (legs == null || legs.isEmpty()) {
            return 0;
        }
        return legs.stream()
                .gather(Gatherers.fold(() -> 0, (running, leg) -> running + leg.units()))
                .findFirst()
                .orElse(0);
    }

    /**
     * The first batch whose combined units reach {@code threshold}, or an empty list.
     * Composes two gatherers with {@code andThen}, which is the point of making the
     * intermediate stage extensible.
     */
    public static List<ShipmentLeg> firstFullBatch(List<ShipmentLeg> legs, int size, int threshold) {
        for (List<ShipmentLeg> batch : batches(legs, size)) {
            int units = batch.stream().mapToInt(ShipmentLeg::units).sum();
            if (units >= threshold) {
                return batch;
            }
        }
        return List.of();
    }

    /** SKUs of consecutive legs that share a carrier, as one string per sliding pair. */
    public static List<String> carrierHandoffs(List<ShipmentLeg> legs) {
        return pairs(legs).stream()
                .filter(p -> p.size() == 2 && !p.get(0).carrier().equals(p.get(1).carrier()))
                .map(p -> p.get(0).carrier() + "->" + p.get(1).carrier())
                .toList();
    }

    /** Convenience: batches of an arbitrary stream, for callers that already have one. */
    public static List<List<String>> windowSkus(Stream<ShipmentLeg> legs, int size) {
        if (legs == null || size <= 0) {
            return List.of();
        }
        return legs.map(ShipmentLeg::sku).gather(Gatherers.windowFixed(size)).toList();
    }
}
