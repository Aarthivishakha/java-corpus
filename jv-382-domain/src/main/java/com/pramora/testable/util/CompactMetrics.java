package com.pramora.testable.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Stores utilisation ratios in half the space, and builds carrier tracking links.
 *
 * <p>Part of the Java 20 lock, which is entirely attribution-time because <b>Java 20 added
 * no final language syntax at all</b>. It is the emptiest release in this corpus: every
 * language feature it carried was preview or incubator - virtual threads (2nd preview),
 * pattern matching for {@code switch} (4th preview), record patterns (2nd preview), the
 * Foreign Function and Memory API (2nd preview), scoped values (incubator) and structured
 * concurrency (2nd incubator). Java 20 is the third consecutive API-only family, after
 * Java 18 and Java 19.
 *
 * <p>Its final API surface is small enough that finding it meant diffing the compiler's own
 * {@code ct.sym} release signatures between 19 and 20 rather than reading a feature list.
 * Two of what that turned up are used here:
 *
 * <ul>
 *   <li>{@code Float.floatToFloat16} and {@code Float.float16ToFloat} - conversion to and
 *       from IEEE 754 binary16, held in a {@code short}. Half the width of a {@code float}
 *       for values that only need about three decimal digits, which is what a utilisation
 *       ratio is. Before Java 20 this needed hand-written bit manipulation.</li>
 *   <li>{@code URL.of(URI, URLStreamHandler)} - the replacement for the {@code URL(String)}
 *       constructors, every one of which was deprecated in Java 20. The constructors parsed
 *       leniently and could produce a {@code URL} that no longer round-trips through
 *       {@code URI}; the factory goes through {@code URI} parsing first.</li>
 * </ul>
 */
public final class CompactMetrics {

    /** Half-precision encoding of zero. */
    public static final short ZERO = 0;

    private CompactMetrics() {
    }

    /**
     * Packs a ratio into a half-precision short. {@code Float.floatToFloat16} is Java 20.
     * Values outside 0.0 to 1.0 are clamped before packing.
     */
    public static short pack(float ratio) {
        float clamped = ratio < 0.0f ? 0.0f : (ratio > 1.0f ? 1.0f : ratio);
        return Float.floatToFloat16(clamped);
    }

    /** Unpacks a half-precision short. {@code Float.float16ToFloat} is Java 20. */
    public static float unpack(short packed) {
        return Float.float16ToFloat(packed);
    }

    /** Packs a used/total pair as a ratio, treating a zero total as zero utilisation. */
    public static short packUtilisation(long used, long total) {
        if (total <= 0L || used <= 0L) {
            return ZERO;
        }
        return pack((float) used / (float) total);
    }

    /** Round-trips a ratio through half precision, showing the precision actually retained. */
    public static float roundTrip(float ratio) {
        return unpack(pack(ratio));
    }

    /**
     * Absolute error introduced by the half-precision round trip. Half precision carries
     * about 3 decimal digits, so this is non-zero for most inputs and exactly zero for
     * values representable in binary16.
     */
    public static float roundTripError(float ratio) {
        float clamped = ratio < 0.0f ? 0.0f : (ratio > 1.0f ? 1.0f : ratio);
        return Math.abs(roundTrip(clamped) - clamped);
    }

    /**
     * Builds a carrier tracking URL. {@code URL.of} is Java 20; the {@code URL} constructors
     * it replaces were all deprecated in the same release.
     *
     * @return the URL, or {@code null} when the inputs do not form a valid one
     */
    public static URL trackingUrl(String carrier, String reference) {
        String safeCarrier = InputSanitizer.sanitize(carrier);
        String safeRef = InputSanitizer.sanitize(reference);
        if (safeCarrier.isEmpty() || safeRef.isEmpty()) {
            return null;
        }
        try {
            URI uri = new URI("https", "tracking.example.com",
                    "/" + safeCarrier.toLowerCase(java.util.Locale.ROOT) + "/" + safeRef,
                    null);
            return URL.of(uri, null);
        } catch (URISyntaxException | MalformedURLException failed) {
            return null;
        }
    }

    /** The tracking URL as a string, or an empty string when it cannot be built. */
    public static String trackingLink(String carrier, String reference) {
        URL url = trackingUrl(carrier, reference);
        return url == null ? "" : url.toString();
    }
}
