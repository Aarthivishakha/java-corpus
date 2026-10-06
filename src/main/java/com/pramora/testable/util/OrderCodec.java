package com.pramora.testable.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import com.pramora.testable.model.OrderLine;

/**
 * Encodes an {@link OrderLine} to a fixed-width binary record and back.
 *
 * <p>This class carries part of the Java 13 version lock. The absolute bulk
 * {@code get(int, byte[])} and {@code put(int, byte[])} methods on {@code ByteBuffer},
 * and the matching {@code get(int, char[])} on {@code CharBuffer}, were added in Java 13
 * (JDK-5029431). Before that, a bulk transfer at a known offset meant mutating the
 * buffer's position - {@code buf.position(off); buf.get(dst);} - which is not
 * thread-safe on a shared buffer and is the reason the absolute forms were added.
 *
 * <p>Java 13 contributed <em>no final language syntax</em>: text blocks were a preview
 * feature in 13 (final in 15) and switch expressions were still preview in 13 (final in
 * 14). Under the corpus rule that preview features stay off, the Java 13 lock is
 * therefore entirely an API lock, as Java 12's was.
 *
 * <p>Record layout, {@value #RECORD_BYTES} bytes:
 * <pre>
 *   [0  .. 16)  sku, US-ASCII, space-padded
 *   [16 .. 20)  quantity, big-endian int
 *   [20 .. 28)  unit price in cents, big-endian long
 * </pre>
 */
public final class OrderCodec {

    /** Width of the SKU field in bytes. */
    public static final int SKU_BYTES = 16;
    /** Total width of one encoded record. */
    public static final int RECORD_BYTES = SKU_BYTES + Integer.BYTES + Long.BYTES;

    private static final int QTY_OFFSET = SKU_BYTES;
    private static final int PRICE_OFFSET = SKU_BYTES + Integer.BYTES;
    private static final byte PAD = (byte) ' ';

    private OrderCodec() {
    }

    /** Encodes one line. Uses the Java 13 absolute bulk {@code put(int, byte[])}. */
    public static byte[] encode(OrderLine line) {
        if (line == null) {
            throw new IllegalArgumentException("line is required");
        }
        ByteBuffer buf = ByteBuffer.allocate(RECORD_BYTES);
        buf.put(0, padded(line.getSku()));
        buf.putInt(QTY_OFFSET, line.getQuantity());
        buf.putLong(PRICE_OFFSET, line.getUnitPriceCents());
        return buf.array();
    }

    /** Decodes one line. Uses the Java 13 absolute bulk {@code get(int, byte[])}. */
    public static OrderLine decode(byte[] record) {
        if (record == null || record.length < RECORD_BYTES) {
            throw new IllegalArgumentException("record must be at least " + RECORD_BYTES + " bytes");
        }
        ByteBuffer buf = ByteBuffer.wrap(record);
        byte[] sku = new byte[SKU_BYTES];
        buf.get(0, sku);
        return new OrderLine(new String(sku, StandardCharsets.US_ASCII).trim(),
                buf.getInt(QTY_OFFSET),
                buf.getLong(PRICE_OFFSET));
    }

    /**
     * Reads the SKU field out of a record without decoding the rest of it.
     * The absolute bulk get leaves the buffer's position untouched, so the same buffer
     * can be read concurrently - the point of the Java 13 addition.
     */
    public static String peekSku(byte[] record) {
        if (record == null || record.length < SKU_BYTES) {
            return "";
        }
        byte[] sku = new byte[SKU_BYTES];
        ByteBuffer.wrap(record).get(0, sku);
        return new String(sku, StandardCharsets.US_ASCII).trim();
    }

    /**
     * Reads a fixed-width header prefix from a character buffer.
     * {@code CharBuffer.get(int, char[])} is the Java 13 absolute bulk char transfer.
     */
    public static String header(CharSequence source, int width) {
        if (source == null || width <= 0) {
            return "";
        }
        CharBuffer buf = CharBuffer.wrap(source);
        int n = Math.min(width, buf.remaining());
        char[] out = new char[n];
        buf.get(0, out);
        return new String(out);
    }

    private static byte[] padded(String sku) {
        byte[] raw = InputSanitizer.sanitize(sku).getBytes(StandardCharsets.US_ASCII);
        byte[] out = new byte[SKU_BYTES];
        java.util.Arrays.fill(out, PAD);
        System.arraycopy(raw, 0, out, 0, Math.min(raw.length, SKU_BYTES));
        return out;
    }
}
