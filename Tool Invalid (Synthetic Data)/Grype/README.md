# Grype

Inverse-corpus counterpart to the sibling Java-Tools-Clean's **Grype**
folder. Stays **NOT_INSTALLED** here too -- the blocker is
infrastructure (GitHub Releases + the Go module proxy both blocked),
not anything about this corpus being Clean or Invalid, so there is
nothing to invert.

Package: github.com/anchore/grype (Go binary / GitHub release)

Domain: container manifest package auditing (ManifestAudit)

**Not installed here**: see Notes for why, and what was checked instead.

## What a result would look like

A Grype scan of ManifestAudit's declared package versions would report
known-vulnerable dependencies. Not measurable here: see Notes.

## Command

```bash
grype dir:.
```

## Notes

Grype ships as a GitHub Release binary; `go install` is blocked the
same way (`proxy.golang.org` not in the allowlist), and no package for
it exists in Ubuntu's own apt archive. Identical to the sibling Clean
corpus's own finding.

## Per-family results

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | NOT INSTALLED |
| `java9` | JDK 11 host, `--release 9` | NOT INSTALLED |
| `java16` | JDK 17 host, `--release 16` | NOT INSTALLED |
| `java24` | JDK 25 host, `--release 24` | NOT INSTALLED |
| `java25` | JDK 25 (native) | NOT INSTALLED |
