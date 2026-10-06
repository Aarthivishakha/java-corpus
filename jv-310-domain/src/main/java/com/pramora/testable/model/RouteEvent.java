package com.pramora.testable.model;

/**
 * Something that happened to a shipment leg.
 *
 * <p>This is a <b>sealed interface</b>, and sealed types (JEP 409) became final in Java 17
 * after preview in 15 (JEP 360) and a second preview in 16 (JEP 397) - the fifth
 * three-release preview arc in this corpus. A sealed type names its permitted subtypes, so
 * the compiler knows the hierarchy is closed and can check a switch over it for
 * exhaustiveness.
 *
 * <p>This file carries the family's <b>parse-time</b> lock and nothing else. Splitting the
 * two lock kinds across files is a rule the corpus adopted at java16: javac halts at the
 * first syntax error in a file, so an API lock sharing a file with a syntax lock can never be
 * reported by a per-file check.
 *
 * <p>Note that the switch over these subtypes in {@code analysis/SlaCalculator.java} is a
 * plain arrow switch over an enum-like tag, <em>not</em> a pattern switch. Pattern matching
 * for {@code switch} was still in preview in Java 18 (its second, JEP 420) and only became
 * final in Java 21, so under the corpus's preview-off rule it belongs to the java21 family.
 */
public sealed interface RouteEvent
        permits RouteEvent.Planned, RouteEvent.Dispatched, RouteEvent.Delivered,
                RouteEvent.Failed {

    /** The leg this event concerns. */
    ShipmentLeg leg();

    /** Short stable name, used where a switch over the closed hierarchy would otherwise go. */
    String kind();

    /** A leg has been planned but not yet handed to a carrier. */
    record Planned(ShipmentLeg leg) implements RouteEvent {
        public Planned {
            if (leg == null) {
                throw new IllegalArgumentException("leg is required");
            }
        }

        @Override
        public String kind() {
            return "PLANNED";
        }
    }

    /** A leg is with the carrier. */
    record Dispatched(ShipmentLeg leg, String trackingRef) implements RouteEvent {
        public Dispatched {
            if (leg == null) {
                throw new IllegalArgumentException("leg is required");
            }
            trackingRef = trackingRef == null ? "" : trackingRef.strip();
        }

        @Override
        public String kind() {
            return "DISPATCHED";
        }
    }

    /** A leg has arrived. */
    record Delivered(ShipmentLeg leg, long transitMinutes) implements RouteEvent {
        public Delivered {
            if (leg == null) {
                throw new IllegalArgumentException("leg is required");
            }
            if (transitMinutes < 0L) {
                throw new IllegalArgumentException("transit minutes must not be negative");
            }
        }

        @Override
        public String kind() {
            return "DELIVERED";
        }
    }

    /** A leg could not be routed. */
    record Failed(ShipmentLeg leg, String reason) implements RouteEvent {
        public Failed {
            if (leg == null) {
                throw new IllegalArgumentException("leg is required");
            }
            reason = (reason == null || reason.isBlank()) ? "unspecified" : reason.strip();
        }

        @Override
        public String kind() {
            return "FAILED";
        }
    }

    /** True once the leg has reached a state it cannot leave. */
    default boolean isTerminal() {
        return this instanceof Delivered || this instanceof Failed;
    }
}
