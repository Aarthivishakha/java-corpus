# PMD

Synthetic, clean-by-design Java project for **PMD**.

Domain: irrigation planning (IrrigationPlanner)

**Not installed here**: see Notes for why, and what was checked instead.

## What a passing result looks like

PMD's default Java ruleset (best-practices, design, error-prone) would report zero violations against IrrigationPlanner -- no unused variables, no empty catch blocks, no needless complexity.

## Command

```bash
pmd check -d src/main/java -R rulesets/java/quickstart.xml
```

## Notes

PMD's only distribution is a GitHub Release zip; Maven Central resolution is the fallback route and both are blocked at this sandbox's egress proxy. No apt package exists for current PMD.

## Per-family results

Boundary families this tool was exploded into, and what each one's own real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | NOT INSTALLED |
| `java9` | JDK 11 host, `--release 9` | NOT INSTALLED |
| `java16` | JDK 17 host, `--release 16` | NOT INSTALLED |
| `java24` | JDK 25 host, `--release 24` | NOT INSTALLED |
| `java25` | JDK 25 (native) | NOT INSTALLED |

Only distribution is a GitHub Release zip (Maven Central is the fallback, also blocked); no apt package exists for current PMD. No JDK family changes this.

