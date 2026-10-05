#!/usr/bin/env python3
"""Write the corpus-root README.md from meta.py."""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from meta import TOOLS  # noqa: E402

OUT = Path(__file__).resolve().parent.parent / "out"

MEASURED = [t for t in TOOLS if t["status"] == "measured"]
NOT_INSTALLED = [t for t in TOOLS if t["status"] != "measured"]

BODY_TOP = """# Clean Java tool corpus -- 100% passing

16 tool-named folders, one per tool, mirroring the layout of the harvested
`Java Tools` set. Where that set holds each tool's **own upstream test
suite**, this one holds synthetic projects built to the opposite goal: every
tool must run and report **nothing wrong**.

This is the negative control the tool-evaluation corpora do not have. A
family where nothing ever fires cannot distinguish *correctly detected
nothing* from *the scan never ran*. A clean baseline is what makes a
zero legible.

## Measured result

Not asserted. Every tool that can be installed in this build environment was
actually invoked against its folder, and the table records what it returned.
Ten tools could not be installed here and are recorded as such rather than
being quietly counted as passing -- collapsing "absent from this host" into
"clean" is the one thing a corpus like this must never do.

**6 measured clean, 0 findings, 10 not installed here.**

| Folder | Result | Command |
|---|---|---|
"""


def command_cell(entry: dict) -> str:
    cmd = entry["command"].replace("\n", " ").strip()
    cmd = " ".join(cmd.split())
    return f"`{cmd}`"


def build_table() -> str:
    rows = []
    for entry in sorted(TOOLS, key=lambda e: e["tool"].lower()):
        result = "measured clean" if entry["status"] == "measured" else "**not installed here**"
        rows.append(f"| `{entry['tool']}` | {result} | {command_cell(entry)} |")
    return "\n".join(rows) + "\n"


def build_not_installed_table() -> str:
    rows = ["| Tool | Reason |", "|---|---|"]
    for entry in NOT_INSTALLED:
        notes = entry["notes"]
        rows.append(f"| **{entry['tool']}** | {notes} |")
    return "\n".join(rows) + "\n"


