package stonegrader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StoneGraderTest {

    private final StoneGrader grader = new StoneGrader();

    @Test
    public void softStoneGrade() {
        assertEquals("soft", grader.grade(2.0, 100.0));
    }

    @Test
    public void ornamentalMidHardnessLightWeight() {
        assertEquals("ornamental", grader.grade(4.0, 100.0));
    }

    @Test
    public void structuralMidHardnessHeavyWeight() {
        assertEquals("structural", grader.grade(4.0, 800.0));
    }

    @Test
    public void premiumHardStone() {
        assertEquals("premium", grader.grade(7.0, 50.0));
    }

    @Test
    public void shippingFeeScalesWithWeight() {
        assertEquals(35.0, grader.shippingFee(100.0), 0.0001);
    }
}
