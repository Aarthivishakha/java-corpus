package supplyregistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers third-party supply components and their declared origin.
 */
public final class SupplyRegistry {

    private final List<String> components = new ArrayList<>();

    /**
     * Registers a supply component.
     *
     * @param name the component name
     */
    public void register(String name) {
        components.add(name);
    }

    /**
     * Reports whether a component is registered.
     *
     * @param name the component name
     * @return true if the component was registered
     */
    public boolean isRegistered(String name) {
        return components.contains(name);
    }

    /**
     * Counts how many components are registered.
     *
     * @return the registered component count
     */
    public int registeredCount() {
        return components.size();
    }
}