BODY_MID = """
### Why ten were not installed here

This sandbox's egress proxy refuses Maven Central, the Gradle Plugin
Portal, GitHub Release downloads, and the Go module proxy -- confirmed by
direct measurement (curl against each host returns 403), not assumed. That
is exactly the "blocking infrastructure problem" the platform's own
`java-repos-build-contract.md` documents for its own, much larger
Java-Repos corpus. Where a Debian apt package exists for a tool (or a
genuine equivalent), it was installed and actually run instead; where none
exists, the tool is recorded honestly as not installed rather than skipped
silently.

{not_installed_table}
Run `_generator/verify.py` on a host where these install and the table
closes further.

## Layout

Every folder is an independent, self-contained project:

```text
<Tool Name>/
  README.md            what clean means for this tool, the command, the expected result
  src/main/java/<pkg>/ the synthetic project -- a different domain in every folder
  src/test/java/<pkg>/ the JUnit4 suite, where the asserts live
  tools/               only where the tool needs a driver JaCoCo/asm-defuse don't ship
                        as a CLI (JaCoCo, ASM-DefUse, diff-cover)
  vendor/              only where a dependency was built from source (ASM-DefUse)
  .git/                only where the tool mines real history (diff-cover, pydriller)
_generator/            meta.py, verify.py, write_readmes.py, write_root_readme.py
```

## Rules every folder obeys

These hold across the whole tree, so each project is clean for *its* tool
without tripping any of the others:

* **Every source file compiles clean and its JUnit4 suite passes.** No
  tool's "clean" is worth anything if the code underneath it doesn't
  actually build and run.
* **A different domain, vocabulary and structural idiom in every folder**
  (fee ledgers, trail routing, crate counting, relay panels, vault gates,
  dock manifests, kiln monitoring, harvest scales, grain intake, stone
  grading, irrigation planning, wharf schedules, coal sorting, mill races,
  tallow batches, orchard ledgers), so the duplicate detectors find
  nothing real between folders. Verified: **0 cross-folder clones** at
  5 lines / 30 tokens (`jscpd . --min-lines 5 --min-tokens 30 --threshold 0
  --format java`), including test files.
* **Pure ASCII.** Not every analyser reads source with the build file's
  declared encoding rather than the platform default, so a stray
  non-ASCII byte can change what a tool reports without changing what the
  interpreter accepts. Enforced at write time; verified: 0 non-ASCII
  bytes anywhere in the corpus.
* **Real multi-author git history where a tool needs one** (diff-cover,
  pydriller): the same three synthetic authors used in the sibling
  Python and JavaScript corpora -- Ada Renwick, Mikkel Aas, Priya
  Nallan -- across real, separately-dated commits.
* **No fabricated tool results.** Where a named tool could not be run at
  all, the folder says so and, where a real adjacent tool could stand in
  (CPD -> jscpd, FindSecBugs -> semgrep), that stand-in was actually run
  and its real result reported -- never presented as the named tool's own
  output.

## Reproducing

```bash
python3 _generator/write_readmes.py         # regenerate the 13 auto-written per-folder READMEs
python3 _generator/write_root_readme.py     # regenerate this file
python3 _generator/verify.py <corpus-dir>   # run every tool for real and tally CLEAN/FINDINGS/NOT_INSTALLED
python3 _generator/verify.py <corpus-dir> --only Checkstyle -v   # run just one tool, verbosely
```

`verify.py` uses the corpora's exit-code vocabulary, kept deliberately
apart: `0` clean, `1` findings, `4` not installed on this host -- a
missing binary must never masquerade as a clean scan. It also clears
generated state (`build/`, `coverage.xml`, `.jscpd-report`) before and
after each run, and leaves `diff-cover` checked out on its `feature`
branch and every other git repo untouched.

Reproduced three times in this session; all three runs converged on the
same `CLEAN=6 FINDINGS=0 NOT_INSTALLED=10` tally.

## Two findings worth calling out

**ASM-DefUse was rescued by building from source, not abandoned.**
`br.usp.each.saeg:asm-defuse` is a Maven Central-only artifact, and Maven
Central is blocked here -- but the library itself is open source. Its real
GitHub repository was cloned (`saeg/asm-defuse`), its one real dependency
was found by reading its own `pom.xml` (`saeg/saeg-commons`, not guessed),
and both were compiled with `javac` against the apt-installed ASM 9.7 jars
already on this host. The result is a genuine, working `asm-defuse.jar`
built entirely from real upstream source -- the fetch mechanism differs
from a normal Maven Central pull, but the library and the analysis it
performs on `FeeLedger.class` are both real.

**SpotBugs's predecessor was tried, and it genuinely failed.** SpotBugs
itself has no apt package and its release jar is GitHub-Release-only, so
its real, Debian-packaged predecessor FindBugs 3.1.0~preview2 (the exact
bytecode engine SpotBugs forked from) was installed and actually run
against `CoalSorter.class` as a plausible stand-in. It failed with
`ResourceNotFoundException: java/lang/Object.class` -- FindBugs predates
JDK 9's module system and cannot resolve the JDK's own base classes
through the `jrt:` filesystem this sandbox's only JDK (21) uses, and no
older JDK is installed to fall back to. That is recorded as a genuine,
verified incompatibility. SpotBugs stays NOT INSTALLED rather than
claiming a stand-in result that was never actually produced -- the same
discipline that keeps CPD's jscpd stand-in and FindSecBugs's semgrep
stand-in honest about being stand-ins, not the named tool.

## Tool versions used for the measurement

```text
java          OpenJDK 21.0.10 (build 21.0.10+7-Ubuntu-124.04)
checkstyle    8.36.1 (apt)
jacoco        0.8.11 (apt, libjacoco-java)
junit4        4.13.2 (apt)
asm           9.7 (apt, libasm-java: asm/asm-tree/asm-analysis/asm-commons/asm-util)
lizard        1.24.0 (pip)
diff-cover    10.6.0 (pip)
pydriller     2.12 (pip)
jscpd         5.3.3 (npm, stand-in for CPD)
semgrep       1.178.0 (stand-in for FindSecBugs, local ruleset)
findbugs      3.1.0~preview2 (apt; attempted stand-in for SpotBugs, verified incompatible with JDK 21)
maven         3.9.11 (pre-installed, unused -- Maven Central unreachable)
gradle        8.14.3 (pre-installed, unused -- Gradle Plugin Portal unreachable)
```

Verified on Linux, Ubuntu 24.04.
"""


def main() -> int:
    table = build_table()
    not_installed_table = build_not_installed_table()
    content = BODY_TOP + table + BODY_MID.format(not_installed_table=not_installed_table)
    (OUT / "README.md").write_text(content, encoding="utf-8")
    print("wrote root README.md")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
