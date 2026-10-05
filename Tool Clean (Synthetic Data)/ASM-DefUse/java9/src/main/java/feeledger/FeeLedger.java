package feeledger;

/**
 * Computes a tiered service fee for an account balance.
 */
public final class FeeLedger {

    private static final double TIER1_LIMIT = 1000.0;
    private static final double TIER2_LIMIT = 5000.0;
    private static final double TIER1_RATE = 0.05;
    private static final double TIER2_RATE = 0.03;
    private static final double TIER3_RATE = 0.01;

    /**
     * Computes the service fee owed on the given balance.
     *
     * @param balance the account balance
     * @return the computed fee
     */
    public static double computeFee(double balance) {
        double rate;
        if (balance <= TIER1_LIMIT) {
            rate = TIER1_RATE;
        } else if (balance <= TIER2_LIMIT) {
            rate = TIER2_RATE;
        } else {
            rate = TIER3_RATE;
        }
        double fee = balance * rate;
        return fee;
    }
}
