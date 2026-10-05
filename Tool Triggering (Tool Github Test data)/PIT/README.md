# PIT

Inverse-corpus counterpart to the sibling Java-Tools-Clean's **PIT**
folder. Stays **NOT_INSTALLED** here too -- the blocker is
infrastructure (Maven Central blocked), not anything about this corpus
being Clean or Invalid, so there is nothing to invert.

Package: org.pitest:pitest-command-line / pitest-maven (Maven Central)

Domain: batch-yield mutation-testing target (YieldInspector)

**Not installed here**: see Notes for why, and what was checked instead.

## What a result would look like

A real PIT mutation run against YieldInspector's `classify`/`passes`
boundary logic would report a mutation score -- low, for this corpus,
since the fixture's own test coverage is deliberately thin. Not
measurable here: see Notes.

## Command

```bash
java -cp <pitest jars> org.pitest.mutationtest.commandline.MutationCoverageReport --targetClasses yieldinspector.* --sourceDirs src/main/java
```

## Notes

PIT's command-line jar and all its runtime dependencies (ASM, the JUnit
plugin) resolve from Maven Central, which this sandbox's egress proxy
refuses. Identical to the sibling Clean corpus's own finding.

## Per-family results

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | NOT INSTALLED |
| `java9` | JDK 11 host, `--release 9` | NOT INSTALLED |
| `java16` | JDK 17 host, `--release 16` | NOT INSTALLED |
| `java24` | JDK 25 host, `--release 24` | NOT INSTALLED |
| `java25` | JDK 25 (native) | NOT INSTALLED |
