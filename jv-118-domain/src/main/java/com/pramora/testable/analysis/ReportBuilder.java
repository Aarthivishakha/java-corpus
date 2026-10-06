package com.pramora.testable.analysis;

import module java.base;

import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;

/// Builds a one-line summary of an order.
///
/// The single `import module java.base;` above replaces every individual `java.util.*`
/// import — a **module import declaration** (JEP 511, final in Java 25). Under
/// `--release 21` javac rejects it as *"module imports are not supported"*.
public class ReportBuilder {

    /// Summarises an order as `<id>: <n> lines, <total>c, top=<sku>`.
    public String summarise(Order order) {
        if (order == null) {
            return "no order";
        }
        List<String> parts = new ArrayList<>();
        parts.add(order.getId());
        parts.add(order.lineCount() + " lines");
        parts.add(order.subtotalCents() + "c");
        parts.add("top=" + order.largestLine().map(OrderLine::sku).orElse("-"));
        return String.join(", ", parts);
    }

    /// Distinct SKUs on the order, sorted.
    public Set<String> skus(Order order) {
        return order.getLines().stream()
                .map(OrderLine::sku)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
