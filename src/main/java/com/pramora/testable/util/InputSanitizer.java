package com.pramora.testable.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Normalises externally supplied identifiers. The taint fixture: untrusted input
 * reaches sanitize, which is the sink guard.
 *
 * The try-with-resources block below names an already-declared effectively-final
 * variable rather than declaring one inside the parentheses - Java 9 syntax, rejected
 * under -source 8.
 */
public final class InputSanitizer {

    private static final int MAX_LENGTH = 64;

    private InputSanitizer() {
    }

    public static String sanitize(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.length() > MAX_LENGTH) {
            trimmed = trimmed.substring(0, MAX_LENGTH);
        }
        StringBuilder out = new StringBuilder(trimmed.length());
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
                out.append(c);
            }
        }
        return out.toString();
    }

    /** Reads and sanitises from a reader, using the Java 9 try-with-resources form. */
    public static String sanitizeStream(String raw) throws IOException {
        Reader reader = new StringReader(raw == null ? "" : raw);
        StringBuilder buffer = new StringBuilder();
        try (reader) {
            int ch;
            while ((ch = reader.read()) != -1) {
                buffer.append((char) ch);
            }
        }
        return sanitize(buffer.toString());
    }

    public static boolean isSafe(String candidate) {
        return candidate != null && candidate.equals(sanitize(candidate));
    }
}
