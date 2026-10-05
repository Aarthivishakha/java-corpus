package ridgesurveyor;

import java.util.List;

/**
 * Picks the shorter of two named ridge trail segments for a hiker,
 * given their total distances.
 */
public final class RidgeSurveyor {

    /**
     * Chooses the shorter of two named ridge segments.
     *
     * @param nameA   the first segment's name
     * @param lengthA the first segment's length in kilometers
     * @param nameB   the second segment's name
     * @param lengthB the second segment's length in kilometers
     * @return the name of the shorter segment
     */
    public String shorterOf(String nameA, double lengthA, String nameB, double lengthB) {
        return lengthA <= lengthB ? nameA : nameB;
    }

    /**
     * Sums the lengths of a list of ridge segments.
     *
     * @param segmentLengths the segment lengths in kilometers
     * @return the total route length
     */
    public double totalLength(List<Double> segmentLengths) {
        double total = 0.0;
        for (double length : segmentLengths) {
            total += length;
        }
        return total;
    }
}
