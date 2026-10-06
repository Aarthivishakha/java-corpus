package com.pramora.testable.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.pramora.testable.model.OrderLine;

/** Covers the Java 13 absolute bulk buffer transfers carried by {@link OrderCodec}. */
public class OrderCodecTest {

    private OrderLine line() {
        return new OrderLine("SKU-1234", 7, 4999L);
    }

    @Test
    public void recordIsFixedWidth() {
        assertEquals(28, OrderCodec.RECORD_BYTES);
        assertEquals(OrderCodec.RECORD_BYTES, OrderCodec.encode(line()).length);
    }

    @Test
    public void roundTripPreservesEveryField() {
        OrderLine back = OrderCodec.decode(OrderCodec.encode(line()));
        assertEquals("SKU-1234", back.getSku());
        assertEquals(7, back.getQuantity());
        assertEquals(4999L, back.getUnitPriceCents());
    }

    @Test
    public void roundTripPreservesTheExtendedAmount() {
        assertEquals(line().extendedCents(),
                OrderCodec.decode(OrderCodec.encode(line())).extendedCents());
    }

    @Test
    public void peekReadsTheSkuWithoutDecodingTheRest() {
        assertEquals("SKU-1234", OrderCodec.peekSku(OrderCodec.encode(line())));
    }

    @Test
    public void peekLeavesTheRecordReadableAgain() {
        byte[] record = OrderCodec.encode(line());
        OrderCodec.peekSku(record);
        OrderCodec.peekSku(record);
        assertEquals("SKU-1234", OrderCodec.decode(record).getSku());
    }

    @Test
    public void longSkuIsTruncatedToTheFieldWidth() {
        OrderLine wide = new OrderLine("SKU-0123456789ABCDEFGH", 1, 100L);
        assertEquals(OrderCodec.SKU_BYTES, OrderCodec.peekSku(OrderCodec.encode(wide)).length());
    }

    @Test
    public void skuIsSanitisedOnTheWayIn() {
        OrderLine dirty = new OrderLine("SKU 1<script>", 1, 100L);
        assertEquals("SKU1script", OrderCodec.peekSku(OrderCodec.encode(dirty)));
    }

    @Test
    public void peekOfAShortRecordIsEmptyRatherThanThrowing() {
        assertEquals("", OrderCodec.peekSku(new byte[3]));
        assertEquals("", OrderCodec.peekSku(null));
    }

    @Test
    public void decodeRejectsAShortRecord() {
        try {
            OrderCodec.decode(new byte[5]);
            fail("expected a short record to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("28"));
        }
    }

    @Test
    public void encodeRejectsNull() {
        try {
            OrderCodec.encode(null);
            fail("expected null to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("line is required", expected.getMessage());
        }
    }

    @Test
    public void headerReadsAFixedWidthPrefix() {
        assertEquals("ORDER", OrderCodec.header("ORDER-2026-01", 5));
    }

    @Test
    public void headerClampsToTheAvailableLength() {
        assertEquals("AB", OrderCodec.header("AB", 40));
    }

    @Test
    public void headerOfNullOrZeroWidthIsEmpty() {
        assertEquals("", OrderCodec.header(null, 4));
        assertEquals("", OrderCodec.header("ABC", 0));
    }
}
