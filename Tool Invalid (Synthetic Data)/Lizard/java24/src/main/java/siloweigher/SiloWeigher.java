package siloweigher;

/**
 * Weighs grain silos and classifies readings into handling categories.
 */
public final class SiloWeigher {

    /**
     * Routes a reading code to a handling category using a wide,
     * deliberately tangled branch structure.
     *
     * @param code the reading code
     * @return a handling category label
     */
    public String routeCode(int code) {
        String label;
        switch (code) {
            case 0: label = "empty"; break;
            case 1: label = "quarter"; break;
            case 2: label = "quarter"; break;
            case 3: label = "half"; break;
            case 4: label = "half"; break;
            case 5: label = "half"; break;
            case 6: label = "three-quarter"; break;
            case 7: label = "three-quarter"; break;
            case 8: label = "full"; break;
            case 9: label = "full"; break;
            case 10: label = "overfull"; break;
            case 11: label = "overfull"; break;
            case 12: label = "overfull"; break;
            default: label = "unknown"; break;
        }
        return label;
    }

    /**
     * Assigns a priority to a silo based on a tangled ladder of
     * moisture, temperature and age thresholds.
     *
     * @param moisture    percent moisture reading
     * @param temperature degrees celsius
     * @param ageDays     age of the grain in days
     * @return a priority label
     */
    public String priorityFor(double moisture, double temperature, int ageDays) {
        if (moisture > 18.0) {
            if (temperature > 30.0) {
                if (ageDays > 90) {
                    return "critical";
                } else if (ageDays > 30) {
                    return "urgent";
                } else {
                    return "watch";
                }
            } else if (temperature > 20.0) {
                if (ageDays > 90) {
                    return "urgent";
                } else {
                    return "watch";
                }
            } else {
                return "watch";
            }
        } else if (moisture > 14.0) {
            if (temperature > 30.0) {
                if (ageDays > 60) {
                    return "watch";
                } else {
                    return "normal";
                }
            } else {
                return "normal";
            }
        } else {
            if (temperature > 35.0) {
                return "watch";
            } else {
                return "normal";
            }
        }
    }

    /**
     * Builds a shipment report line from six separate reading fields.
     *
     * @param siloId      the silo identifier
     * @param bushelCount the bushel count
     * @param moisture    percent moisture
     * @param temperature degrees celsius
     * @param ageDays     age in days
     * @param handler     the handler name
     * @return a formatted report line
     */
    public String shipmentReport(String siloId, int bushelCount, double moisture,
            double temperature, int ageDays, String handler) {
        return siloId + ":" + bushelCount + ":" + moisture + ":" + temperature
                + ":" + ageDays + ":" + handler;
    }

    /**
     * Reports whether a silo is empty.
     *
     * @param bushelCount the bushel count
     * @return true if the count is zero
     */
    public boolean isEmpty(int bushelCount) {
        return bushelCount == 0;
    }
}
