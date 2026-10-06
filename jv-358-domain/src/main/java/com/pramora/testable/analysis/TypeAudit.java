package com.pramora.testable.analysis;

import java.lang.constant.ClassDesc;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Checks whether a domain type is shaped the way the corpus assumes.
 *
 * <p>The second half of the Java 20 lock, also attribution-time:
 *
 * <ul>
 *   <li>{@code Class.accessFlags()} and {@code Field.accessFlags()}, returning
 *       {@code Set<java.lang.reflect.AccessFlag>} - both added in Java 20, along with the
 *       {@code AccessFlag} enum itself. They replace the {@code int} bitmask from
 *       {@code getModifiers()}, which conflates flags whose meaning depends on where they
 *       appear: bit {@code 0x0080} is {@code TRANSIENT} on a field and {@code VARARGS} on a
 *       method, and a bitmask cannot tell you which. {@code AccessFlag} carries its own
 *       location, so it can.</li>
 *   <li>{@code ClassDesc.ofInternalName} - builds a nominal descriptor from the internal
 *       form ({@code java/lang/String}) rather than the source form. Java 20.</li>
 * </ul>
 */
public final class TypeAudit {

    private TypeAudit() {
    }

    /** Access flags declared on the type itself. {@code Class.accessFlags} is Java 20. */
    public static Set<AccessFlag> flagsOf(Class<?> type) {
        if (type == null) {
            return Set.of();
        }
        return type.accessFlags();
    }

    /** True when the type is declared {@code final}. */
    public static boolean isFinal(Class<?> type) {
        return flagsOf(type).contains(AccessFlag.FINAL);
    }

    /** True when the type is declared {@code public}. */
    public static boolean isPublic(Class<?> type) {
        return flagsOf(type).contains(AccessFlag.PUBLIC);
    }

    /** True when the type is an interface. */
    public static boolean isInterface(Class<?> type) {
        return flagsOf(type).contains(AccessFlag.INTERFACE);
    }

    /**
     * True when every declared instance field is {@code final} - the corpus's shallow test
     * for an immutable value type. Uses {@code Field.accessFlags}, Java 20.
     */
    public static boolean hasOnlyFinalInstanceFields(Class<?> type) {
        if (type == null) {
            return false;
        }
        for (Field f : type.getDeclaredFields()) {
            Set<AccessFlag> flags = f.accessFlags();
            if (flags.contains(AccessFlag.STATIC) || f.isSynthetic()) {
                continue;
            }
            if (!flags.contains(AccessFlag.FINAL)) {
                return false;
            }
        }
        return true;
    }

    /** Names of the declared non-static fields that are not final, sorted. */
    public static Set<String> mutableFieldNames(Class<?> type) {
        Set<String> out = new TreeSet<>();
        if (type == null) {
            return out;
        }
        for (Field f : type.getDeclaredFields()) {
            Set<AccessFlag> flags = f.accessFlags();
            if (flags.contains(AccessFlag.STATIC) || f.isSynthetic()) {
                continue;
            }
            if (!flags.contains(AccessFlag.FINAL)) {
                out.add(f.getName());
            }
        }
        return out;
    }

    /**
     * The flags a field carries, restricted to the ones meaningful on a field. This is what
     * {@code getModifiers()} could not express: the same bit means different things in
     * different locations, and {@code AccessFlag} knows which location it belongs to.
     */
    public static Set<AccessFlag> fieldFlags(Class<?> type, String fieldName) {
        if (type == null || fieldName == null) {
            return Set.of();
        }
        for (Field f : type.getDeclaredFields()) {
            if (f.getName().equals(fieldName)) {
                Set<AccessFlag> flags = f.accessFlags();
                return flags.isEmpty() ? Set.of() : EnumSet.copyOf(flags);
            }
        }
        return Set.of();
    }

    /**
     * Nominal descriptor from a JVM internal name.
     * {@code ClassDesc.ofInternalName} is Java 20.
     */
    public static ClassDesc descriptorOf(String internalName) {
        if (internalName == null || internalName.isBlank()) {
            return null;
        }
        try {
            return ClassDesc.ofInternalName(internalName.strip());
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }

    /** The descriptor string for an internal name, or an empty string when invalid. */
    public static String descriptorString(String internalName) {
        ClassDesc desc = descriptorOf(internalName);
        return desc == null ? "" : desc.descriptorString();
    }
}
