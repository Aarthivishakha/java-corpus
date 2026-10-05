# Spoon

Inverse-corpus counterpart to the sibling Java-Tools-Clean's **Spoon**
folder. Stays **NOT_INSTALLED** here too -- the blocker is
infrastructure (Maven Central blocked), not anything about this corpus
being Clean or Invalid, so there is nothing to invert.

Package: fr.inria.gforge.spoon:spoon-core (Maven Central)

Domain: refinery batch-throughput planning (RefineryPlan)

**Not installed here**: see Notes for why, and what was checked instead.

## What a result would look like

A Spoon source-model build over RefineryPlan would let a transformation
or analysis walk its AST. Not measurable here: see Notes.

## Command

```bash
java -cp <spoon jars> spoon.Launcher -i src/main/java -o /tmp/spoon-out --noclasspath
```

## Notes

Spoon's core jar and its own dependency tree resolve from Maven
Central, which this sandbox's egress proxy refuses. Identical to the
sibling Clean corpus's own finding; the platform's own build-contract
doc separately notes Spoon needs JDK 17+ to run, independent of this
block.

## Per-family results

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | NOT INSTALLED |
| `java9` | JDK 11 host, `--release 9` | NOT INSTALLED |
| `java16` | JDK 17 host, `--release 16` | NOT INSTALLED |
| `java24` | JDK 25 host, `--release 24` | NOT INSTALLED |
| `java25` | JDK 25 (native) | NOT INSTALLED |
