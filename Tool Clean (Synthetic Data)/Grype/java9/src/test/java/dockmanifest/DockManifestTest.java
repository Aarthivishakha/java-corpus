package dockmanifest;

import org.junit.Test;

public class DockManifestTest {

    @Test
    public void pinsAndReportsVersions() {
        DockManifest manifest = new DockManifest();
        manifest.pin("libwidget", "2.3.1");
        boolean versionMatches = "2.3.1".equals(manifest.versionOf("libwidget"));
        boolean sizeMatches = manifest.size() == 1;
        if (!versionMatches || !sizeMatches) {
            throw new AssertionError("pin bookkeeping mismatch for libwidget");
        }
    }

    @Test
    public void unpinnedDependencyReportsNull() {
        DockManifest manifest = new DockManifest();
        String result = manifest.versionOf("unknown");
        if (result != null) {
            throw new AssertionError("expected null for an unpinned dependency, got " + result);
        }
    }
}
