package com.pramora.testable.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

/** Covers {@link PricingSnapshot}, which carries the Java 14 {@code @Serial} API lock. */
public class PricingSnapshotTest {

    private PricingSnapshot snapshot() {
        return new PricingSnapshot("O-14", 10000L, 1500L, 9299L, 35);
    }

    private static byte[] serialise(Object o) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(o);
        }
        return bytes.toByteArray();
    }

    private static Object deserialise(byte[] raw) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(raw))) {
            return in.readObject();
        }
    }

    @Test
    public void roundTripsThroughSerialization() throws Exception {
        PricingSnapshot back = (PricingSnapshot) deserialise(serialise(snapshot()));
        assertEquals(snapshot(), back);
        assertEquals("O-14", back.getOrderId());
        assertEquals(9299L, back.getTotalCents());
        assertEquals(35, back.getRiskScore());
    }

    @Test
    public void derivedFiguresSurviveTheRoundTrip() throws Exception {
        PricingSnapshot back = (PricingSnapshot) deserialise(serialise(snapshot()));
        assertEquals(8500L, back.netCents());
        assertEquals(799L, back.shippingCents());
    }

    @Test
    public void netIsFlooredAtZero() {
        assertEquals(0L, new PricingSnapshot("O-1", 100L, 500L, 0L, 0).netCents());
    }

    @Test
    public void shippingIsFlooredAtZero() {
        assertEquals(0L, new PricingSnapshot("O-1", 1000L, 0L, 500L, 0).shippingCents());
    }

    @Test
    public void equalityCoversEveryField() {
        assertEquals(snapshot(), snapshot());
        assertEquals(snapshot().hashCode(), snapshot().hashCode());
        assertNotEquals(snapshot(), new PricingSnapshot("O-15", 10000L, 1500L, 9299L, 35));
        assertNotEquals(snapshot(), new PricingSnapshot("O-14", 10001L, 1500L, 9299L, 35));
        assertNotEquals(snapshot(), new PricingSnapshot("O-14", 10000L, 1501L, 9299L, 35));
        assertNotEquals(snapshot(), new PricingSnapshot("O-14", 10000L, 1500L, 9300L, 35));
        assertNotEquals(snapshot(), new PricingSnapshot("O-14", 10000L, 1500L, 9299L, 36));
    }

    @Test
    public void equalsHandlesNullAndForeignTypes() {
        assertFalse(snapshot().equals(null));
        assertFalse(snapshot().equals("O-14"));
        assertTrue(snapshot().equals(snapshot()));
    }

    @Test
    public void toStringNamesTheOrder() {
        assertTrue(snapshot().toString().contains("O-14"));
    }

    @Test
    public void orderIdIsRequired() {
        try {
            new PricingSnapshot("", 1L, 0L, 1L, 0);
            fail("expected an empty order id to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("order id is required", expected.getMessage());
        }
        try {
            new PricingSnapshot(null, 1L, 0L, 1L, 0);
            fail("expected a null order id to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("order id is required", expected.getMessage());
        }
    }

    @Test
    public void serialVersionUidIsPinned() throws Exception {
        java.lang.reflect.Field f = PricingSnapshot.class.getDeclaredField("serialVersionUID");
        f.setAccessible(true);
        assertEquals(20260901L, f.getLong(null));
    }
}
