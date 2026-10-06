package com.pramora.testable.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;

import com.pramora.testable.model.RouteEvent;
import com.pramora.testable.model.ShipmentLeg;

/**
 * Inspects the outcome of dispatch tasks that have already finished.
 *
 * <p>This file carries the whole of the Java 19 lock, and it is entirely
 * <b>attribution-time</b>, because <b>Java 19 added no final language syntax</b> - the second
 * consecutive family for which that is true, after Java 18. Everything Java 19 is remembered
 * for was still provisional in it: virtual threads were a preview (JEP 425), pattern matching
 * for {@code switch} was in its <em>third</em> preview (JEP 427), record patterns were a
 * preview (JEP 405), and the Foreign Function and Memory API was a preview (JEP 424). All of
 * them landed final in Java 21, so under the corpus's preview-off rule they belong to that
 * family, and Java 19 contributes APIs only.
 *
 * <p>The three it does contribute are below:
 *
 * <ul>
 *   <li>{@code Thread.threadId()} - the replacement for {@code Thread.getId()}, which was
 *       deprecated in the same release. {@code getId()} is not final and could be overridden
 *       to lie; {@code threadId()} is final and cannot.</li>
 *   <li>{@code Future.state()}, {@code Future.resultNow()}, {@code Future.exceptionNow()} -
 *       non-blocking inspection of a task that has already completed. Before Java 19 the only
 *       way to read a finished {@code Future} was {@code get()}, which is a blocking call
 *       whose checked exceptions must be handled even when the task demonstrably finished
 *       long ago.</li>
 *   <li>{@code Locale.of(...)} - static factories replacing the {@code new Locale(...)}
 *       constructors, which were deprecated in the same release because they returned a new
 *       instance rather than an interned one.</li>
 * </ul>
 */
public final class DispatchTracker {

    /** How a finished dispatch turned out. */
    public enum Outcome {
        DELIVERED,
        FAILED,
        CANCELLED,
        STILL_RUNNING
    }

    private final Locale locale;

    public DispatchTracker() {
        // Locale.of is Java 19; new Locale(...) was deprecated in the same release.
        this(Locale.of("en", "US"));
    }

    public DispatchTracker(Locale locale) {
        this.locale = locale == null ? Locale.of("en", "US") : locale;
    }

    /** The locale this tracker formats with. */
    public Locale locale() {
        return locale;
    }

    /**
     * Classifies a finished task without blocking.
     * {@code Future.state()} is Java 19; before it this needed {@code isDone()},
     * {@code isCancelled()} and a {@code get()} in a try/catch.
     */
    public Outcome outcomeOf(Future<?> task) {
        if (task == null) {
            return Outcome.STILL_RUNNING;
        }
        Future.State state = task.state();
        if (state == Future.State.SUCCESS) {
            return Outcome.DELIVERED;
        }
        if (state == Future.State.FAILED) {
            return Outcome.FAILED;
        }
        if (state == Future.State.CANCELLED) {
            return Outcome.CANCELLED;
        }
        return Outcome.STILL_RUNNING;
    }

    /**
     * Reads the value of a task that succeeded, or returns {@code fallback}.
     * {@code Future.resultNow()} is Java 19 and does not block or throw checked exceptions.
     */
    public <T> T resultOr(Future<T> task, T fallback) {
        if (task == null || task.state() != Future.State.SUCCESS) {
            return fallback;
        }
        return task.resultNow();
    }

    /**
     * Reads the failure of a task that failed, or {@code null}.
     * {@code Future.exceptionNow()} is Java 19.
     */
    public Throwable failureOf(Future<?> task) {
        if (task == null || task.state() != Future.State.FAILED) {
            return null;
        }
        return task.exceptionNow();
    }

    /** A short reason line for a failed task. */
    public String failureReason(Future<?> task) {
        Throwable cause = failureOf(task);
        if (cause == null) {
            return "";
        }
        String message = cause.getMessage();
        return (message == null || message.isBlank())
                ? cause.getClass().getSimpleName()
                : message.strip();
    }

    /**
     * Identifies the worker that ran a dispatch.
     * {@code Thread.threadId()} is Java 19 and is final; {@code getId()} was deprecated in
     * the same release.
     */
    public String workerLabel(Thread worker) {
        if (worker == null) {
            return "unassigned";
        }
        String name = worker.getName();
        return String.format(locale, "worker-%d (%s)", worker.threadId(),
                (name == null || name.isBlank()) ? "unnamed" : name.strip());
    }

    /** The current thread's id, for the same reason. */
    public long currentWorkerId() {
        return Thread.currentThread().threadId();
    }

    /** Summarises a batch of finished dispatches, one line per task. */
    public List<String> summarise(List<ShipmentLeg> legs, List<Future<RouteEvent>> tasks) {
        List<String> out = new ArrayList<>();
        if (legs == null || tasks == null) {
            return List.of();
        }
        int n = Math.min(legs.size(), tasks.size());
        for (int i = 0; i < n; i++) {
            ShipmentLeg leg = legs.get(i);
            Future<RouteEvent> task = tasks.get(i);
            Outcome outcome = outcomeOf(task);
            String detail = outcome == Outcome.FAILED ? " - " + failureReason(task) : "";
            out.add(String.format(locale, "%s x%d: %s%s",
                    leg.sku(), leg.units(), outcome.name().toLowerCase(locale), detail));
        }
        return List.copyOf(out);
    }

    /** How many of the batch succeeded. */
    public long deliveredCount(List<Future<RouteEvent>> tasks) {
        if (tasks == null) {
            return 0L;
        }
        return tasks.stream().filter(t -> outcomeOf(t) == Outcome.DELIVERED).count();
    }
}
