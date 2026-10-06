package com.pramora.testable.util;

import java.util.Optional;

/**
 * Normalises externally supplied identifiers. The taint fixture: untrusted input
 * reaches sanitize, which is the sink guard.
 */
public final class InputSanitizer {

    private static final int MAX_LENGTH = 64;

    private InputSanitizer() {
    }

    public static String sanitize(String raw) {
        return Optional.ofNullable(raw)
                .map(String::trim)
                .map(s -> s.length() > MAX_LENGTH ? s.substring(0, MAX_LENGTH) : s)
                .map(InputSanitizer::stripUnsafe)
                .orElse("");
    }

    private static String stripUnsafe(String value) {
        StringBuilder out = new StringBuilder(value.length());
        value.chars()
                .filter(c -> Character.isLetterOrDigit(c) || c == '-' || c == '_')
                .forEach(c -> out.append((char) c));
        return out.toString();
    }

    public static boolean isSafe(String candidate) {
        return candidate != null && candidate.equals(sanitize(candidate));
    }
}
