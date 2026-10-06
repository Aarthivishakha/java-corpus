# Testable Java corpus — JV_V15_GRADLEKOTLINDSL_WAR_MICRO

Grid cell `GRK-WAR-S` of the 24-cell Java grid.

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
| Java version | 15 |
| Host JDK | 17 |
| Build system | Gradle / Kotlin DSL |
| Packaging | WAR |
| Architecture | Microservices |

## Supported tools

19 tools are wired on this branch (one `Tool Triggering (Synthetic Data)/<dir>/` folder
each). **14 of them run on JDK 15 (host JDK 17); 5 do not.**

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

Main and test sources both compile at Java 15 (bytecode major version 59), built by
`javac 17` with `--release 15`.

**Text blocks land here.** They were preview in Java 13 (JEP 355) and again in Java 14
(JEP 368), and became final in Java 15 (JEP 378). That is the second three-release preview
arc in this corpus, after switch expressions (preview 12, preview 13, final 14) - and it is
the reason the java13 family's lock had to be API-only. `service/PricingReport.java` carries
the entire Java 15 lock, in both layers:

| Lock | Kind | Since |
|---|---|---|
| text blocks | **parse-time** | 15 (final) |
| `String.formatted` | attribution-time | 15 (final) |
| `String.stripIndent` | attribution-time | 15 (final) |
| `String.translateEscapes` | attribution-time | 15 (final) |
| `CharSequence.isEmpty` | attribution-time | 15 |

`javac --release 14` reports **one** error - the text block - because the parser stops there.
Neutralise the text blocks in a scratch copy and four more appear.

### The three String methods, and why they belong here and not in java13

`String.formatted`, `stripIndent` and `translateEscapes` were evaluated as the **Java 13**
family's lock and rejected. This is the family where they are correct. Measured with
`javac 17` and `javac 25`, reading the exact diagnostic rather than pass/fail:

| `--release` | Status |
|---|---|
| 12 | absent - `cannot find symbol` |
| 13 | present, **deprecated for removal** - warning only, compiles |
| 14 | present, **preview API, disabled by default** - hard error under preview-off |
| **15** | **final - compiles clean** |
| 16 / 17 / 21 / 25 | final - compiles clean |

They shipped in JDK 13 as provisional API alongside preview text blocks, were reclassified
as preview APIs in JDK 14 with JEP 368, and became final in JDK 15. They travel with the
text blocks they were built for, which is why they land in the same family and the same
file.

Two things follow that the corpus's gates now encode. First, **a version lock has to be
checked forward as well as backward** - at java13 these pass the backward check and then
stop compiling one release up. Second, **pass/fail is not enough**: at `--release 13` they
compile with only a deprecation warning, so a gate reading exit status alone would have
called them healthy.

**Records, pattern matching for `instanceof` and sealed types are deliberately absent** -
records and `instanceof` patterns were in their second preview in 15 (final in 16) and
sealed types in their first (final in 17).

Forward-checked at `--release 16, 17, 21` and `25`: clean, so the cumulative ladder holds
above this family.

Java 15 is **not an LTS release**. It shipped September 2020 and reached end of life in
March 2021, six months later. It is in this corpus to complete the version axis, not as
a recommendation.

Produces: `jv-252.war`

## Run

```
java -jar <artifact> O-1234
```

## Test

```
./gradlew test
```

## Workspace projects

- `jv-252-domain/`
- `jv-252-pricing/`
- `jv-252-risk/`
- `jv-252-catalog/`


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
