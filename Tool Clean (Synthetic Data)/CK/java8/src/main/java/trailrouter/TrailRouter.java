package trailrouter;

import java.util.List;

/**
 * Picks the shorter of two named trail segments for a hiker, given
 * their total distances.
 */
public final class TrailRouter {

    /**
     * Chooses the shorter of two named trail segments.
     *
     * @param nameA     the first trail's name
     * @param lengthA   the first trail's length in kilometers
     * @param nameB     the second trail's name
     * @param lengthB   the second trail's length in kilometers
     * @return the name of the shorter trail
     */
    public String shorterOf(String nameA, double lengthA, String nameB, double lengthB) {
        return lengthA <= lengthB ? nameA : nameB;
    }

    /**
     * Sums the lengths of a list of trail segments.
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
