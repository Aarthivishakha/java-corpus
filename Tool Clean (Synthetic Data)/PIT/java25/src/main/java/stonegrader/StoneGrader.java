package stonegrader;

/**
 * Grades quarry stone blocks by hardness and size into shipping
 * categories.
 */
public final class StoneGrader {

    /**
     * Grades a stone block by Mohs hardness and weight.
     *
     * @param mohsHardness the measured Mohs hardness
     * @param weightKg     the block weight in kilograms
     * @return a grade label
     */
    public String grade(double mohsHardness, double weightKg) {
        if (mohsHardness < 3.0) {
            return "soft";
        }
        if (mohsHardness < 6.0) {
            if (weightKg > 500.0) {
                return "structural";
            }
            return "ornamental";
        }
        return "premium";
    }

    /**
     * Computes the shipping fee for a block based on its weight.
     *
     * @param weightKg the block weight in kilograms
     * @return the shipping fee
     */
    public double shippingFee(double weightKg) {
        double base = 20.0;
        double perKilogram = 0.15;
        return base + weightKg * perKilogram;
    }
}
