package com.pramora.testable.util;

import java.io.IOException;
import java.io.Reader;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;
import java.util.List;

/**
 * Reads what a compiled class declares, without loading it.
 *
 * <p>Carries the rest of the Java 24 lock:
 *
 * <ul>
 *   <li>the <b>Class-File API</b> (JEP 484), final in Java 24 after preview in 22 and 23. It
 *       is the JDK's own parser and writer for class files, and it replaces a genuinely bad
 *       situation: every tool that needed to read bytecode - this corpus's own SpotBugs,
 *       JaCoCo, PIT and CK among them - shipped its own copy of ASM, each pinned to a
 *       different version, each needing an update before it could read the next release's
 *       class files. That is exactly the ceiling this corpus keeps measuring: PIT stops at
 *       Java 25 because its bundled ASM does, and CK stops at Java 16 because its bundled
 *       Eclipse JDT does. A parser maintained inside the JDK cannot fall behind the JDK.</li>
 *   <li>{@code Reader.of(CharSequence)} - Java 24. Reads from any {@code CharSequence}
 *       without the copy that {@code new StringReader(cs.toString())} forces.</li>
 * </ul>
 *
 * <p>Everything here is attribution-time, because Java 24 added no final language syntax.
 */
public final class ClassFileProbe {

    private ClassFileProbe() {
    }

    /**
     * Parses class-file bytes and returns the major version they declare.
     * The Class-File API is Java 24.
     *
     * @return the major version, or 0 when the bytes are not a class file
     */
    public static int majorVersionOf(byte[] classBytes) {
        ClassModel model = parse(classBytes);
        return model == null ? 0 : model.majorVersion();
    }

    /** The class's own name in internal form, or an empty string. */
    public static String nameOf(byte[] classBytes) {
        ClassModel model = parse(classBytes);
        return model == null ? "" : model.thisClass().asInternalName();
    }

    /** The superclass descriptor, or an empty string when there is none. */
    public static String superclassOf(byte[] classBytes) {
        ClassModel model = parse(classBytes);
        if (model == null) {
            return "";
        }
        return model.superclass()
                .map(entry -> entry.asSymbol().descriptorString())
                .orElse("");
    }

    /** Number of methods the class declares. */
    public static int methodCount(byte[] classBytes) {
        ClassModel model = parse(classBytes);
        return model == null ? 0 : model.methods().size();
    }

    /** Number of fields the class declares. */
    public static int fieldCount(byte[] classBytes) {
        ClassModel model = parse(classBytes);
        return model == null ? 0 : model.fields().size();
    }

    /** Declared method names, in class-file order. */
    public static List<String> methodNames(byte[] classBytes) {
        ClassModel model = parse(classBytes);
        if (model == null) {
            return List.of();
        }
        return model.methods().stream().map(m -> m.methodName().stringValue()).toList();
    }

    /**
     * Builds a minimal class file for {@code name} and returns its bytes. The Class-File API
     * writes as well as reads, which is what a corpus that plants bytecode fixtures needs.
     */
    public static byte[] emitMinimal(String name) {
        String safe = InputSanitizer.sanitize(name);
        if (safe.isEmpty()) {
            return new byte[0];
        }
        return ClassFile.of().build(ClassDesc.of(safe), builder -> builder.withFlags(
                java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL));
    }

    /**
     * Reads a whole {@link CharSequence} through a {@link Reader}.
     * {@code Reader.of} is Java 24 and avoids the {@code toString()} copy that
     * {@code new StringReader(cs.toString())} forces on a {@code StringBuilder}.
     */
    public static String drain(CharSequence source) {
        if (source == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        try (Reader reader = Reader.of(source)) {
            char[] buffer = new char[64];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                out.append(buffer, 0, read);
            }
        } catch (IOException unreachable) {
            return "";
        }
        return out.toString();
    }

    /** Number of characters a CharSequence yields when read through a Reader. */
    public static int drainedLength(CharSequence source) {
        return drain(source).length();
    }

    private static ClassModel parse(byte[] classBytes) {
        if (classBytes == null || classBytes.length < 8) {
            return null;
        }
        try {
            return ClassFile.of().parse(classBytes);
        } catch (IllegalArgumentException notAClassFile) {
            return null;
        }
    }
}
