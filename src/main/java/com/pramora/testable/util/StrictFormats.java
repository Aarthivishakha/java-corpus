package com.pramora.testable.util;

import java.lang.foreign.MemorySegment;
import java.net.Inet4Address;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

/**
 * Parses externally supplied numbers, addresses and timestamps without guessing.
 *
 * <p>Carries the whole Java 23 lock, and all of it is attribution-time, because <b>Java 23
 * added no final language syntax</b>. Its one language JEP was Markdown documentation
 * comments (JEP 467), and {@code ///} is lexically an ordinary line comment - it compiles
 * unchanged at every release back to Java 8, so it locks nothing. Everything else Java 23
 * carried was preview or incubator: primitive types in patterns, module import declarations,
 * implicitly declared classes (3rd), flexible constructor bodies (2nd), stream gatherers
 * (2nd), structured concurrency (3rd), scoped values (3rd), the Class-File API (2nd) and the
 * Vector API (8th incubator). Java 23 is the fourth API-only family, after 12, 13, 18, 19
 * and 20.
 *
 * <p>The locks below were found by diffing {@code javac}'s own {@code ct.sym} release
 * signatures between 22 and 23, the same method the java20 family needed:
 *
 * <ul>
 *   <li>{@code NumberFormat.setStrict} / {@code isStrict} - strict parsing, Java 23. By
 *       default {@code NumberFormat.parse} stops at the first character it cannot use and
 *       returns what it has, so {@code "12abc"} parses to {@code 12} and {@code "1,2,3"} to
 *       {@code 123}. Strict mode makes both a {@code ParseException}. For a value arriving
 *       from outside the system that difference is a defect class, not a preference.</li>
 *   <li>{@code Instant.until(Instant)} - Java 23, returning a {@code Duration} directly
 *       rather than through {@code Duration.between} or a {@code ChronoUnit}.</li>
 *   <li>{@code Inet4Address.ofPosixLiteral} - Java 23, parsing the POSIX forms
 *       {@code inet_addr} accepts: octal, hex and shortened dotted notation. See
 *       {@link #resolvePosix} for why this exists.</li>
 *   <li>{@code MemorySegment.maxByteAlignment} - Java 23, on the FFM API this corpus first
 *       used at java22.</li>
 * </ul>
 */
public final class StrictFormats {

    private StrictFormats() {
    }

    /** A number format that refuses partial matches. {@code setStrict} is Java 23. */
    public static NumberFormat strictInteger(Locale locale) {
        NumberFormat format = NumberFormat.getIntegerInstance(locale == null ? Locale.US : locale);
        format.setStrict(true);
        return format;
    }

    /** The lenient default, for comparison. */
    public static NumberFormat lenientInteger(Locale locale) {
        NumberFormat format = NumberFormat.getIntegerInstance(locale == null ? Locale.US : locale);
        format.setStrict(false);
        return format;
    }

    /** True when the format rejects partial matches. {@code isStrict} is Java 23. */
    public static boolean isStrict(NumberFormat format) {
        return format != null && format.isStrict();
    }

    /**
     * Parses a quantity, returning {@code fallback} when the text is not entirely a number.
     * Strict mode is what makes {@code "12abc"} a failure rather than {@code 12}.
     */
    public static long parseQuantity(String raw, long fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            Number parsed = strictInteger(Locale.US).parse(raw.strip());
            long value = parsed.longValue();
            return value >= 0L ? value : fallback;
        } catch (ParseException rejected) {
            return fallback;
        }
    }

    /** The same parse without strict mode, to show what strict mode prevents. */
    public static long parseQuantityLeniently(String raw, long fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return lenientInteger(Locale.US).parse(raw.strip()).longValue();
        } catch (ParseException rejected) {
            return fallback;
        }
    }

    /** A strict decimal format for money amounts. */
    public static DecimalFormat strictDecimal() {
        DecimalFormat format = new DecimalFormat("#,##0.00");
        format.setStrict(true);
        return format;
    }

    /**
     * Parses an IPv4 address in any form {@code inet_addr} accepts - dotted decimal, but also
     * octal ({@code 0177.0.0.1}), hex ({@code 0x7f.0.0.1}) and shortened
     * ({@code 127.1}) forms, all of which denote 127.0.0.1.
     *
     * <p>{@code Inet4Address.ofPosixLiteral} is Java 23 and exists precisely because those
     * forms are a hazard: a filter that blocklists the string {@code "127.0.0.1"} does not
     * stop {@code "0177.0.0.1"}, and before Java 23 the JDK's own parsers disagreed about
     * which forms they accepted. Having one method with documented semantics is what lets a
     * caller normalise first and compare afterwards.
     *
     * @return the address, or {@code null} when the literal is not valid
     */
    public static Inet4Address resolvePosix(String literal) {
        if (literal == null || literal.isBlank()) {
            return null;
        }
        try {
            return Inet4Address.ofPosixLiteral(literal.strip());
        } catch (IllegalArgumentException rejected) {
            return null;
        }
    }

    /** True when a literal, in any POSIX form, denotes the loopback address. */
    public static boolean isLoopbackLiteral(String literal) {
        Inet4Address address = resolvePosix(literal);
        return address != null && address.isLoopbackAddress();
    }

    /** The canonical dotted-decimal form of a POSIX literal, or an empty string. */
    public static String normaliseAddress(String literal) {
        Inet4Address address = resolvePosix(literal);
        return address == null ? "" : address.getHostAddress();
    }

    /** Elapsed time between two instants. {@code Instant.until(Instant)} is Java 23. */
    public static Duration elapsed(Instant from, Instant to) {
        if (from == null || to == null) {
            return Duration.ZERO;
        }
        return from.until(to);
    }

    /** Elapsed minutes, floored at zero. */
    public static long elapsedMinutes(Instant from, Instant to) {
        Duration d = elapsed(from, to);
        return d.isNegative() ? 0L : d.toMinutes();
    }

    /**
     * The strongest alignment the segment's address is known to satisfy.
     * {@code MemorySegment.maxByteAlignment} is Java 23, on the FFM API that became final in
     * Java 22 - so this family adds to a lock the family below it introduced.
     */
    public static long maxAlignment(MemorySegment segment) {
        return segment == null ? 0L : segment.maxByteAlignment();
    }
}
