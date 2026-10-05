# Spoon

Synthetic, clean-by-design Java project for **Spoon**.

Domain: wharf loading schedule (WharfSchedule)

**Not installed here**: see Notes for why, and what was checked instead.

## What a passing result looks like

Spoon's AST model of WharfSchedule would resolve every reference cleanly (no unresolved types/imports), reflecting genuinely simple, self-contained scheduling logic.

## Command

```bash
java -cp spoon-core.jar spoon.Launcher -i src/main/java --output-type nooutput
```

## Notes

Spoon is a large multi-module Maven artifact with many transitive dependencies, all resolved from Maven Central, which is blocked at this sandbox's egress proxy. No apt package exists.

## Per-family results

Boundary families this tool was exploded into, and what each one's own real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | NOT INSTALLED |
| `java9` | JDK 11 host, `--release 9` | NOT INSTALLED |
| `java16` | JDK 17 host, `--release 16` | NOT INSTALLED |
| `java24` | JDK 25 host, `--release 24` | NOT INSTALLED |
| `java25` | JDK 25 (native) | NOT INSTALLED |

Large multi-module Maven artifact resolved entirely from Maven Central, which is blocked; no apt package exists. No JDK family changes this.

