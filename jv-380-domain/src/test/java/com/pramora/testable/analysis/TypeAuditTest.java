package com.pramora.testable.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.AccessFlag;
import java.util.Set;

import org.junit.Test;

import com.pramora.testable.model.Order;
import com.pramora.testable.model.OrderLine;
import com.pramora.testable.model.Priceable;
import com.pramora.testable.model.RouteEvent;
import com.pramora.testable.model.ShipmentLeg;

/** Covers the Java 20 AccessFlag and ClassDesc additions in {@link TypeAudit}. */
public class TypeAuditTest {

    @Test
    public void typeFlagsAreReadFromTheClass() {
        assertTrue(TypeAudit.isPublic(OrderLine.class));
        assertTrue(TypeAudit.isFinal(ShipmentLeg.class));
        assertFalse(TypeAudit.isFinal(Order.class));
    }

    @Test
    public void interfacesAreIdentified() {
        assertTrue(TypeAudit.isInterface(Priceable.class));
        assertTrue(TypeAudit.isInterface(RouteEvent.class));
        assertFalse(TypeAudit.isInterface(OrderLine.class));
    }

    @Test
    public void flagsOfNullIsEmpty() {
        assertTrue(TypeAudit.flagsOf(null).isEmpty());
        assertFalse(TypeAudit.isFinal(null));
        assertFalse(TypeAudit.isPublic(null));
        assertFalse(TypeAudit.isInterface(null));
    }

    @Test
    public void valueTypesHaveOnlyFinalInstanceFields() {
        assertTrue(TypeAudit.hasOnlyFinalInstanceFields(OrderLine.class));
        assertTrue(TypeAudit.hasOnlyFinalInstanceFields(ShipmentLeg.class));
    }

    @Test
    public void mutableStateIsDetected() {
        // Order carries a settable status, so it is not a value type
        assertFalse(TypeAudit.hasOnlyFinalInstanceFields(Order.class));
        assertEquals(Set.of("status"), TypeAudit.mutableFieldNames(Order.class));
    }

    @Test
    public void valueTypesReportNoMutableFields() {
        assertTrue(TypeAudit.mutableFieldNames(OrderLine.class).isEmpty());
        assertTrue(TypeAudit.mutableFieldNames(ShipmentLeg.class).isEmpty());
    }

    @Test
    public void nullTypeIsHandled() {
        assertFalse(TypeAudit.hasOnlyFinalInstanceFields(null));
        assertTrue(TypeAudit.mutableFieldNames(null).isEmpty());
    }

    @Test
    public void fieldFlagsCarryTheirOwnLocation() {
        Set<AccessFlag> flags = TypeAudit.fieldFlags(OrderLine.class, "sku");
        assertTrue(flags.contains(AccessFlag.PRIVATE));
        assertTrue(flags.contains(AccessFlag.FINAL));
        // every flag returned is one that is legal on a field - the point of AccessFlag
        for (AccessFlag f : flags) {
            assertTrue(f + " should be valid on a field",
                    f.locations().contains(AccessFlag.Location.FIELD));
        }
    }

    @Test
    public void unknownFieldGivesNoFlags() {
        assertTrue(TypeAudit.fieldFlags(OrderLine.class, "nosuchfield").isEmpty());
        assertTrue(TypeAudit.fieldFlags(null, "sku").isEmpty());
        assertTrue(TypeAudit.fieldFlags(OrderLine.class, null).isEmpty());
    }

    @Test
    public void descriptorsAreBuiltFromInternalNames() {
        assertEquals("Ljava/lang/String;", TypeAudit.descriptorString("java/lang/String"));
        assertEquals("Lcom/pramora/testable/model/OrderLine;",
                TypeAudit.descriptorString("com/pramora/testable/model/OrderLine"));
        assertEquals("Ljava/lang/String;", TypeAudit.descriptorString("  java/lang/String  "));
    }

    @Test
    public void invalidInternalNamesGiveNoDescriptor() {
        assertNull(TypeAudit.descriptorOf(null));
        assertNull(TypeAudit.descriptorOf("   "));
        assertNull(TypeAudit.descriptorOf("java.lang.String"));
        assertEquals("", TypeAudit.descriptorString("java.lang.String"));
    }
}
