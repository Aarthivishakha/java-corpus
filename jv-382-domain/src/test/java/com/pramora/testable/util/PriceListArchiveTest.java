package com.pramora.testable.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Covers {@link PriceListArchive}, which carries the Java 13
 * {@code FileSystems.newFileSystem(Path, Map)} overload.
 */
public class PriceListArchiveTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private Path archiveWith(String csv) throws IOException {
        return archiveWith(PriceListArchive.ENTRY, csv);
    }

    private Path archiveWith(String entryName, String csv) throws IOException {
        Path zip = folder.newFile("prices-" + System.nanoTime() + ".zip").toPath();
        try (OutputStream out = Files.newOutputStream(zip);
             ZipOutputStream zos = new ZipOutputStream(out)) {
            zos.putNextEntry(new ZipEntry(entryName));
            zos.write(csv.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return zip;
    }

    @Test
    public void loadsEveryWellFormedRow() throws IOException {
        Map<String, Long> prices = PriceListArchive.load(archiveWith("SKU-1,1999\nSKU-2,4999\n"));
        assertEquals(2, prices.size());
        assertEquals(Long.valueOf(1999L), prices.get("SKU-1"));
        assertEquals(Long.valueOf(4999L), prices.get("SKU-2"));
    }

    @Test
    public void skipsCommentsAndBlankLines() throws IOException {
        Map<String, Long> prices =
                PriceListArchive.load(archiveWith("# header\n\nSKU-1,1999\n   \n"));
        assertEquals(1, prices.size());
    }

    @Test
    public void skipsMalformedRowsWithoutFailing() throws IOException {
        Map<String, Long> prices = PriceListArchive.load(
                archiveWith("SKU-1,1999\nSKU-2,notanumber\nSKU-3\n,500\nSKU-4,\nSKU-5,-1\n"));
        assertEquals(1, prices.size());
        assertTrue(prices.containsKey("SKU-1"));
    }

    @Test
    public void sanitisesSkusOnTheWayIn() throws IOException {
        Map<String, Long> prices = PriceListArchive.load(archiveWith("SKU 1<script>,250\n"));
        assertEquals(Long.valueOf(250L), prices.get("SKU1script"));
    }

    @Test
    public void returnedMapIsUnmodifiable() throws IOException {
        Map<String, Long> prices = PriceListArchive.load(archiveWith("SKU-1,1999\n"));
        try {
            prices.put("SKU-9", 1L);
            org.junit.Assert.fail("expected the map to be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            assertEquals(1, prices.size());
        }
    }

    @Test
    public void archiveWithoutThePriceEntryLoadsEmpty() throws IOException {
        assertTrue(PriceListArchive.load(archiveWith("other.csv", "SKU-1,1999\n")).isEmpty());
    }

    @Test
    public void nullArchiveLoadsEmpty() throws IOException {
        assertTrue(PriceListArchive.load(null).isEmpty());
    }

    @Test
    public void priceOfFallsBackWhenTheSkuIsAbsent() throws IOException {
        Map<String, Long> prices = PriceListArchive.load(archiveWith("SKU-1,1999\n"));
        assertEquals(1999L, PriceListArchive.priceOf(prices, "SKU-1", 7L));
        assertEquals(7L, PriceListArchive.priceOf(prices, "SKU-X", 7L));
        assertEquals(7L, PriceListArchive.priceOf(null, "SKU-1", 7L));
    }

    @Test
    public void priceOfSanitisesTheLookupKey() throws IOException {
        Map<String, Long> prices = PriceListArchive.load(archiveWith("SKU-1,1999\n"));
        assertEquals(1999L, PriceListArchive.priceOf(prices, " SKU-1 ", 7L));
    }
}
