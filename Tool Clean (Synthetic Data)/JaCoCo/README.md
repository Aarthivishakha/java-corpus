# JaCoCo

Synthetic, clean-by-design Java project for **JaCoCo**.

Domain: kiln-temperature monitoring (KilnMonitor)

**Measured**: installed (or built from real source) and actually invoked in the build environment; the result below is real, not asserted.

## What a passing result looks like

The JaCoCo agent instruments KilnMonitor during its own JUnit4 test run and the resulting XML report shows 100% line and 100% branch coverage -- every branch in KilnMonitor's temperature logic is exercised by the test suite.

## Command

```bash
java -javaagent:jacocoagent.jar=destfile=jacoco.exec -cp ... org.junit.runner.JUnitCore KilnMonitorTest && java -jar jacococli.jar report jacoco.exec --classfiles ... --xml coverage.xml
```

## Notes

Installed via `apt install libjacoco-java`, which resolves entirely from the Ubuntu archive. Older than the current 0.8.15 release but a real, installed, invoked one -- the agent, the offline instrumenter and the report CLI are all present as real jars under /usr/share/java.

## Per-family results

Boundary families this tool was exploded into, and what each one's own real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | CLEAN |
| `java9` | JDK 11 host, `--release 9` | CLEAN |
| `java16` | JDK 17 host, `--release 16` | CLEAN |
| `java24` | JDK 25 host, `--release 24` | **FINDING** |
| `java25` | JDK 25 (native) | **FINDING** |

Genuine finding(s) on this tool:

**java24**: JaCoCo's offline instrumenter (`org.jacoco.core.instr.Instrumenter`) uses the same apt-installed ASM 9.7 internally (Debian's jacoco package links the system libasm-java rather than bundling its own copy). Instrumenting a Java-24-targeted KilnMonitor.class throws the identical `IllegalArgumentException: Unsupported class file major version 68` inside `InstrSupport.classReaderFor`. Confirmed two ways: via the in-process `Instrumenter` API and via the real `-javaagent` runtime jar attached to a live JVM, both fail identically.

**java25**: Same ASM 9.7 ceiling at major version 69 (Java 25). Identical failure, identical unreachable fix.

