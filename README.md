# Testable Java corpus — JV_V24_MAVEN_THINJAR_MICRO

Grid cell `MVN-THIN-S` of the 24-cell Java grid.

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
| Java version | 24 |
| Host JDK | 25 |
| Build system | Maven |
| Packaging | Thin jar |
| Architecture | Microservices |

## Supported tools

19 tools are wired on this branch (one `Tool Triggering (Synthetic Data)/<dir>/` folder
each). **13 of them run on JDK 24 (host JDK 25); 6 do not.**

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
mvn -B clean package
```

Main and test sources both compile at Java 24 (bytecode major version 68), built by
`javac 25` with `--release 24`.

**Java 24 added no final language syntax** - every language JEP it carried was a preview:
primitive types in patterns (2nd), module import declarations (2nd), flexible constructor
bodies (3rd), implicitly declared classes (4th), structured concurrency (4th) and scoped
values (4th). Java 24 is the **sixth** API-only family, after 12, 13, 18, 19, 20 and 23.

What it did finalise is substantial - two APIs that had previewed in 22 and 23:

| Lock | File | Since |
|---|---|---|
| stream gatherers: `Stream.gather`, `Gatherers.windowFixed`/`windowSliding`/`scan`/`fold` | `analysis/LegGatherer.java` | 24 |
| the Class-File API: `java.lang.classfile` | `util/ClassFileProbe.java` | 24 |
| `Reader.of(CharSequence)` | `util/ClassFileProbe.java` | 24 |

26 errors at `--release 23`: 11 in `LegGatherer`, 15 in `ClassFileProbe`. Whole-family and
per-file agree - no parse-time lock to halt on.

### Gatherers reopened a closed part of the stream API

Before Java 24 the intermediate stage of a stream was fixed: `map`, `filter`, `flatMap`,
`mapMulti` and nothing else. A `Collector` could be written by anyone, but an *intermediate*
operation could not, so anything with a window or a running total had to leave the stream, be
done in a loop, and come back. `Gatherers.scan` is the clearest case - stateful and
intermediate, expressible by neither `map` nor a `Collector`. `LegGatherer.runningUnits`
returns one running total per leg in a single pass, and the test asserts that its last element
equals what `Gatherers.fold` produces independently.

### The Class-File API is directly relevant to this corpus's own findings

Every tool in the roster that reads bytecode ships its own copy of a parser, each pinned to a
different version, each needing an update before it can read the next release's class files.
That is precisely the ceiling this corpus keeps measuring: **PIT stops at Java 25 because its
bundled ASM 9.10.1 does, and CK stops at Java 16 because its bundled Eclipse JDT 3.26.0 does.**
A parser maintained inside the JDK cannot fall behind the JDK, which is the whole argument for
JEP 484 - and this family is where the corpus can measure the difference rather than assert it.

`ClassFileProbe` reads a class file's declared major version, name, superclass and members,
and writes one too. The test parses this family's own compiled classes and asserts major
version 68, which is a nice closing of the loop: the branch uses the Java 24 API to confirm
the Java 24 bytecode level it was compiled at.

One incidental finding the test records: the API counts **every declared field**, statics
included, so `ShipmentLeg` reports 5 fields - its 3 record components plus 2 constants - not
the 3 a reader might expect from the record header.

### Fourth and fifth instances of the preview-API diagnostic

At `--release 23` neither lock fails with *"cannot find symbol"*. Both fail with **"is a
preview API and is disabled by default"** - `Gatherers` and `ClassFile` alike, because both
existed in Java 23 as preview. With `String.formatted` at release 14, `ScopedValue` at 21 and
FFM at 21, that is five independent instances across the corpus. The rule is settled: **a
version lock is often a policy error rather than a missing symbol.**

Forward-checked at `--release 25`: clean.

Java 24 is **not an LTS release**. It shipped March 2025 and reached end of life in September
2025, six months later. It is in this corpus to complete the version axis, not as
a recommendation.

Produces: `dist/jv-410.jar or target/jv-410-1.0.0.jar`

## Run

```
java -jar <artifact> O-1234
```

## Test

```
mvn -B test
```

## Workspace projects

- `jv-410-domain/`
- `jv-410-pricing/`
- `jv-410-risk/`
- `jv-410-catalog/`


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
