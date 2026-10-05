# CPD

Inverse-corpus counterpart to the sibling Java-Tools-Clean's **CPD**
folder. Engineered so the real stand-in tool finds something genuinely
wrong, confirmed by actually running it -- not asserted.

Domain: depot-parcel tallying (ParcelTally)

**Not installed here** (CPD itself): see Notes for why, and what was
checked instead -- the same infrastructure gap as Clean's own CPD
folder.

## What a failing result looks like

CPD (PMD's copy-paste detector) is not reachable here (see notes), so
`jscpd` -- the same real, npm-installed stand-in already used for this
purpose in the sibling Clean corpus -- was run for real against
ParcelTally's Java source and found **1 real clone**: `summarizeEastWing()`
and `summarizeWestWing()` are copy-pasted, 10 duplicated lines (17.54%
of the file) and 59 duplicated tokens (8.78%).

## Command

```bash
npx jscpd . --min-lines 5 --min-tokens 30 --threshold 0
```

## Notes

CPD has no standalone distribution outside PMD's own release zip (a
GitHub Release asset); PMD/CPD have no Debian package either. Both are
unreachable at this sandbox's egress proxy, so CPD itself is NOT
INSTALLED -- identical to the sibling Clean corpus's own finding.
jscpd -- real, npm-installed, already vetted in Clean -- was run for
real in its place, against a fixture deliberately written with a
copy-pasted pair of methods.

## Per-family results

Boundary families this tool was exploded into, and what each one's own
real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | **FINDING** |
| `java9` | JDK 11 host, `--release 9` | **FINDING** |
| `java16` | JDK 17 host, `--release 16` | **FINDING** |
| `java24` | JDK 25 host, `--release 24` | **FINDING** |
| `java25` | JDK 25 (native) | **FINDING** |

Genuine finding(s) on this tool: 1 real clone (10 lines / 59 tokens),
identical across all five families since jscpd reads only source text.
