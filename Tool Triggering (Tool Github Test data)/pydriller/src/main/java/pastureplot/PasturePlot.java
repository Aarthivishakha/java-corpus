package pastureplot;

import java.util.ArrayList;
import java.util.List;

/**
 * Records grazing rotations for a pasture plot.
 */
public final class PasturePlot {

    /** Rotation history, most recent entry last. */
    private final List<String> rotationLog = new ArrayList<>();

    /**
     * Logs a grazing rotation entry.
     *
     * @param entry a description of the rotation
     */
    public void logRotation(String entry) {
        rotationLog.add(entry);
    }

    /**
     * Reports how many rotations have been logged.
     *
     * @return the rotation count
     */
    public int rotationCount() {
        return rotationLog.size();
    }

    /**
     * Clears the rotation log entirely.
     */
    public void clearLog() {
        rotationLog.clear();
    }

    /**
     * Reports whether any rotations have been logged.
     *
     * @return true if the log is empty
     */
    public boolean isEmpty() {
        return rotationLog.isEmpty();
    }
}
