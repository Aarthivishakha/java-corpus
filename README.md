# Testable Java corpus — JV_V14_GRADLEKOTLINDSL_THINJAR_MICRO

Grid cell `GRK-THIN-S` of the 24-cell Java grid.

## Project type

Order pricing and risk domain. The domain layer is byte-identical across all 24 branches
in this family, so any difference in tool output is attributable to the branch variables
below and not to the code the tool was pointed at.

## Branches

This repository holds 432 orphan branches, one per grid cell across every Java version, build system and packaging combination in this corpus. `main` carries the title
only. See `dataset.json` for the machine-readable description of this branch.

## Branch variables

| Variable | Value |
|---|---|
| Java version | 14 |
| Host JDK | 17 |
| Build system | Gradle / Kotlin DSL |
| Packaging | Thin jar |
| Architecture | Microservices |

## Supported tools

19 tools are wired on this branch (one `Tool Triggering (Synthetic Data)/<dir>/` folder
each). **14 of them run on JDK 14 (host JDK 17); 5 do not.**

That is the measurement, not a defect. A tool that cannot run exits **3**,
not 0 -- a skip that looks like a pass is the failure mode this corpus
exists to expose. Every `trigger.yaml` records the real, specific reason
when a tool can't run on this family.

### Running here

| Tool | Role | Block |
|---|---|---|
| `checkstyle` | primary | Lint / Rule Violations |
| `ck` | primary | Cyclomatic Complexity |
| `cpd` | primary | Code Duplication |
| `diff-cover` | primary | Coverage Delta |
| `git-churn` | primary | Code Churn |
| `grype` | alternative | Dependency Risk (SCA) |
| `jacoco` | primary | Statement / Branch / Path Coverage |
| `lizard` | alternative | Cyclomatic Complexity |
| `owasp-dependency-check` | primary | Dependency Risk (SCA) |
| `pit` | primary | Mutation Score |
| `pmd` | primary | Cognitive Complexity |
| `pydriller` | alternative | Code Churn |
| `spoon` | primary | Data Flow Testing |
| `spotbugs` | primary | Static Vulnerabilities (SAST) |

### Dark here

| Tool | Role | Block | Why |
|---|---|---|---|
| `asm-defuse` | primary | Data Flow Testing | no jar published under a stable coordinate - place one in Tool Triggering (Synthetic Data)/asm-defuse/ |
| `ba-dua` | primary | All Definition Coverage | ba-dua 0.8.0 links JaCoCo 0.8.1, which stops at Java 10 class files |
| `custom-def-use` | n/a (placeholder) | Data Flow Testing | not a tool - the sheet names no package, version or vendor |
| `nullaway` | primary | All Definition Coverage | runs as an Error Prone compiler plugin, not a standalone step |
| `sonar` | primary | Coverage Delta | needs a running SonarQube server; set SONAR_HOST_URL to enable |

## Build

```
./gradlew build
```

Main and test sources both compile at Java 14 (bytecode major version 58), built by
`javac 17` with `--release 14`.

**Syntax is back.** Java 12 and Java 13 both contributed no final language syntax, because
their headline feature was held in preview through two cycles. That feature lands here:
**switch expressions became final in Java 14** (JEP 361), after preview in 12 (JEP 325) and
again in 13 (JEP 354). java14 closes the arc the two families before it opened, and it is
the first family since java11 to carry both kinds of lock at once:

| Lock | Kind | Where | Since |
|---|---|---|---|
| switch expressions, arrow rules, multiple case labels, `yield` | **parse-time** | `analysis/OrderClassifier.java` | 14 (final) |
| `java.io.Serial` | **attribution-time** | `model/PricingSnapshot.java` | 14 |

The distinction is measured, not asserted. `javac --release 13` reports only **three** errors
on the branch, all in `OrderClassifier` - *"switch expressions are not supported in
-source 13"*, *"switch rules..."*, *"multiple case labels..."* - because the parser stops
there and never reaches attribution. Delete that one file from a scratch copy and
`--release 13` reports **four more**, all `cannot find symbol: class Serial`. Seven locks
total, in two layers.

