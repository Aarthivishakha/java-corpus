# Java-Tools-Invalid

A folder of 16 tool-named projects, mirroring the folder names and
boundary-JDK structure of the sibling **Java-Tools-Clean** corpus. This
corpus is the deliberate inverse: each folder is a small **synthetic**
project, engineered so the tool it is named after finds something
genuinely, majority **wrong** -- confirmed by actually installing (or,
where genuinely unreachable, honestly recording as absent) and
invoking the real tool, never by assertion.

This is the negative control of the negative control. Clean proves a
scan that finds nothing really ran; Invalid proves a scan that finds
something really measured it, and that the "something" is the majority
of the fixture, not an edge case. "Declared support is a claim;
invoking is the fact" applies here exactly as it does in Clean.

## Boundary-version structure -- identical to Clean

14 of the 16 tools are exploded into five per-tool subfolders, one per
boundary JDK family -- **2 earliest + 1 middle + 2 latest**, the same
shape as every sibling corpus:

| Family | Role | How it's verified |
| --- | --- | --- |
| `java8` | earliest | **live** -- compiled with JDK 8 (native), the real installed tool run for real |
| `java9` | earliest+1 | **live** -- compiled with `javac --release 9` under a JDK 11 host, the real tool run for real |
| `java16` | middle | **live** -- compiled with `javac --release 16` under a JDK 17 host, the real tool run for real |
| `java24` | latest-1 | **live** -- compiled with `javac --release 24` under a JDK 25 host, the real tool run for real |
| `java25` | latest | **live** -- compiled with JDK 25 (native), the real tool run for real |

