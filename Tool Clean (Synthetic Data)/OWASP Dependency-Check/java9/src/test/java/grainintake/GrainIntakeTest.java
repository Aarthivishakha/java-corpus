package grainintake;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GrainIntakeTest {

    private GrainIntake intake;

    @Before
    public void setUp() {
        intake = new GrainIntake(100.0);
    }

    @Test
    public void acceptsDeliveriesUpToCapacity() {
        Assert.assertEquals(60.0, intake.accept(60.0), 0.0001);
        Assert.assertEquals(40.0, intake.accept(50.0), 0.0001);
        Assert.assertEquals(100.0, intake.stored(), 0.0001);
        Assert.assertTrue(intake.isFull());
    }

    @Test
    public void notFullBelowCapacity() {
        intake.accept(20.0);
        Assert.assertFalse(intake.isFull());
    }
}
