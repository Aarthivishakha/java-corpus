#!/usr/bin/env python3
"""Writes the corpus-root README.md reflecting the java8/java9/java16/
java24/java25 boundary-family structure, mirroring the sibling
Python-Tools-Clean and JavaScript-Tools-Clean root READMEs.
"""
import json
from pathlib import Path

from family_table import (
    FAMILIES, HOST_JDK, JDK_BLIND_LIVE, BYTECODE_SENSITIVE_LIVE,
    NOT_INSTALLED_PERMANENT, UNVERSIONED, FINDINGS,
)

ROOT = Path(__file__).resolve().parent.parent
live_results = json.loads((Path(__file__).resolve().parent / "live_results.json").read_text())

VERSIONABLE = sorted(
    list(JDK_BLIND_LIVE) + list(BYTECODE_SENSITIVE_LIVE) + NOT_INSTALLED_PERMANENT
)

STANDIN_NOTE = {
    "CPD": " (jscpd stand-in)",
    "FindSecBugs": " (semgrep stand-in)",
}


def cell(tool, fam):
    if tool in NOT_INSTALLED_PERMANENT:
        return "NOT INSTALLED"
    status = live_results[tool][fam]["status"]
    return "**FINDING**" if status == "FINDING" else status


def build_table():
    rows = []
    for tool in VERSIONABLE:
        label = tool + STANDIN_NOTE.get(tool, "")
        cells = " | ".join(cell(tool, fam) for fam in FAMILIES)
        rows.append(f"| {label} | {cells} |")
    return "\n".join(rows)


def tally():
    clean = finding = not_installed = 0
    for tool in VERSIONABLE:
        for fam in FAMILIES:
            c = cell(tool, fam)
            if c == "CLEAN":
                clean += 1
            elif c == "**FINDING**":
                finding += 1
            else:
                not_installed += 1
    return clean, finding, not_installed


