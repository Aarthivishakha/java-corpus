package com.pramora.testable.analysis;

import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;
import com.pramora.testable.model.ShipmentLeg;
import com.pramora.testable.service.PricingReport;

/**
 * Describes a heterogeneous routing hint in one line.
 *
 * <p>Carries one of the Java 16 parse-time locks: <b>pattern matching for
 * {@code instanceof}</b> (JEP 394) became final in Java 16 after preview in 14 and a second
 * preview in 15. The binding variable is in scope exactly where the pattern is known to have
 * matched, which is what lets the {@code &&} refinements below read the way they do - the
 * binding is usable in the same expression that introduced it.
 *
 * <p>This file holds only parse-time locks; the family's attribution-time locks are in
 * {@code analysis/RoutePlanner.java}. javac halts at the first syntax error in a file, so
 * mixing the two kinds in one file would hide the API locks from any per-file check.
 */
public final class RouteDescriber {

    private RouteDescriber() {
    }

    /**
     * Every branch is a Java 16 type pattern with a binding variable, several of them
     * refined by a condition that uses the binding in the same expression.
     */
    public static String describe(Object hint) {
        if (hint instanceof ShipmentLeg leg && leg.isBulk()) {
            return "bulk leg " + leg.sku() + " x" + leg.units() + " via " + leg.carrier();
        }
        if (hint instanceof ShipmentLeg leg) {
            return "leg " + leg.sku() + " x" + leg.units() + " via " + leg.carrier();
        }
        if (hint instanceof OrderLine line && line.getQuantity() >= RoutePlanner.MAX_UNITS_PER_LEG) {
            return "oversized line " + line.getSku();
        }
        if (hint instanceof OrderLine line) {
            return "line " + line.getSku();
        }
        if (hint instanceof Order order && order.lineCount() == 0) {
            return "empty order " + order.getId();
        }
        if (hint instanceof Order order) {
            return "order " + order.getId() + " with " + order.lineCount() + " lines";
        }
        if (hint instanceof CharSequence text && !PricingReport.isBlank(text)) {
            return "note " + text;
        }
        return "unknown";
    }
}
