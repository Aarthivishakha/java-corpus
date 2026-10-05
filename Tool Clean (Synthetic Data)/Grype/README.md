# Grype

Synthetic, clean-by-design Java project for **Grype**.

Domain: dock manifest dependency listing (DockManifest)

**Not installed here**: see Notes for why, and what was checked instead.

## What a passing result looks like

A Grype SBOM scan of DockManifest's pinned dependencies would report zero known-vulnerable packages, all pins chosen from current, non-CVE-flagged releases.

## Command

```bash
grype dir:. -o table
```

## Notes

Grype ships only as a GitHub Release binary or via `go install`. Both are blocked here: the GitHub releases host returns 403 at the egress proxy, and `go install github.com/anchore/grype@latest` fails with "Host not in allowlist: proxy.golang.org" -- measured directly, not assumed. No apt package exists for Grype.

## Per-family results

Boundary families this tool was exploded into, and what each one's own real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | NOT INSTALLED |
| `java9` | JDK 11 host, `--release 9` | NOT INSTALLED |
| `java16` | JDK 17 host, `--release 16` | NOT INSTALLED |
| `java24` | JDK 25 host, `--release 24` | NOT INSTALLED |
| `java25` | JDK 25 (native) | NOT INSTALLED |

Grype ships only as a GitHub Release binary or via `go install`; both are blocked (GitHub releases 403, `proxy.golang.org` not allowlisted). No apt package exists. No JDK family changes this.

