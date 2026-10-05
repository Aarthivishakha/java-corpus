package furnacegauge;

/**
 * Watches a furnace's temperature reading and classifies it into a
 * heat-treatment stage, raising an alarm outside the safe operating band.
 */
public final class FurnaceGauge {

    private static final int LOW_BAND = 200;
    private static final int TEMPER_BAND = 950;
    private static final int ANNEAL_BAND = 1300;
    private static final int MAX_SAFE = 1350;

    /**
     * Classifies a temperature reading (in Celsius) into a treatment stage.
     *
     * @param celsius the current furnace temperature
     * @return a short stage label
     */
    public String stageFor(int celsius) {
        if (celsius > MAX_SAFE) {
            throw new IllegalStateException("over-temperature: " + celsius);
        }
        if (celsius < LOW_BAND) {
            return "idle";
        } else if (celsius < TEMPER_BAND) {
            return "temper";
        } else if (celsius < ANNEAL_BAND) {
            return "anneal";
        } else {
            return "peak";
        }
    }

    /**
     * Reports whether the given reading is within the safe operating band.
     *
     * @param celsius the current furnace temperature
     * @return true if the reading is at or below the maximum safe value
     */
    public boolean isSafe(int celsius) {
        return celsius <= MAX_SAFE;
    }

    /**
     * Computes how many degrees of headroom remain before the maximum
     * safe temperature.
     *
     * @param celsius the current furnace temperature
     * @return the remaining headroom, zero or negative if already over
     */
    public int headroom(int celsius) {
        return MAX_SAFE - celsius;
    }
}
