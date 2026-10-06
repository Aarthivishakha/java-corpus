package com.pramora.testable.service;

import com.pramora.testable.model.PricingSnapshot;

/**
 * Renders a pricing snapshot as a short plain-text report.
 *
 * <p>This class carries the whole Java 15 version lock, in both layers:
 *
 * <ul>
 *   <li><b>parse-time</b> - text blocks (JEP 378) became final in Java 15 after preview in
 *       13 (JEP 355) and 14 (JEP 368). Under the corpus's preview-off rule neither of those
 *       families could use them, which is why the java13 lock had to be API-only.</li>
 *   <li><b>attribution-time</b> - {@code String.formatted}, {@code String.stripIndent} and
 *       {@code String.translateEscapes}, plus {@code CharSequence.isEmpty}.</li>
 * </ul>
 *
 * <p><b>The three String methods have a history worth knowing.</b> They first appeared in
 * JDK 13 alongside preview text blocks, carrying {@code @Deprecated(forRemoval=true)}
 * because they were provisional. They were <b>removed in JDK 14</b>, and re-added as final
 * API in JDK 15. Compiled against {@code --release}, that reads as: absent at 12, present
 * at 13, absent at 14, present from 15 on.
 *
 * <p>They were evaluated as the Java 13 family's lock and rejected for exactly that reason -
 * a lock that disappears one release later breaks a cumulative ladder. Here they are
 * legitimate: from Java 15 they are final and monotonic through 25. The corpus's
 * forward-monotonicity gate exists because of them, and this is the family where they pass
 * it.
 */
public final class PricingReport {

    /** Text block. Final in Java 15; a parse error under {@code --release 14}. */
    private static final String TEMPLATE = """
            order %s
              lines    : %d
              subtotal : %d cents
              discount : %d cents
              shipping : %d cents
              total    : %d cents
              risk     : %d
            """;

    /** A second text block, used for the escaped-caption path. */
    private static final String CAPTION_PREFIX = """
            note:\s""";

    private PricingReport() {
    }

    /**
     * Renders the full report. {@code String.formatted} is the instance-side equivalent of
     * {@code String.format(this, args)} and is Java 15.
     */
    public static String render(PricingSnapshot snapshot, int lineCount) {
        if (snapshot == null) {
            return "";
        }
        return TEMPLATE.formatted(
                snapshot.getOrderId(),
                Math.max(0, lineCount),
                snapshot.getSubtotalCents(),
                snapshot.getDiscountCents(),
                snapshot.shippingCents(),
                snapshot.getTotalCents(),
                snapshot.getRiskScore());
    }

    /**
     * Renders a one-line summary from an indented concatenated literal.
     * {@code String.stripIndent} removes the common leading whitespace from every line and
     * is Java 15 - it is what a text block applies automatically, exposed for strings that
     * are not text blocks.
     */
    public static String renderSummary(PricingSnapshot snapshot) {
        if (snapshot == null) {
            return "";
        }
        String indented = "        " + snapshot.getOrderId()
                + " -> " + snapshot.getTotalCents() + " cents"
                + " (risk " + snapshot.getRiskScore() + ")";
        return indented.stripIndent();
    }

    /**
     * Turns a caption whose escapes are still literal backslash sequences - as they arrive
     * from a properties file or a CSV cell - into real characters.
     * {@code String.translateEscapes} is Java 15.
     */
    public static String caption(String rawWithEscapes) {
        if (isBlank(rawWithEscapes)) {
            return "";
        }
        return CAPTION_PREFIX + rawWithEscapes.translateEscapes();
    }

    /**
     * Blank test over any {@code CharSequence}.
     * {@code CharSequence.isEmpty} is a Java 15 default method; before it, a
     * {@code CharSequence} had to be tested with {@code length() == 0}.
     */
    public static boolean isBlank(CharSequence value) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** Number of lines the rendered report occupies. */
    public static int lineCount(PricingSnapshot snapshot, int lines) {
        String rendered = render(snapshot, lines);
        if (rendered.isEmpty()) {
            return 0;
        }
        return rendered.lines().toArray().length;
    }
}
