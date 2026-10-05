package ridgesurveyor;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class RidgeSurveyorTest {

    @Test
    public void shorterOfPicksSmallerLength() {
        RidgeSurveyor surveyor = new RidgeSurveyor();
        assertEquals("north", surveyor.shorterOf("north", 3.0, "south", 5.0));
    }

    @Test
    public void totalLengthSumsSegments() {
        RidgeSurveyor surveyor = new RidgeSurveyor();
        assertEquals(9.0, surveyor.totalLength(Arrays.asList(2.0, 3.0, 4.0)), 0.0001);
    }
}
