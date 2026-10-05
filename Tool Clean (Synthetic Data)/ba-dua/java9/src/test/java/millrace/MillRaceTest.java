package millrace;

import org.junit.Test;

public class MillRaceTest {

    private final MillRace race = new MillRace();

    @Test
    public void computesWheelSpeed() {
        double rpm = race.wheelSpeedRpm(3.0);
        double delta = Math.abs(rpm - 13.75);
        if (delta > 0.05) {
            throw new AssertionError("expected ~13.75 rpm, got " + rpm);
        }
    }

    @Test
    public void ratingChecksBracketTheThreshold() {
        boolean overRated = race.exceedsRating(10.0, 20.0);
        boolean underRated = race.exceedsRating(1.0, 20.0);
        boolean ok = overRated && !underRated;
        if (!ok) {
            throw new AssertionError("rating check mismatch: over=" + overRated + " under=" + underRated);
        }
    }
}
