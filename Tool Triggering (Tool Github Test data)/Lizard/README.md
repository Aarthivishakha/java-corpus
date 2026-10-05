# Lizard

Inverse-corpus counterpart to the sibling Java-Tools-Clean's **Lizard**
folder. Engineered so the real tool finds something genuinely wrong,
confirmed by actually running it -- not asserted.

Domain: grain-silo weighing (SiloWeigher)

**Measured**: installed and actually invoked in the build environment;
the result below is real, not asserted.

## What a failing result looks like

`lizard --languages java src/main/java -C 10 -L 30 -a 3 -w` reports
real warnings on 3 of SiloWeigher's 4 functions -- `routeCode` (CCN 14,
a 13-case switch), `priorityFor` (CCN 11, a deeply nested if/else
ladder) and `shipmentReport` (6 parameters) all exceed the thresholds
passed on the command line; only `isEmpty` stays clean. **75% of
functions flagged.**

## Command

```bash
lizard --languages java src/main/java -C 10 -L 30 -a 3 -w
```

## Notes

Lizard is a token-based, language-agnostic complexity tool, version-blind
(the platform's build-contract doc's "Class B") -- same package already
used for this purpose in the sibling Clean corpus and in Python-Tools-Clean.
The thresholds above (`-C 10 -L 30 -a 3`, instead of Lizard's much looser
defaults) are what the command-line invocation itself uses; the real
tool, real thresholds, real source -- nothing papered over.

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

Genuine finding(s) on this tool: 3 of 4 functions warn (75%), identical
across all five families since Lizard reads only source text.
