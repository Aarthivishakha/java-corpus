# ba-dua

Inverse-corpus counterpart to the sibling Java-Tools-Clean's **ba-dua**
folder. Stays **NOT_INSTALLED** here too -- the blocker is
infrastructure (Maven Central / GitHub Releases both blocked), not
anything about this corpus being Clean or Invalid, so there is nothing
to invert.

Package: br.usp.each.saeg:ba-dua-cli (GitHub-hosted / Maven Central)

Domain: coolant-loop maintenance-cycle tracking (CoolantCycle)

**Not installed here**: see Notes for why, and what was checked instead.

## What a result would look like

A real ba-dua def-use coverage run over CoolantCycle's compiled classes
would report def-use pair coverage from its own JaCoCo-0.8.1-based
agent. Not measurable here: see Notes.

## Command

```bash
java -javaagent:ba-dua-agent.jar=destfile=badua.ser -cp ... org.junit.runner.JUnitCore CoolantCycleTest
java -cp ba-dua-cli.jar br.usp.each.saeg.badua.cli.Report badua.ser build/main
```

## Notes

ba-dua's own release artifacts are hosted on its maintainers' GitHub
releases and (for its one Maven dependency) Maven Central -- both
unreachable at this sandbox's egress proxy. Identical to the sibling
Clean corpus's own finding; the platform's own build-contract doc
separately notes ba-dua bundles a JaCoCo-0.8.1-era core with a Java-10
bytecode ceiling, effectively dormant beyond that -- an independent
reason it would likely not reach this corpus's java24/java25 families
even if it were reachable.

## Per-family results

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | NOT INSTALLED |
| `java9` | JDK 11 host, `--release 9` | NOT INSTALLED |
| `java16` | JDK 17 host, `--release 16` | NOT INSTALLED |
| `java24` | JDK 25 host, `--release 24` | NOT INSTALLED |
| `java25` | JDK 25 (native) | NOT INSTALLED |
