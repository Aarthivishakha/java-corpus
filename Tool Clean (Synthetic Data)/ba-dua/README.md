# ba-dua

Synthetic, clean-by-design Java project for **ba-dua**.

Domain: mill-race flow tracking (MillRace)

**Not installed here**: see Notes for why, and what was checked instead.

## What a passing result looks like

ba-dua's def-use coverage instrumentation on MillRace would report 100% all-uses coverage -- every definition-use pair the JUnit4 suite can reach is exercised.

## Command

```bash
java -javaagent:ba-dua-agent.jar=... org.junit.runner.JUnitCore MillRaceTest
```

## Notes

ba-dua's own pom pins `org.jacoco:org.jacoco.core:0.8.1` internally (2018, Java-10 class-file ceiling) rather than a floating version, so a from-source build (the same technique used for ASM-DefUse) would need that exact old JaCoCo core, not the 0.8.11 this sandbox has -- and 0.8.1 is itself unreachable via Maven Central. Documented, not attempted with a mismatched substitute.

## Per-family results

Boundary families this tool was exploded into, and what each one's own real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | NOT INSTALLED |
| `java9` | JDK 11 host, `--release 9` | NOT INSTALLED |
| `java16` | JDK 17 host, `--release 16` | NOT INSTALLED |
| `java24` | JDK 25 host, `--release 24` | NOT INSTALLED |
| `java25` | JDK 25 (native) | NOT INSTALLED |

ba-dua's own pom pins an old JaCoCo core (0.8.1) unreachable via Maven Central; a from-source build would need that exact old dependency. No JDK family changes this.

