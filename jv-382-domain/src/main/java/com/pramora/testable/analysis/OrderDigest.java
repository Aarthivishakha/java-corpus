package com.pramora.testable.analysis;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;

import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;
import com.pramora.testable.util.InputSanitizer;

/**
 * Renders a short human-readable digest of an order.
 *
 * <p>This class carries the Java 12 version lock for this family. Every API it uses
 * below was added in Java 12 and is absent from Java 11, so the file compiles at
 * {@code --release 12} and fails at {@code --release 11}:
 *
 * <ul>
 *   <li>{@code Collectors.teeing} - two downstream collectors, one pass (JDK-8209685)</li>
 *   <li>{@code String.transform} - applies a function to the receiver</li>
 *   <li>{@code String.indent} - prefixes every line with n spaces</li>
 *   <li>{@code NumberFormat.getCompactNumberInstance} and {@code CompactNumberFormat}</li>
 * </ul>
 *
 * <p>Java 12's other headline change, switch expressions, was a <em>preview</em>
 * feature in 12 and did not become final until Java 14. The corpus rule is that
 * preview features stay off, so the Java 12 lock is entirely an API lock - there is
 * no Java-12-final syntax to lock against.
 */
public final class OrderDigest {

    /** Holds the two figures the teeing collector produces in a single traversal. */
    public static final class LineStats {

        private final long count;
        private final long totalCents;

        LineStats(long count, long totalCents) {
            this.count = count;
            this.totalCents = totalCents;
        }

        public long getCount() {
            return count;
        }

        public long getTotalCents() {
            return totalCents;
        }

        public long averageCents() {
            return count == 0L ? 0L : totalCents / count;
        }
    }

    private final Locale locale;

    public OrderDigest() {
        this(Locale.US);
    }

    public OrderDigest(Locale locale) {
        this.locale = locale == null ? Locale.US : locale;
    }

    /**
     * Counts the lines and sums their extended amounts in one traversal.
     * Collectors.teeing is Java 12; on Java 11 this needs two passes or a manual reduce.
     */
    public LineStats stats(Order order) {
        if (order == null) {
            return new LineStats(0L, 0L);
        }
        return order.getLines().stream()
                .collect(Collectors.teeing(
                        Collectors.counting(),
                        Collectors.summingLong(OrderLine::extendedCents),
                        LineStats::new));
    }

    /**
     * Formats a cents amount compactly - 1234500 cents becomes something like "12K".
     * NumberFormat.getCompactNumberInstance and the CompactNumberFormat class are Java 12.
     */
    public String compactAmount(long cents) {
        NumberFormat compact =
                NumberFormat.getCompactNumberInstance(locale, NumberFormat.Style.SHORT);
        compact.setMaximumFractionDigits(1);
        return compact.format(cents / 100L);
    }

    /** Plain, non-compact rendering of the same figure, for comparison in tests. */
    public String plainAmount(long cents) {
        return NumberFormat.getIntegerInstance(locale).format(cents / 100L);
    }

    /**
     * A short indented report. String.transform and String.indent are both Java 12.
     * String.indent appends a line terminator to every line it emits, so the result
     * always ends in a newline.
     */
    public String render(Order order) {
        if (order == null) {
            return "";
        }
        LineStats s = stats(order);
        String id = order.getId().transform(InputSanitizer::sanitize);
        String body = "lines: " + s.getCount() + "\n"
                + "total: " + compactAmount(s.getTotalCents()) + "\n"
                + "avg:   " + compactAmount(s.averageCents());
        return ("order " + id + "\n" + body.indent(2)).transform(OrderDigest::trimTrailing);
    }

    /** Turns the digest into a single line. String.transform is Java 12. */
    public String renderOneLine(Order order) {
        return render(order).transform(text -> text.replace('\n', ' ')).transform(String::strip);
    }

    private static String trimTrailing(String value) {
        return value.stripTrailing();
    }
}
