package com.pramora.testable.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a SKU price list out of a zip archive.
 *
 * <p>Carries the second half of the Java 13 version lock:
 * {@code FileSystems.newFileSystem(Path, Map)} was added in Java 13 (JDK-8218875).
 * Before it, opening a zip as a filesystem meant either the three-argument overload that
 * also takes a {@code ClassLoader}, or building a {@code jar:file:} URI by hand - which
 * is why the Path-plus-options form was added.
 *
 * <p>Archive format: one entry, {@code prices.csv}, lines of {@code SKU,cents}. Blank
 * lines and lines beginning with {@code #} are ignored.
 */
public final class PriceListArchive {

    /** Name of the entry read from the archive. */
    public static final String ENTRY = "prices.csv";

    private PriceListArchive() {
    }

    /**
     * Loads SKU to unit-price-in-cents from the archive at {@code archive}.
     * Returns an empty map if the archive has no {@value #ENTRY} entry.
     *
     * @throws IOException if the archive cannot be opened or read
     */
    public static Map<String, Long> load(Path archive) throws IOException {
        if (archive == null) {
            return Map.of();
        }
        Map<String, Long> prices = new HashMap<>();
        // Java 13: the (Path, Map) overload. Java 12 and below need (Path, ClassLoader)
        // or a hand-built "jar:file:..." URI.
        try (FileSystem fs = FileSystems.newFileSystem(archive, Map.of())) {
            Path entry = fs.getPath(ENTRY);
            if (!Files.exists(entry)) {
                return Map.of();
            }
            List<String> lines = Files.readAllLines(entry, StandardCharsets.UTF_8);
            for (String line : lines) {
                parseInto(line, prices);
            }
        }
        return Map.copyOf(prices);
    }

    /** Looks one SKU up, falling back to {@code fallbackCents} when it is absent. */
    public static long priceOf(Map<String, Long> prices, String sku, long fallbackCents) {
        if (prices == null) {
            return fallbackCents;
        }
        String key = InputSanitizer.sanitize(sku);
        Long found = prices.get(key);
        return found == null ? fallbackCents : found;
    }

    private static void parseInto(String line, Map<String, Long> into) {
        if (line == null) {
            return;
        }
        String trimmed = line.strip();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }
        int comma = trimmed.indexOf(',');
        if (comma <= 0 || comma == trimmed.length() - 1) {
            return;
        }
        String sku = InputSanitizer.sanitize(trimmed.substring(0, comma));
        if (sku.isEmpty()) {
            return;
        }
        try {
            long cents = Long.parseLong(trimmed.substring(comma + 1).strip());
            if (cents >= 0L) {
                into.put(sku, cents);
            }
        } catch (NumberFormatException ignored) {
            // a malformed row is skipped, not fatal - the archive is external input
        }
    }
}
