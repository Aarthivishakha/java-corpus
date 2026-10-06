# Testable Java corpus — JV_V25_ANTIVY_WAR_MONO

Grid cell `ANT-WAR-M` of the 24-cell Java grid.

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
| Java version | 25 |
| Host JDK | 25 |
| Build system | Ant + Ivy |
| Packaging | WAR |
| Architecture | Monolith |

## Supported tools

19 tools are wired on this branch (one `Tool Triggering (Synthetic Data)/<dir>/` folder
each). **13 of them run on JDK 25 (host JDK 25); 6 do not.**

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

Main and test sources both compile at Java 25 (bytecode major version 69). The code
uses stream gatherers (`Stream.gather`, final in 24), flexible constructor bodies,
module import declarations and scoped values (all final in 25), plus Markdown `///` doc
comments (final in 23), so it fails to compile under `--release 21` — the version differentiation is real, not
declared.

Produces: `jv-119.war`

## Run

```
java -jar <artifact> O-1234
```

## Test

```
ant test
```

## Workspace projects

- `src/main/java/` (single module)


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
