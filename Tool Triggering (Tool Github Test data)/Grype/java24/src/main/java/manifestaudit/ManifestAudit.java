package manifestaudit;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks declared package versions for a container image manifest.
 */
public final class ManifestAudit {

    private final Map<String, String> declaredVersions = new HashMap<>();

    /**
     * Declares a package version in the manifest.
     *
     * @param packageName the package name
     * @param version     the declared version string
     */
    public void declare(String packageName, String version) {
        declaredVersions.put(packageName, version);
    }

    /**
     * Reports the declared version for a package.
     *
     * @param packageName the package name
     * @return the declared version, or null if not declared
     */
    public String versionOf(String packageName) {
        return declaredVersions.get(packageName);
    }

    /**
     * Counts how many packages are declared.
     *
     * @return the declared package count
     */
    public int packageCount() {
        return declaredVersions.size();
    }
}
