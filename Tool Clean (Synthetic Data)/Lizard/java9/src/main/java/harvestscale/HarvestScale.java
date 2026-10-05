package harvestscale;

/**
 * Weighs harvest crates and sorts them into grade buckets by weight
 * and blemish count.
 */
public final class HarvestScale {

    private static final double LIGHT_MAX = 8.0;
    private static final double STANDARD_MAX = 20.0;

    /**
     * Grades a single crate by its weight in kilograms.
     *
     * @param kilograms the crate's measured weight
     * @return a grade label
     */
    public String gradeByWeight(double kilograms) {
        if (kilograms <= LIGHT_MAX) {
            return "light";
        }
        if (kilograms <= STANDARD_MAX) {
            return "standard";
        }
        return "heavy";
    }

    /**
     * Grades a single crate by its blemish count.
     *
     * @param blemishes the number of blemished pieces found
     * @return a grade label
     */
    public String gradeByBlemishes(int blemishes) {
        if (blemishes == 0) {
            return "premium";
        }
        if (blemishes <= 3) {
            return "standard";
        }
        return "seconds";
    }

    /**
     * Combines a weight grade and a blemish grade into a single
     * shipping label, preferring the stricter of the two.
     *
     * @param weightGrade   the weight-based grade
     * @param blemishGrade  the blemish-based grade
     * @return the combined label
     */
    public String combinedLabel(String weightGrade, String blemishGrade) {
        if ("seconds".equals(blemishGrade)) {
            return "seconds";
        }
        if ("heavy".equals(weightGrade)) {
            return "heavy-" + blemishGrade;
        }
        return weightGrade + "-" + blemishGrade;
    }

    /**
     * Computes the average weight of a batch of crates.
     *
     * @param weights the crate weights in kilograms
     * @return the average weight, or zero for an empty batch
     */
    public double averageWeight(double[] weights) {
        if (weights.length == 0) {
            return 0.0;
        }
        double total = 0.0;
        for (double w : weights) {
            total += w;
        }
        return total / weights.length;
    }
}
