package com.pramora.testable.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.junit.Test;

import com.pramora.testable.model.RouteEvent;
import com.pramora.testable.model.ShipmentLeg;

/** Covers the Java 19 Thread, Future and Locale additions in {@link DispatchTracker}. */
public class DispatchTrackerTest {

    private final DispatchTracker tracker = new DispatchTracker();

    private ShipmentLeg leg(String sku, int units) {
        return new ShipmentLeg(sku, units, "GROUND");
    }

    private Future<RouteEvent> delivered(String sku) {
        return CompletableFuture.completedFuture(
                new RouteEvent.Delivered(leg(sku, 1), 42L));
    }

    private Future<RouteEvent> failed(String message) {
        return CompletableFuture.failedFuture(new IllegalStateException(message));
    }

    private Future<RouteEvent> cancelled() {
        CompletableFuture<RouteEvent> f = new CompletableFuture<>();
        f.cancel(true);
        return f;
    }

    private Future<RouteEvent> running() {
        return new CompletableFuture<>();
    }

    // ---- Locale.of ----

    @Test
    public void defaultLocaleIsBuiltWithTheFactory() {
        assertEquals(Locale.of("en", "US"), tracker.locale());
    }

    @Test
    public void nullLocaleFallsBack() {
        assertEquals(Locale.of("en", "US"), new DispatchTracker(null).locale());
    }

    @Test
    public void explicitLocaleIsKept() {
        assertEquals(Locale.of("fr", "FR"), new DispatchTracker(Locale.of("fr", "FR")).locale());
    }

    // ---- Future.state ----

    @Test
    public void everyFutureStateMapsToAnOutcome() {
        assertEquals(DispatchTracker.Outcome.DELIVERED, tracker.outcomeOf(delivered("SKU-1")));
        assertEquals(DispatchTracker.Outcome.FAILED, tracker.outcomeOf(failed("no capacity")));
        assertEquals(DispatchTracker.Outcome.CANCELLED, tracker.outcomeOf(cancelled()));
        assertEquals(DispatchTracker.Outcome.STILL_RUNNING, tracker.outcomeOf(running()));
        assertEquals(DispatchTracker.Outcome.STILL_RUNNING, tracker.outcomeOf(null));
    }

    // ---- Future.resultNow ----

    @Test
    public void resultIsReadWithoutBlocking() {
        RouteEvent event = tracker.resultOr(delivered("SKU-7"), null);
        assertNotNull(event);
        assertEquals("SKU-7", event.leg().sku());
        assertTrue(event.isTerminal());
    }

    @Test
    public void resultFallsBackForEveryNonSuccessState() {
        RouteEvent fallback = new RouteEvent.Planned(leg("SKU-X", 1));
        assertEquals(fallback, tracker.resultOr(failed("x"), fallback));
        assertEquals(fallback, tracker.resultOr(cancelled(), fallback));
        assertEquals(fallback, tracker.resultOr(running(), fallback));
        assertEquals(fallback, tracker.resultOr(null, fallback));
    }

    // ---- Future.exceptionNow ----

    @Test
    public void failureIsReadWithoutBlocking() {
        Throwable cause = tracker.failureOf(failed("no capacity"));
        assertNotNull(cause);
        assertEquals("no capacity", cause.getMessage());
        assertTrue(cause instanceof IllegalStateException);
    }

    @Test
    public void failureIsNullForEveryNonFailedState() {
        assertNull(tracker.failureOf(delivered("SKU-1")));
        assertNull(tracker.failureOf(cancelled()));
        assertNull(tracker.failureOf(running()));
        assertNull(tracker.failureOf(null));
    }

    @Test
    public void failureReasonUsesTheMessageWhenThereIsOne() {
        assertEquals("no capacity", tracker.failureReason(failed("no capacity")));
        assertEquals("no capacity", tracker.failureReason(failed("  no capacity  ")));
    }

    @Test
    public void failureReasonFallsBackToTheTypeName() {
        assertEquals("IllegalStateException", tracker.failureReason(failed(null)));
        assertEquals("IllegalStateException", tracker.failureReason(failed("   ")));
        assertEquals("", tracker.failureReason(delivered("SKU-1")));
    }

    // ---- Thread.threadId ----

    @Test
    public void workerLabelCarriesTheThreadId() {
        Thread current = Thread.currentThread();
        String label = tracker.workerLabel(current);
        assertTrue(label, label.startsWith("worker-" + current.threadId() + " ("));
        assertTrue(label, label.endsWith(")"));
    }

    @Test
    public void workerLabelHandlesAnUnnamedThreadAndNull() {
        Thread unnamed = new Thread(() -> { }, "   ");
        assertTrue(tracker.workerLabel(unnamed).endsWith("(unnamed)"));
        assertEquals("unassigned", tracker.workerLabel(null));
    }

    @Test
    public void currentWorkerIdMatchesTheCurrentThread() {
        assertEquals(Thread.currentThread().threadId(), tracker.currentWorkerId());
        assertTrue(tracker.currentWorkerId() > 0L);
    }

    // ---- batch summary ----

    @Test
    public void summaryHasOneLinePerTask() {
        List<ShipmentLeg> legs = List.of(leg("SKU-1", 2), leg("SKU-2", 5), leg("SKU-3", 1));
        List<Future<RouteEvent>> tasks =
                List.of(delivered("SKU-1"), failed("no capacity"), cancelled());
        List<String> lines = tracker.summarise(legs, tasks);
        assertEquals(3, lines.size());
        assertEquals("SKU-1 x2: delivered", lines.get(0));
        assertEquals("SKU-2 x5: failed - no capacity", lines.get(1));
        assertEquals("SKU-3 x1: cancelled", lines.get(2));
    }

    @Test
    public void summaryStopsAtTheShorterList() {
        assertEquals(1, tracker.summarise(List.of(leg("SKU-1", 1)),
                List.of(delivered("SKU-1"), delivered("SKU-2"))).size());
        assertTrue(tracker.summarise(null, List.of()).isEmpty());
        assertTrue(tracker.summarise(List.of(), null).isEmpty());
    }

    @Test
    public void summaryIsUnmodifiable() {
        List<String> lines = tracker.summarise(List.of(leg("SKU-1", 1)), List.of(delivered("SKU-1")));
        try {
            lines.add("x");
            org.junit.Assert.fail("expected the summary to be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            assertEquals(1, lines.size());
        }
    }

    @Test
    public void deliveredCountOnlyCountsSuccesses() {
        assertEquals(2L, tracker.deliveredCount(
                List.of(delivered("A"), failed("x"), delivered("B"), cancelled(), running())));
        assertEquals(0L, tracker.deliveredCount(List.of()));
        assertEquals(0L, tracker.deliveredCount(null));
    }
}
