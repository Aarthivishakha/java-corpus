package dockmanifest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Records the pinned dependency versions a dock shipment manifest
 * declares, so they can be checked against a vulnerability database.
 */
public final class DockManifest {

    private final Map<String, String> pinnedVersions = new LinkedHashMap<>();

    /**
     * Pins a dependency to an exact version.
     *
     * @param name    the dependency name
     * @param version the exact version pinned
     */
    public void pin(String name, String version) {
        pinnedVersions.put(name, version);
    }

    /**
     * Reports the pinned version for a dependency.
     *
     * @param name the dependency name
     * @return the pinned version, or null if not pinned
     */
    public String versionOf(String name) {
        return pinnedVersions.get(name);
    }

    /**
     * Counts how many dependencies this manifest pins.
     *
     * @return the number of pinned dependencies
     */
    public int size() {
        return pinnedVersions.size();
    }
}
