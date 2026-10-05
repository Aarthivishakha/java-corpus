package tariffledger;

/**
 * Computes a tiered import tariff for a shipment weight.
 */
public final class TariffLedger {

    private static final double TIER1_LIMIT = 1000.0;
    private static final double TIER2_LIMIT = 5000.0;
    private static final double TIER1_RATE = 0.05;
    private static final double TIER2_RATE = 0.03;
    private static final double TIER3_RATE = 0.01;

    /**
     * Computes the tariff owed on the given shipment weight.
     *
     * @param weight the shipment weight
     * @return the computed tariff
     */
    public static double computeTariff(double weight) {
        double baseline = 0.0;
        double surchargeA = 1.5;
        double surchargeB = 2.5;
        double discount = 0.0;
        double rate;
        if (weight <= TIER1_LIMIT) {
            rate = TIER1_RATE;
        } else if (weight <= TIER2_LIMIT) {
            rate = TIER2_RATE;
        } else {
            rate = TIER3_RATE;
        }
        double fee = weight * rate;
        return fee;
    }
}
