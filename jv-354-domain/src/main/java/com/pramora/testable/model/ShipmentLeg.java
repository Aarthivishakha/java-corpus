package com.pramora.testable.model;

import com.pramora.testable.util.InputSanitizer;

/**
 * One leg of a shipment: some units of a SKU moving with one carrier.
 *
 * <p>This is a <b>record</b>, and records became final in Java 16 (JEP 395) after preview
 * in 14 (JEP 359) and a second preview in 15 (JEP 384). Under the corpus's preview-off rule
 * neither of those families could use them, which is why records arrive here and not
 * earlier.
 *
 * <p>The declaration below uses a <b>compact canonical constructor</b> - the form with no
 * parameter list, where assignments to the implicit parameters are written before the
 * implicit field assignment happens. It is the record-specific place to normalise and
 * validate, and it exists only in record declarations, so it is a syntax lock on top of the
 * record itself.
 *
 * <p>Note what is deliberately <em>not</em> here: no sealed hierarchy. Sealed types were in
 * their second preview in Java 16 and only became final in Java 17, so they belong to that
 * family.
 */
public record ShipmentLeg(String sku, int units, String carrier) {

    /** Carrier used when none is supplied. */
    public static final String DEFAULT_CARRIER = "GROUND";

    /** Units above which a leg counts as bulk. */
    public static final int BULK_UNITS = 20;

    /**
     * Compact canonical constructor - Java 16. Normalises the SKU and carrier and rejects a
     * non-positive unit count. The implicit field assignment happens after this body runs,
     * so assigning to the parameter names is what changes the stored state.
     */
    public ShipmentLeg {
        sku = InputSanitizer.sanitize(sku);
        if (sku.isEmpty()) {
            throw new IllegalArgumentException("sku is required");
        }
        if (units <= 0) {
            throw new IllegalArgumentException("units must be positive");
        }
        carrier = (carrier == null || carrier.isBlank())
                ? DEFAULT_CARRIER
                : carrier.strip().toUpperCase(java.util.Locale.ROOT);
    }

    /** Builds a single leg covering an entire order line. */
    public static ShipmentLeg of(OrderLine line) {
        if (line == null) {
            throw new IllegalArgumentException("line is required");
        }
        return new ShipmentLeg(line.getSku(), line.getQuantity(), DEFAULT_CARRIER);
    }

    /** Builds a leg with an explicit carrier. */
    public static ShipmentLeg of(OrderLine line, String carrier) {
        if (line == null) {
            throw new IllegalArgumentException("line is required");
        }
        return new ShipmentLeg(line.getSku(), line.getQuantity(), carrier);
    }

    /** True when this leg is large enough to route as bulk freight. */
    public boolean isBulk() {
        return units >= BULK_UNITS;
    }

    /** Splits this leg into legs of at most {@code maxUnits} units each. */
    public java.util.List<ShipmentLeg> split(int maxUnits) {
        if (maxUnits <= 0 || units <= maxUnits) {
            return java.util.List.of(this);
        }
        java.util.List<ShipmentLeg> out = new java.util.ArrayList<>();
        int remaining = units;
        while (remaining > 0) {
            int take = Math.min(maxUnits, remaining);
            out.add(new ShipmentLeg(sku, take, carrier));
            remaining -= take;
        }
        return java.util.List.copyOf(out);
    }
}