BODY = """# Java-Tools-Clean

A folder of 16 tool-named projects, mirroring the folder names in the
harvested `Java Tools` directory (each of which holds that tool's own
real upstream test suite). This corpus is the opposite of that: each
folder is a small **synthetic** project, engineered so that the tool it
is named after finds **nothing wrong** -- the same negative-control
methodology already used for the sibling `Python-Tools-Clean` and
`JavaScript-Tools-Clean` corpora.

A family where nothing ever fires cannot tell "correctly detected
nothing" apart from "the scan never ran." A clean baseline is what makes
a zero legible: every result below was produced by actually installing
(or, where genuinely unreachable, honestly recording as absent) and
invoking the real tool, not by assertion. "Declared support is a claim;
invoking is the fact."

## Boundary-version structure

14 of the 16 tools are exploded into five per-tool subfolders, one per
boundary JDK family, mirroring `Python-Tools-Clean`'s `py3.X/` and
`JavaScript-Tools-Clean`'s `nodeXX/` pattern:

| Family | Role | How it's verified |
| --- | --- | --- |
| `java8` | earliest | **live** -- compiled with JDK 8 (native), the real installed tool run for real |
| `java9` | earliest+1 | **live** -- compiled with `javac --release 9` under a JDK 11 host, the real tool run for real |
| `java16` | middle | **live** -- compiled with `javac --release 16` under a JDK 17 host, the real tool run for real |
| `java24` | latest-1 | **live** -- compiled with `javac --release 24` under a JDK 25 host, the real tool run for real |
| `java25` | latest | **live** -- compiled with JDK 25 (native), the real tool run for real |

Unlike the sibling Python/JavaScript corpora, **no family here is
code-only.** Those corpora needed a code-only tier because their
earliest interpreter/runtime versions genuinely could not execute a
modern release of the tool being tested. That problem doesn't arise for
Java: every boundary JDK from 8 to 25 can run Checkstyle 8.36.1, Lizard,
jscpd and semgrep identically (they only read source text), and the real
failure mode here shows up at the *opposite* end of the range instead --
see Genuine findings below.

`openjdk-8-jdk` and `openjdk-25-jdk` are Ubuntu 24.04's own apt packages
(both LTS releases); `openjdk-9-jdk`, `openjdk-16-jdk` and
`openjdk-24-jdk` have **no apt package at all** (confirmed directly:
`apt-cache policy` returns nothing for all three -- only the LTS releases
8/11/17/21/25 are packaged). Each of those three families is therefore
produced by cross-compiling with `javac --release N` under the next
newest already-installed LTS JDK as host -- exactly the pattern the
platform's own `java-repos-build-contract.md` documents for its much
larger sibling corpus. Verified directly: `--release 9` under JDK 11
produces a real class file major version 53; `--release 16` under JDK 17
produces major 60; `--release 24` under JDK 25 produces major 68 --
exactly the values the JVM spec assigns those releases.

The domain source uses no version-gated Java syntax (no `var`, records,
sealed types, or pattern matching), so the same `.java` files compile
unchanged at every one of the five targets -- only the compiler
invocation (host JDK + `--release` flag) differs per family, mirroring
the "same domain source, unmodified, across all families" rule already
used in the sibling corpora.

The remaining 2 tools -- **diff-cover, pydriller** -- are git-history/
coverage-diff tools and stay single-version and unversioned, exactly
like Python's and JavaScript's diff-cover/pydriller pair, since what they
measure is commit history and a coverage report, not JDK compatibility.

## Measured results (14 versioned tools x 5 families = 70 cells)

| Tool | java8 | java9 | java16 | java24 | java25 |
| --- | --- | --- | --- | --- | --- |
{table}

**Tally: {clean} CLEAN, {finding} FINDING, {not_installed} NOT INSTALLED.**

Plus the 2 unversioned tools, both CLEAN:

| Tool | Result | Command |
| --- | --- | --- |
| diff-cover | CLEAN | `diff-cover coverage.xml --compare-branch main` |
| pydriller | CLEAN | `python3 -c "from pydriller import Repository; ..."` |

**NOT INSTALLED** (CK, Grype, OWASP Dependency-Check, PIT, PMD, Spoon,
SpotBugs, ba-dua -- unchanged from the original single-version build,
across every family): this sandbox's egress proxy refuses Maven Central,
the Gradle Plugin Portal, GitHub Release downloads, and the Go module
proxy -- confirmed by direct measurement, not assumed. That blocking is
the same regardless of which JDK compiles the domain source, so these 8
tools show the identical result in all 5 families; see each tool's own
README for the exact reason and what was checked.

The exit-code vocabulary throughout this corpus never collapses 1
(findings), 3 (skipped), and 4 (not installed) into the same result -- a
missing binary must never masquerade as a clean scan, and neither must
"this family can't run it."

## Genuine findings (not papered over)

**ASM-DefUse / java24, java25** and **JaCoCo / java24, java25** -- both
tools depend, directly or (for JaCoCo, via Debian's unbundled packaging)
indirectly, on the apt-installed `libasm-java` 9.7. Verified directly:
ASM 9.7's `ClassReader` throws `IllegalArgumentException: Unsupported
class file major version 68` (java24) and `...69` (java25) -- it simply
does not parse Java 24/25 bytecode. ASM 9.8, which adds that support, is
not reachable here (Maven Central, where it would come from, is blocked
at the egress proxy). Confirmed two independent ways for JaCoCo: via its
in-process `Instrumenter` API and via the real `-javaagent` runtime jar
attached to a live JVM -- both fail identically. No upgrade path exists
today; this is left as a genuine, reproducible, documented incompatibility
rather than patched around. Every other cell for both tools (java8,
java9, java16) is independently measured CLEAN: the same real
`DefUseRunner`/`JacocoRunner` drivers, run against that family's own
really-compiled bytecode, actually produce
`ORPHAN_DEFINITIONS=0`/`100.00%` coverage.

Both findings are baked into the generator (`family_table.py`'s
`FINDINGS`, consumed by `verify_live.py` and the README writers), not
just hand-noted -- rerunning `verify_live.py` reproduces them.

## Rules every folder obeys

- No dependency on any other folder in this corpus -- each is its own Java project (or, for diff-cover/pydriller, its own real invocation of an external CLI).
- Pure ASCII throughout -- verified corpus-wide, 0 non-ASCII bytes in any file.
- 0 code clones corpus-wide (verified with jscpd, `--min-lines 5 --min-tokens 30 --threshold 0`, Java files): every folder's domain, vocabulary, and control-flow shape is deliberately distinct from every other folder's.
- Real git history (3 synthetic authors, real commits) for the two tools that mine history: diff-cover (plus a real feature-branch diff) and pydriller.
- Same domain source, unmodified, across all five families of a given tool -- only the compiling JDK and `--release` flag differ per family.
- No fabricated tool results. Where a named tool could not be run at all, the folder says so and, where a real adjacent tool could stand in (CPD -> jscpd, FindSecBugs -> semgrep), that stand-in was actually run in every family and its real result reported -- never presented as the named tool's own output.

## Layout

```text
<Tool Name>/
  README.md              what clean means for this tool, the per-family results
  java8/ java9/ java16/  each holds its own src/main + src/test copy,
  java24/ java25/        compiled at that family's own bytecode target
  tools/                 shared driver, only where the tool needs one (JaCoCo, ASM-DefUse) -- compiled once, family-independent
  vendor/                only where a dependency was built from source (ASM-DefUse) -- shared, family-independent
  security-rules.yml     FindSecBugs's local semgrep ruleset -- shared, family-independent
  .git/                  only where the tool mines real history (diff-cover, pydriller) -- unversioned, untouched
_generator/               family_table.py, generate.py, verify_live.py, write_tool_readmes.py, write_family_readmes.py, write_new_root_readme.py, meta.py, verify.py (original single-version tally)
```

## Reproducing

```bash
python3 _generator/write_tool_readmes.py     # regenerate the 13 auto-written base per-folder READMEs from meta.py
python3 _generator/generate.py               # explode each versionable tool into java8/9/16/24/25
python3 _generator/verify_live.py            # real compile + real tool run for every live tool, every family
python3 _generator/write_family_readmes.py   # append the per-family results section to each tool's README
python3 _generator/write_new_root_readme.py  # regenerate this file
```

`generate.py` and `verify_live.py` read `family_table.py` for the host-JDK/
`--release` mapping per family (including the `FINDINGS` documented
above) and need JDK 8, 11, 17, 21 and 25 on this host (`apt install
openjdk-8-jdk openjdk-11-jdk openjdk-17-jdk openjdk-25-jdk`; JDK 21 was
already present). `Checkstyle`/`Lizard`/`CPD`(jscpd)/`FindSecBugs`(semgrep)
need the same apt/pip/npm packages as the original single-version build.

## Tool versions used for the measurement

```text
openjdk-8-jdk    8u504-ga-1ubuntu1~24.04.3 (apt)
openjdk-11-jdk   11.0.32.1+1-1ubuntu1~24.04 (apt, --release 9 host)
openjdk-17-jdk   17.0.20.1+1-1~24.04 (apt, --release 16 host)
openjdk-21-jdk   21.0.10+7-1~24.04 (apt, pre-existing, unused as a family host this round)
openjdk-25-jdk   25.0.4.1+1-1~24.04.4 (apt, --release 24 host and java25 native)
checkstyle       8.36.1 (apt)
jacoco           0.8.11 (apt, libjacoco-java -- links system libasm-java, not a bundled ASM)
junit4           4.13.2 (apt)
asm              9.7 (apt, libasm-java -- confirmed ceiling: class file major version 67 / Java 23)
lizard           1.24.0 (pip)
diff-cover       10.6.0 (pip)
pydriller        2.12 (pip)
jscpd            5.3.3 (npm, stand-in for CPD)
semgrep          1.178.0 (stand-in for FindSecBugs, local ruleset)
```

Verified on Linux, Ubuntu 24.04.
"""


def main():
    clean, finding, not_installed = tally()
    content = BODY.format(
        table=build_table(), clean=clean, finding=finding, not_installed=not_installed
    )
    (ROOT / "README.md").write_text(content, encoding="utf-8")
    print(f"wrote root README.md ({clean} CLEAN, {finding} FINDING, {not_installed} NOT INSTALLED)")


if __name__ == "__main__":
    main()
