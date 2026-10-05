# Lizard

Synthetic, clean-by-design Java project for **Lizard**.

Domain: harvest-scale weighing (HarvestScale)

**Measured**: installed (or built from real source) and actually invoked in the build environment; the result below is real, not asserted.

## What a passing result looks like

`lizard` reports every function in HarvestScale under its default cyclomatic-complexity threshold (15) and under its default parameter-count threshold (100) -- zero warnings.

## Command

```bash
lizard --languages java src/main/java
```

## Notes

Lizard is a token-based, language-agnostic complexity tool (the same package already used for this purpose in Python-Tools-Clean); it needs no JDK or build system, only its own tokeniser, so it is version-blind the way the platform's build-contract doc classifies it ("Class B").

## Per-family results

Boundary families this tool was exploded into, and what each one's own real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | CLEAN |
| `java9` | JDK 11 host, `--release 9` | CLEAN |
| `java16` | JDK 17 host, `--release 16` | CLEAN |
| `java24` | JDK 25 host, `--release 24` | CLEAN |
| `java25` | JDK 25 (native) | CLEAN |

