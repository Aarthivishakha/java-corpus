package com.pramora.testable.util;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;

/**
 * Holds a run of shipment counters in off-heap memory.
 *
 * <p>Carries the <b>Java 22</b> attribution-time lock: the <b>Foreign Function and Memory
 * API</b> (JEP 454), final in Java 22 after incubating in 17, 18, 19 and 20 and previewing in
 * 21 and 22. Six releases from first incubator to final - the longest road of any feature in
 * this corpus, which is why the java18, java19 and java20 families all had to exclude it and
 * why it arrives here.
 *
 * <p>What it replaces matters: before it, off-heap memory meant either
 * {@code ByteBuffer.allocateDirect} - whose lifetime is decided by the garbage collector, not
 * by you, and which is capped at {@code Integer.MAX_VALUE} bytes - or {@code sun.misc.Unsafe},
 * which is neither safe nor supported. An {@link Arena} gives deterministic deallocation at
 * the close of a try-with-resources, and a confined arena additionally enforces that only the
 * owning thread may touch its segments, turning a class of use-after-free and data-race bugs
 * into exceptions.
 *
 * <p>Only memory access is used here - no native downcalls - so no
 * {@code --enable-native-access} flag is required to run these branches.
 *
 * <p>This file holds only attribution-time locks; the family's Java 22 syntax lock is in
 * {@code analysis/LegAuditor.java}.
 */
public final class NativeMetrics implements AutoCloseable {

    /** Layout of one counter. */
    public static final ValueLayout.OfLong COUNTER = ValueLayout.JAVA_LONG;

    private final Arena arena;
    private final MemorySegment segment;
    private final int capacity;

    /**
     * Allocates {@code capacity} counters in a confined arena. The arena is closed by
     * {@link #close()}, at which point the memory is released deterministically.
     */
    public NativeMetrics(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
        this.arena = Arena.ofConfined();
        SequenceLayout layout = MemoryLayout.sequenceLayout(capacity, COUNTER);
        this.segment = arena.allocate(layout);
    }

    /** How many counters this block holds. */
    public int capacity() {
        return capacity;
    }

    /** Bytes actually allocated - {@code capacity} counters of eight bytes each. */
    public long byteSize() {
        return segment.byteSize();
    }

    /** True once {@link #close()} has run. Backed by the arena's scope. */
    public boolean isClosed() {
        return !segment.scope().isAlive();
    }

    /** Reads one counter. */
    public long get(int index) {
        checkIndex(index);
        return segment.getAtIndex(COUNTER, index);
    }

    /** Writes one counter. */
    public void set(int index, long value) {
        checkIndex(index);
        segment.setAtIndex(COUNTER, index, value);
    }

    /** Adds to one counter and returns the new value. */
    public long add(int index, long delta) {
        checkIndex(index);
        long updated = segment.getAtIndex(COUNTER, index) + delta;
        segment.setAtIndex(COUNTER, index, updated);
        return updated;
    }

    /** Sum of every counter. */
    public long total() {
        long sum = 0L;
        for (int i = 0; i < capacity; i++) {
            sum += segment.getAtIndex(COUNTER, i);
        }
        return sum;
    }

    /** Zeroes every counter. */
    public void reset() {
        segment.fill((byte) 0);
    }

    /** Copies the counters onto the heap. */
    public long[] toArray() {
        return segment.toArray(COUNTER);
    }

    /** Releases the off-heap memory. Deterministic, unlike a direct ByteBuffer. */
    @Override
    public void close() {
        arena.close();
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= capacity) {
            throw new IndexOutOfBoundsException("index " + index + " outside 0.." + (capacity - 1));
        }
    }
}
