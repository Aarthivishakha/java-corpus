# Testable Java corpus — JV_V20_ANTIVY_THINJAR_MICRO

Grid cell `ANT-THIN-S` of the 24-cell Java grid.

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
| Java version | 20 |
| Host JDK | 21 |
| Build system | Ant + Ivy |
| Packaging | Thin jar |
| Architecture | Microservices |

## Supported tools

19 tools are wired on this branch (one `Tool Triggering (Synthetic Data)/<dir>/` folder
each). **13 of them run on JDK 20 (host JDK 21); 6 do not.**

That is the measurement, not a defect. A tool that cannot run exits **3**,
not 0 -- a skip that looks like a pass is the failure mode this corpus
exists to expose. Every `trigger.yaml` records the real, specific reason
when a tool can't run on this family.

### Running here

| Tool | Role | Block |
|---|---|---|
| `checkstyle` | primary | Lint / Rule Violations |
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
| `ck` | primary | Cyclomatic Complexity | CK 0.7.0 bundles Eclipse JDT 3.26.0, which parses only up to Java 16 - it cannot read this branch's records or sealed types |
| `custom-def-use` | n/a (placeholder) | Data Flow Testing | not a tool - the sheet names no package, version or vendor |
| `nullaway` | primary | All Definition Coverage | runs as an Error Prone compiler plugin, not a standalone step |
| `sonar` | primary | Coverage Delta | needs a running SonarQube server; set SONAR_HOST_URL to enable |

## Build

```
ant clean package
```

Main and test sources both compile at Java 20 (bytecode major version 64), built by
`javac 21` with `--release 20`.

**Java 20 is the emptiest release in this corpus.** It added no final language syntax, and
almost no final API either. Every language feature it carried was preview or incubator:

| Feature | Status in Java 20 | Final in |
|---|---|---|
| virtual threads (JEP 436) | 2nd preview | 21 |
| pattern matching for `switch` (JEP 433) | **fourth** preview | 21 |
| record patterns (JEP 432) | 2nd preview | 21 |
| Foreign Function & Memory (JEP 434) | 2nd preview | 22 |
| scoped values (JEP 429) | incubator | 25 |
| structured concurrency (JEP 437) | 2nd incubator | - |

That makes java20 the **third consecutive API-only family**, after Java 18 and Java 19.

### Finding the lock required diffing the compiler, not reading a feature list

Java 20's final API surface is small enough that no feature list names it. The locks below
were found by extracting `ct.sym` - the table of historical API signatures `javac` uses to
implement `--release` - and diffing the `java.base` member signatures between release 19 and
release 20 directly. That turned up 151 classes whose signature changed and 35 with added
members, most of them the *preview* `java.lang.foreign` API, which is excluded.

Four survive as final, non-preview and monotonic through 25:

| API | File | What it replaces |
|---|---|---|
| `Float.floatToFloat16`, `Float.float16ToFloat` | `util/CompactMetrics.java` | hand-written binary16 bit manipulation |
| `URL.of(URI, URLStreamHandler)` | `util/CompactMetrics.java` | the `URL` constructors, all deprecated in Java 20 |
| `Class.accessFlags()`, `Field.accessFlags()`, and the `AccessFlag` enum | `analysis/TypeAudit.java` | `getModifiers()`, an `int` bitmask |
| `ClassDesc.ofInternalName` | `analysis/TypeAudit.java` | building a descriptor from the source-form name |

`AccessFlag` is the one worth understanding. `getModifiers()` returns a bitmask in which the
same bit means different things depending on where it appears - `0x0080` is `TRANSIENT` on a
field and `VARARGS` on a method - and an `int` cannot tell you which. `AccessFlag` carries its
own valid locations, so it can, and `TypeAudit.fieldFlags` asserts exactly that.

21 errors at `--release 19`: 18 in `TypeAudit`, 3 in `CompactMetrics`. Whole-family and
per-file counts agree, because neither file carries a parse-time lock to halt on.

### Three API-only families in a row, and what it means for the axis

java18, java19 and java20 are three consecutive families with no final syntax between them.
Pattern matching for `switch` alone was previewed in **17, 18, 19 and 20** before going final
in 21. The corpus's preview-off rule is what makes those three families distinguishable at
all: without it, virtual threads and pattern `switch` would appear identically in java19,
java20 and java21, and three families would collapse into one.

Forward-checked at `--release 21` and `25`: clean.

Java 20 is **not an LTS release**. It shipped March 2023 and reached end of life in September
2023, six months later. It is in this corpus to complete the version axis, not as
a recommendation.

Produces: `dist/jv-356.jar or target/jv-356-1.0.0.jar`

## Run

```
java -jar <artifact> O-1234
```

## Test

```
ant test
```

## Workspace projects

- `jv-356-domain/`
- `jv-356-pricing/`
- `jv-356-risk/`
- `jv-356-catalog/`


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