Same host-JDK/`--release` mapping as Clean (`openjdk-9-jdk`,
`openjdk-16-jdk` and `openjdk-24-jdk` have no apt package on Ubuntu
24.04, so those three families are cross-compiled under the next-newest
installed LTS JDK, exactly as Clean's own README documents). The domain
source uses no version-gated Java syntax, so the same `.java` files
compile unchanged at every one of the five targets -- only the compiler
invocation differs per family.

The remaining 2 tools -- **diff-cover, pydriller** -- stay single-version
and unversioned, like their counterparts in Clean: diff-cover and
pydriller mine git history and a coverage report, not JDK compatibility.

## Measured results (14 versionable tools x 5 families = 70 cells)

| Tool | java8 | java9 | java16 | java24 | java25 |
| --- | --- | --- | --- | --- | --- |
| ASM-DefUse | **FINDING** | **FINDING** | **FINDING** | **FINDING** | **FINDING** |
| CK | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED |
| CPD (jscpd stand-in) | **FINDING** | **FINDING** | **FINDING** | **FINDING** | **FINDING** |
| Checkstyle | **FINDING** | **FINDING** | **FINDING** | **FINDING** | **FINDING** |
| FindSecBugs (semgrep stand-in) | **FINDING** | **FINDING** | **FINDING** | **FINDING** | **FINDING** |
| Grype | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED |
| JaCoCo | **FINDING** | **FINDING** | **FINDING** | **FINDING** | **FINDING** |
| Lizard | **FINDING** | **FINDING** | **FINDING** | **FINDING** | **FINDING** |
| OWASP Dependency-Check | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED |
| PIT | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED |
| PMD | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED |
| Spoon | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED |
| SpotBugs | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED |
| ba-dua | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED | NOT INSTALLED |

**Tally: 30 FINDING, 0 CLEAN, 40 NOT INSTALLED.** The exact mirror image
of Clean's own 26 CLEAN / 4 FINDING / 40 NOT_INSTALLED tally -- same 70
cells, same split (26+4=30), opposite verdict on every cell that was
genuinely content-dependent in Clean.

Plus the 2 unversioned tools, both FINDING:

| Tool | Result | Command |
| --- | --- | --- |
| diff-cover | **FINDING** -- 45% diff coverage (6/11 new lines missing) | `diff-cover coverage.xml --compare-branch main` |
| pydriller | **FINDING** -- 62.5% of tagged commits (5/8) mismatch their claimed area | `python3 driver.py` |

**NOT INSTALLED** (CK, Grype, OWASP Dependency-Check, PIT, PMD, Spoon,
SpotBugs, ba-dua -- unchanged from Clean, across every family): this
sandbox's egress proxy refuses Maven Central, the Gradle Plugin Portal,
GitHub Release downloads, and the Go module proxy -- the identical,
measured block Clean's own README documents. That blocking has nothing
to do with whether the fixture is Clean or Invalid, so these 8 tools
show the identical result in both corpora; see each tool's own README
for the exact reason.

**ASM-DefUse and JaCoCo are the two tools whose java24/java25 result
has a genuinely different cause than their java8/java9/java16 result**
(see Genuine findings below) -- every other versioned tool's row is
flat (FINDING x5), not because the family never mattered, but because
each of those 4 tools' own planted defect is entirely independent of
target framework.

The exit-code vocabulary throughout this corpus never collapses
findings and not-installed into the same result -- a missing binary
must never masquerade as a genuine result either way.

## Genuine findings (not papered over)

**ASM-DefUse** finds 4 of 7 local-variable definitions in
`TariffLedger.computeTariff` to be dead stores (orphan definitions,
never read) on java8/java9/java16 -- a real def-use result from the
same real, from-source-built `asm-defuse` library Clean's own folder
uses. On java24/java25, the real finding is a different one: apt's
libasm-java 9.7 cannot parse a Java-24/25-targeted class file at all
(`IllegalArgumentException: Unsupported class file major version
68/69`) -- the identical ASM ceiling documented in Clean's own README,
independent of the planted dead stores, and itself a genuine
"not-CLEAN" result either way.

**JaCoCo** finds real, majority-uncovered branch coverage (20.00%) on
java8/java9/java16, from a real JaCoCo instrumenter run against a
deliberately thin test suite. On java24/java25, the same ASM 9.7
ceiling that blocks ASM-DefUse blocks JaCoCo's own offline instrumenter
too (Debian's jacoco package links the system libasm-java) -- again a
genuine environmental ceiling, not a content-dependent result.

Three distinct failure/finding modes split across five families for
each of these two tools, all confirmed by direct invocation -- mirrored
exactly, cell-for-cell, from Clean's own two genuine findings.

## Rules every folder obeys -- same hygiene as Clean, inverted only on purpose

- No suppression anywhere -- no `@SuppressWarnings`, no checkstyle
  `// CHECKSTYLE:OFF`, no jscpd/lizard ignore directives, no semgrep
  `// nosemgrep`. A finding must be real and undisguised.
- Pure ASCII throughout -- verified corpus-wide, 0 non-ASCII bytes in
  any file.
- 0 unintended code clones corpus-wide (verified with jscpd across one
  representative family per tool): only CPD's own deliberately planted
  self-clone remains; no cross-tool collisions.
- Real git history for diff-cover (2 commits on `main`/`feature`, plus
  a `.gitignore`) and pydriller (9 commits: 1 untagged init + 8 tagged).
- Same domain source, unmodified, across all five families of a given
  tool -- only the compiling toolchain (host JDK + `--release` flag)
  differs per family.
- No fabricated tool results. Where a named tool could not be run at
  all, the folder says so and, where a real adjacent tool could stand
  in (CPD -> jscpd, FindSecBugs -> semgrep), that stand-in was actually
  run and its real result reported -- never presented as the named
  tool's own output.
- A different domain, vocabulary and structural idiom in every folder.

## Layout

```text
<Tool Name>/
  README.md              what wrong means for this tool, the per-family results
  java8/ java9/ java16/  each holds its own src/main + src/test copy,
  java24/ java25/        compiled at that family's own bytecode target
  tools/                 shared driver, only where the tool needs one (JaCoCo, ASM-DefUse) -- compiled once, family-independent
  vendor/                only where a dependency was built from source (ASM-DefUse) -- the identical jar Clean's own folder built, shared, family-independent
  security-rules.yml     FindSecBugs's local semgrep ruleset -- byte-for-byte the same as Clean's, shared, family-independent
  .git/                  only where the tool mines real history (diff-cover, pydriller) -- unversioned, untouched
_generator/               family_table.py, generate.py, verify_live.py
```

## Reproducing

```bash
python3 _generator/generate.py       # explode each tool into java8/9/16/24/25
python3 _generator/verify_live.py    # real compile + real tool run for every live tool, every family
```

`verify_live.py` needs JDK 8, 11, 17, and 25 on this host (`apt install
openjdk-8-jdk openjdk-11-jdk openjdk-17-jdk openjdk-25-jdk`), plus the
same `checkstyle`/`lizard`/`jscpd`/`semgrep`/`pydriller`/`diff-cover`
apt/pip/npm packages used by Clean.

## Tool versions used for the measurement

```text
openjdk-8-jdk    8u504-ga-1ubuntu1~24.04.3 (apt)
openjdk-11-jdk   11.0.32.1+1-1ubuntu1~24.04 (apt, --release 9 host)
openjdk-17-jdk   17.0.20.1+1-1~24.04 (apt, --release 16 host)
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

Verified on Linux, Ubuntu 24.04 -- the same build environment as
Java-Tools-Clean's own measurement.
