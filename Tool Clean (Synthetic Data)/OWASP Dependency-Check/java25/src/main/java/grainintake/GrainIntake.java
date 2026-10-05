package grainintake;

/**
 * Tracks grain intake weight against a silo's maximum capacity.
 */
public final class GrainIntake {

    private final double capacityKilograms;
    private double storedKilograms;

    /**
     * Creates an intake tracker for a silo of the given capacity.
     *
     * @param capacityKilograms the silo's maximum capacity in kilograms
     */
    public GrainIntake(double capacityKilograms) {
        this.capacityKilograms = capacityKilograms;
    }

    /**
     * Adds an incoming grain delivery to the silo.
     *
     * @param kilograms the delivered weight in kilograms
     * @return the weight actually accepted, capped at remaining capacity
     */
    public double accept(double kilograms) {
        double room = capacityKilograms - storedKilograms;
        double accepted = Math.min(room, kilograms);
        storedKilograms += accepted;
        return accepted;
    }

    /**
     * Reports the currently stored weight.
     *
     * @return the stored weight in kilograms
     */
    public double stored() {
        return storedKilograms;
    }

    /**
     * Reports whether the silo is completely full.
     *
     * @return true if stored weight has reached capacity
     */
    public boolean isFull() {
        return storedKilograms >= capacityKilograms;
    }
}