`@Serial` is a real API, not a decorative one: serialization's hook methods are matched by
name and signature at runtime with no interface to implement, so a misspelled `readObject`
silently does nothing. The annotation is what turns that into a compile error.

**Records, pattern matching for `instanceof` and text blocks are deliberately absent.** All
three were still preview in Java 14 - final in 16, 16 and 15 - so under the preview-off rule
they belong to later families, exactly as pattern-matching `switch` was held back from
java17 to java21.

Forward-checked as well as backward: the branch compiles clean at `--release 15, 16, 17, 21`
and `25`, so the cumulative ladder holds above it. That check entered the corpus after the
Java 13 family turned up `String.formatted` - an API that passes the backward check and then
disappears one release later.

Java 14 is **not an LTS release**. It shipped March 2020 and reached end of life in
September 2020, six months later. It is in this corpus to complete the version axis, not as
a recommendation.

Produces: `dist/jv-224.jar or target/jv-224-1.0.0.jar`

## Run

```
java -jar <artifact> O-1234
```

## Test

```
./gradlew test
```

## Workspace projects

- `jv-224-domain/`
- `jv-224-pricing/`
- `jv-224-risk/`
- `jv-224-catalog/`


## Tool test-data folders

Three sibling folders sit at the repo root, alongside this branch's own
`Tool Triggering (Synthetic Data)/` (above).

### `Tool Triggering (Tool Github Test data)/`
16 of this branch's 19 wired tools carry their own real upstream test suite
or source, pulled as-is from that tool's actual GitHub project -- not
generated: `CK/`, `Spoon/`, `JaCoCo/`, `PMD/`, `SpotBugs/`, `FindSecBugs/`,
`Checkstyle/`, `CPD/`, `PIT/`, `OWASP Dependency-Check/`, `Grype/`, `Lizard/`,
`ASM-DefUse/`, `ba-dua/`, `diff-cover/` and `pydriller/`. The remaining 3
wired tools have no folder here because there is no applicable upstream test
suite to pull: `git-churn` is just git's own log, not a packaged tool;
`nullaway` runs embedded as an Error Prone compiler plugin, not a standalone
artifact; and `sonar` needs a live SonarQube server rather than a static
test suite. (`custom-def-use` is the corpus's own deliberate non-tool
placeholder -- see the Supported tools table above -- so it was never a
candidate for this folder either.)

### `Tool Clean (Synthetic Data)/`
Most tools here carry 5 generated fixture packages, one per representative
JDK family (8, 9, 16, 24, 25), engineered to be clean so the tool should
report zero findings: the **Tool Clean (100% pass)** condition.

### `Tool Invalid (Synthetic Data)/`
Same shape as Clean -- the same 5-JDK-family fixtures -- but engineered so
every fixture makes the tool flag or fail rather than pass: the
**Tool Invalid** condition. `diff-cover` and `pydriller` each carry one real
git repository's worth of history in both Clean and Invalid rather than 5
per-version copies, restored from `_git-bundles/` via `restore-git.ps1`
rather than kept as a live `.git` folder, so a plain file copy never
silently drops their content.

## Tool entry points

Each tool has `Tool Triggering (Synthetic Data)/<tool>/trigger.yaml` and `Tool Triggering (Synthetic Data)/<tool>/run.sh`. Runners follow the
exit-code contract: 0 ran, 1 failed, 3 skipped-cannot-run, 4 not-installed.

## Planted CVE pins

| Dependency | Version |
|---|---|
| `commons-collections:commons-collections` | 3.2.1 |
| `org.apache.commons:commons-text` | 1.9 |
| `com.fasterxml.jackson.core:jackson-databind` | 2.9.10.1 |
| `log4j:log4j` | 1.2.17 |
| `org.yaml:snakeyaml` | 1.30 |

Deliberately vulnerable versions, so the SCA metrics have known true positives. Do not
upgrade them.
