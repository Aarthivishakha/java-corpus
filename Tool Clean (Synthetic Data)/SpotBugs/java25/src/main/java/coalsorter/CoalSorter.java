package coalsorter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Sorts coal loads into grade bins by calorific value, keeping a
 * running tally per bin.
 */
public final class CoalSorter {

    private final List<String> binOrder;
    private final int[] tallies;

    /**
     * Creates a sorter with the given ordered list of grade bins.
     *
     * @param binOrder the grade bin labels, from lowest to highest grade
     */
    public CoalSorter(List<String> binOrder) {
        this.binOrder = new ArrayList<>(Objects.requireNonNull(binOrder));
        this.tallies = new int[binOrder.size()];
    }

    /**
     * Sorts a single load into the bin matching its calorific value.
     *
     * @param calorificValue the load's measured calorific value
     * @return the bin label the load was sorted into
     */
    public String sort(int calorificValue) {
        int index = binIndexFor(calorificValue);
        tallies[index]++;
        return binOrder.get(index);
    }

    /**
     * Reports the running tally for a bin.
     *
     * @param bin the bin label
     * @return the tally, or zero if the label is not one of this sorter's bins
     */
    public int tallyFor(String bin) {
        int index = binOrder.indexOf(bin);
        if (index < 0) {
            return 0;
        }
        return tallies[index];
    }

    private int binIndexFor(int calorificValue) {
        int thresholdStep = 1000;
        int index = calorificValue / thresholdStep;
        if (index < 0) {
            return 0;
        }
        if (index >= binOrder.size()) {
            return binOrder.size() - 1;
        }
        return index;
    }
}
