package relaypanel;

/**
 * Tracks the open/closed state of a bank of signal relays and reports
 * which ones need attention.
 */
public final class RelayPanel {

    /** Number of relays managed by a single panel. */
    private static final int RELAY_COUNT = 8;

    /** Closed state of each relay, indexed zero-based. */
    private final boolean[] closed;

    /**
     * Creates a panel with every relay open.
     */
    public RelayPanel() {
        this.closed = new boolean[RELAY_COUNT];
    }

    /**
     * Closes the relay at the given index.
     *
     * @param index the relay index, zero-based
     */
    public void close(final int index) {
        validate(index);
        closed[index] = true;
    }

    /**
     * Opens the relay at the given index.
     *
     * @param index the relay index, zero-based
     */
    public void open(final int index) {
        validate(index);
        closed[index] = false;
    }

    /**
     * Reports whether the relay at the given index is closed.
     *
     * @param index the relay index, zero-based
     * @return true if the relay is closed
     */
    public boolean isClosed(final int index) {
        validate(index);
        return closed[index];
    }

    /**
     * Counts how many relays are currently closed.
     *
     * @return the number of closed relays
     */
    public int closedCount() {
        int count = 0;
        for (final boolean state : closed) {
            if (state) {
                count++;
            }
        }
        return count;
    }

    /**
     * Validates that the given index is a real relay index.
     *
     * @param index the relay index to check
     */
    private void validate(final int index) {
        if (index < 0 || index >= RELAY_COUNT) {
            throw new IllegalArgumentException(
                    "relay index out of range: " + index);
        }
    }
}
