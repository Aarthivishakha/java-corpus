# JaCoCo

Inverse-corpus counterpart to the sibling Java-Tools-Clean's **JaCoCo**
folder. Engineered so the real tool finds something genuinely wrong,
confirmed by actually running it -- not asserted.

Domain: furnace-temperature monitoring (FurnaceGauge)

**Measured**: installed and actually invoked in the build environment;
the result below is real, not asserted.

## What a failing result looks like

The real JaCoCo offline instrumenter runs FurnaceGauge's own JUnit4
test through instrumented bytecode and the resulting coverage is
genuinely weak: the test suite exercises only the `stageFor(100)` ->
"idle" path, leaving `temper`, `anneal`, `peak`, the over-temperature
throw, `isSafe` and `headroom` all unexercised. Measured instruction
coverage **23.91-27.50%**, branch coverage **20.00%** -- a clear
majority of FurnaceGauge's own logic goes untested.

## Command

```bash
java -javaagent:jacocoagent.jar=destfile=jacoco.exec -cp ... org.junit.runner.JUnitCore FurnaceGaugeTest && java -jar jacococli.jar report jacoco.exec --classfiles ... --xml coverage.xml
```

## Notes

Same installed `libjacoco-java` 0.8.11 as the sibling Clean corpus
(`apt install libjacoco-java`); only FurnaceGauge's own (deliberately
under-tested) source and test suite differ.

## Per-family results

Boundary families this tool was exploded into, and what each one's own
real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | **FINDING** -- `INSTRUCTION_PCT=23.91 BRANCH_PCT=20.00` |
| `java9` | JDK 11 host, `--release 9` | **FINDING** -- `INSTRUCTION_PCT=27.50 BRANCH_PCT=20.00` |
| `java16` | JDK 17 host, `--release 16` | **FINDING** -- `INSTRUCTION_PCT=27.50 BRANCH_PCT=20.00` |
| `java24` | JDK 25 host, `--release 24` | **FINDING** -- ASM ceiling (see below) |
| `java25` | JDK 25 (native) | **FINDING** -- ASM ceiling (see below) |

Genuine finding(s) on this tool:

**java8/java9/java16**: branch coverage measured at 20.00% (instruction
coverage 23.91-27.50%, the small native-vs-cross-compiled bytecode
difference is real and expected) -- a real, majority-uncovered result
from the real JaCoCo instrumenter and analyzer, not an edge case.

**java24, java25**: JaCoCo's offline instrumenter uses the same
apt-installed ASM 9.7 internally (Debian's jacoco package links the
system libasm-java rather than bundling its own copy). Instrumenting a
Java-24/25-targeted FurnaceGauge.class throws the identical
`IllegalArgumentException: Unsupported class file major version
68/69` inside `InstrSupport.classReaderFor`. Identical root cause to
the sibling Clean corpus's own JaCoCo folder, independent of this
corpus's own planted under-coverage -- a genuine environmental ceiling,
so this cell is "not CLEAN" (and thus FINDING) for a reason unrelated
to the planted test gaps.
