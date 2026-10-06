package com.pramora.testable.analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.LinkedHashSet;

import com.pramora.testable.model.RouteEvent;

/**
 * An ordered log of route events.
 *
 * <p>Carries the <b>Java 21</b> attribution-time lock: <b>sequenced collections</b>
 * (JEP 431). Before Java 21 there was no common type for "a collection with a defined
 * encounter order" - {@code List} had {@code get(0)}, {@code Deque} had
 * {@code getFirst()}, {@code LinkedHashSet} had neither, and reversing meant copying. Java 21
 * added {@link SequencedCollection}, {@link SequencedSet} and {@link SequencedMap} with
 * {@code getFirst}, {@code getLast}, {@code addFirst}, {@code addLast}, {@code removeFirst},
 * {@code removeLast} and {@code reversed}, and retrofitted them onto the existing types.
 *
 * <p>{@code reversed()} returns a <b>view</b>, not a copy, which is what makes it worth
 * using and also what makes it worth testing: mutating the log is visible through a reversed
 * view taken earlier.
 *
 * <p>This file holds only attribution-time locks; the family's Java 21 syntax lock is in
 * {@code analysis/EventNarrator.java}.
 */
public final class RouteLog {

    private final List<RouteEvent> events = new ArrayList<>();

    /** Appends an event. */
    public RouteLog record(RouteEvent event) {
        if (event != null) {
            events.add(event);
        }
        return this;
    }

    /** Number of events logged. */
    public int size() {
        return events.size();
    }

    /** True when nothing has been logged. */
    public boolean isEmpty() {
        return events.isEmpty();
    }

    /** The first event, or null. {@code List.getFirst} is Java 21. */
    public RouteEvent first() {
        return events.isEmpty() ? null : events.getFirst();
    }

    /** The most recent event, or null. {@code List.getLast} is Java 21. */
    public RouteEvent latest() {
        return events.isEmpty() ? null : events.getLast();
    }

    /**
     * A reversed <em>view</em> of the log, most recent first. {@code List.reversed} is
     * Java 21 and does not copy - later writes to the log show through it.
     */
    public SequencedCollection<RouteEvent> mostRecentFirst() {
        return events.reversed();
    }

    /** Drops the oldest event and returns it. {@code List.removeFirst} is Java 21. */
    public RouteEvent dropOldest() {
        return events.isEmpty() ? null : events.removeFirst();
    }

    /** Drops the newest event and returns it. {@code List.removeLast} is Java 21. */
    public RouteEvent dropNewest() {
        return events.isEmpty() ? null : events.removeLast();
    }

    /** Inserts an event at the front. {@code List.addFirst} is Java 21. */
    public RouteLog recordFirst(RouteEvent event) {
        if (event != null) {
            events.addFirst(event);
        }
        return this;
    }

    /**
     * The distinct SKUs seen, in first-seen order.
     * {@code LinkedHashSet} is a {@link SequencedSet} from Java 21, so it has
     * {@code getFirst}/{@code getLast} without a copy.
     */
    public SequencedSet<String> skusSeen() {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (RouteEvent e : events) {
            out.add(e.leg().sku());
        }
        return out;
    }

    /** The SKU of the earliest event, or an empty string. */
    public String firstSku() {
        SequencedSet<String> skus = skusSeen();
        return skus.isEmpty() ? "" : skus.getFirst();
    }

    /**
     * Event kinds counted, in first-seen order.
     * {@code LinkedHashMap} is a {@link SequencedMap} from Java 21.
     */
    public SequencedMap<String, Integer> kindCounts() {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        for (RouteEvent e : events) {
            counts.merge(e.kind(), 1, Integer::sum);
        }
        return counts;
    }

    /** The first entry of the kind counts, or null. {@code SequencedMap.firstEntry} is Java 21. */
    public java.util.Map.Entry<String, Integer> firstKind() {
        SequencedMap<String, Integer> counts = kindCounts();
        return counts.isEmpty() ? null : counts.firstEntry();
    }
}
