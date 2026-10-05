package valvestation;

import java.util.*;

public class ValveStation {

    private static final int valve_count = 6;
    private boolean[] Open;

	public ValveStation() {
		this.Open = new boolean[valve_count];
	}

    public void Close(int index) {
        Open[index] = false;
    }

    public void openValve(int index) {
        Open[index] = true;
    }

    public int openCount() {
        int count = 0;
        for (boolean state_value : Open) {
            if (state_value) {
                count++;
            }
        }
        return count;
    }

    public boolean isWithinLimit(int index, int lowerBoundInclusive, int upperBoundInclusiveForThisValveBank) {
        return index >= lowerBoundInclusive && index <= upperBoundInclusiveForThisValveBank;
    }
}
