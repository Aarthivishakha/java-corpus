# CPD

Synthetic, clean-by-design Java project for **CPD**.

Domain: crate-inventory counting (CrateCounter)

**Not installed here**: see Notes for why, and what was checked instead.

## What a passing result looks like

CPD (PMD's copy-paste detector) is not reachable here (see notes), so `jscpd` -- the same duplicate-token-window detector already used for this purpose in the sibling JavaScript-Tools-Clean corpus, and named as the repaired alternative for this exact block in the platform's own java-repos-build-contract.md -- was run for real against CrateCounter's Java source and found zero duplicate blocks.

## Command

```bash
npx jscpd . --min-lines 5 --min-tokens 30 --threshold 0
```

## Notes

CPD has no standalone distribution outside PMD's own release zip, a GitHub Release asset; PMD/CPD have no Debian package either. Both are unreachable at this sandbox's egress proxy, so CPD itself is NOT INSTALLED. jscpd -- real, npm-installed, already vetted in the sibling corpus -- was run for real in its place.

## Per-family results

Boundary families this tool was exploded into, and what each one's own real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | CLEAN |
| `java9` | JDK 11 host, `--release 9` | CLEAN |
| `java16` | JDK 17 host, `--release 16` | CLEAN |
| `java24` | JDK 25 host, `--release 24` | CLEAN |
| `java25` | JDK 25 (native) | CLEAN |

