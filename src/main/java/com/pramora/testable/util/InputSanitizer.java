package com.pramora.testable.util;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Normalises externally supplied identifiers. The taint fixture: untrusted input
 * reaches sanitize, which is the sink guard. Uses String.strip, String.isBlank and
 * Predicate.not - all added in Java 11.
 */
public final class InputSanitizer {

    private static final int MAX_LENGTH = 64;

    private InputSanitizer() {
    }

    public static String sanitize(String raw) {
        return Optional.ofNullable(raw)
                .map(String::strip)
                .filter(Predicate.not(String::isBlank))
                .map(s -> s.length() > MAX_LENGTH ? s.substring(0, MAX_LENGTH) : s)
                .map(InputSanitizer::stripUnsafe)
                .orElse("");
    }

    private static String stripUnsafe(String value) {
        var out = new StringBuilder(value.length());
        value.chars()
                .filter(c -> Character.isLetterOrDigit(c) || c == '-' || c == '_')
                .forEach(c -> out.append((char) c));
        return out.toString();
    }

    public static boolean isSafe(String candidate) {
        return candidate != null && candidate.equals(sanitize(candidate));
    }

    /** String.repeat is Java 11. */
    public static String mask(String value) {
        var safe = sanitize(value);
        return safe.isEmpty() ? "" : safe.charAt(0) + "*".repeat(Math.max(0, safe.length() - 1));
    }
}
