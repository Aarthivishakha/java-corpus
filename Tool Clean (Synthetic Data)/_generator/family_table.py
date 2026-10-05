#!/usr/bin/env python3
"""Single source-of-truth boundary-family table for Java-Tools-Clean.

Mirrors Python-Tools-Clean's pin_table.py / JavaScript-Tools-Clean's
pin_table.py, adapted to what actually varies for a JVM corpus: not a
package-manager pin, but which JDK compiles each family's domain source
into which bytecode target, and whether the real, installed tool chain
can consume that bytecode.

Boundary versions requested: java8, java9, java16, java24, java25
(2 earliest + 1 middle + 2 latest of the corpus's own five-JDK span).

Empirically established this session (not assumed):
  * openjdk-8-jdk and openjdk-25-jdk are apt-installable directly.
  * openjdk-9-jdk, openjdk-16-jdk, openjdk-24-jdk have NO apt package on
    Ubuntu 24.04 (only the LTS releases 8/11/17/21/25 do) -- confirmed
    via `apt-cache policy`, empty result for all three.
  * Each non-LTS family is therefore produced by compiling with
    `javac --release N` under the next-newest *installed* LTS JDK as
    host -- exactly the pattern the platform's own
    java-repos-build-contract.md documents for its much larger sibling
    corpus ({9,10,11}->JDK11 host, {12-17}->JDK17 host, {22-25}->JDK25
    host). Verified directly: `--release 9` under JDK11 produces a real
    class file major version 53; `--release 16` under JDK17 produces
    major 60; `--release 24` under JDK25 produces major 68.
  * The corpus's own domain source uses no version-gated syntax (no
    var, records, sealed types, pattern matching), so -- exactly like
    the sibling Python/JavaScript corpora's "same domain source,
    unmodified, across every family" rule -- the exact same .java files
    compile unchanged at every one of the five targets. Only the
    compiler invocation (host JDK + --release flag) differs per family.
"""

FAMILIES = ["java8", "java9", "java16", "java24", "java25"]

# Host JDK used to produce each family's own bytecode, and the --release
# flag passed to it (empty = native compile, no cross-target needed).
HOST_JDK = {
    "java8": {"jdk": "/usr/lib/jvm/java-8-openjdk-amd64", "release": None, "expect_major": 52},
    "java9": {"jdk": "/usr/lib/jvm/java-11-openjdk-amd64", "release": "9", "expect_major": 53},
    "java16": {"jdk": "/usr/lib/jvm/java-17-openjdk-amd64", "release": "16", "expect_major": 60},
    "java24": {"jdk": "/usr/lib/jvm/java-25-openjdk-amd64", "release": "24", "expect_major": 68},
    "java25": {"jdk": "/usr/lib/jvm/java-25-openjdk-amd64", "release": None, "expect_major": 69},
}

# Fixed JDK used to compile+run the two bytecode-level driver programs
# (DefUseRunner, JacocoRunner). Both link against classes built at
# bytecode major 65 (asm-defuse.jar / org.jacoco.core's own apt build),
# which no javac older than JDK21 can even read as a compile-time
# dependency -- confirmed directly (JDK11/JDK17 javac both refuse to
# read asm-defuse.jar's classes: "class file has wrong version 65.0").
# The driver's own execution JDK is independent of which family's
# bytecode it is pointed at: it reads the target .class file as a byte
# array (ASM ClassReader / JaCoCo Instrumenter), a data-format parse
# capped by the apt-installed ASM 9.7's own version ceiling -- not by
# the executing JVM's class-loading limit. Verified both ways directly.
TOOL_JDK = "/usr/lib/jvm/java-25-openjdk-amd64"

# --- Tool classification (empirically determined this session) -----------

# JDK-version-blind: pure source (or source-level stand-in) analysis.
# Verified to produce the identical real result under every family's own
# host JDK -- Checkstyle and Lizard don't care what compiled the class,
# only what the source says; CPD's jscpd stand-in and FindSecBugs's
# semgrep stand-in are both source/token-window tools with no JDK
# dependency either.
JDK_BLIND_LIVE = {
    "Checkstyle": {
        "domain_dir": "relaypanel",
        "command": "checkstyle -c sun_checks.xml src/main/java/**/*.java",
    },
    "Lizard": {
        "domain_dir": "harvestscale",
        "command": "lizard --languages java src/main/java",
    },
    "CPD": {
        "domain_dir": "cratecounter",
        "command": "npx jscpd . --min-lines 5 --min-tokens 30 --threshold 0",
        "standin": "jscpd",
    },
    "FindSecBugs": {
        "domain_dir": "vaultgate",
        "command": "semgrep --config security-rules.yml src/",
        "standin": "semgrep (local ruleset, not the registry p/java+p/security-audit packs)",
    },
}

# Bytecode-version-sensitive: real tool, real dependency on being able to
# *parse* the compiled .class file, capped by the apt-installed ASM 9.7's
# own version ceiling (max supported class file major version 67 = Java
# 23) -- confirmed directly against class files of major 52/53/60/68/69.
BYTECODE_SENSITIVE_LIVE = {
    "ASM-DefUse": {
        "domain_dir": "feeledger",
        "domain_class": "feeledger.FeeLedger",
        "domain_file": "FeeLedger",
        "test_class": "feeledger.FeeLedgerTest",
        "test_file": "FeeLedgerTest",
        "command": "java -cp <asm-defuse.jar>:<asm jars> DefUseRunner FeeLedger.class computeFee",
    },
    "JaCoCo": {
        "domain_dir": "kilnmonitor",
        "domain_class": "kilnmonitor.KilnMonitor",
        "domain_file": "KilnMonitor",
        "test_class": "kilnmonitor.KilnMonitorTest",
        "test_file": "KilnMonitorTest",
        "command": (
            "java -javaagent:jacocoagent.jar=destfile=jacoco.exec -cp ... "
            "org.junit.runner.JUnitCore KilnMonitorTest && "
            "java -jar jacococli.jar report jacoco.exec --classfiles ... --xml coverage.xml"
        ),
    },
}

# Permanently not installed, regardless of JDK family: the blocking
# factor is this sandbox's egress proxy (Maven Central / Gradle Plugin
# Portal / GitHub Releases / the Go module proxy all refused, confirmed
# by direct measurement -- see the existing single-version README), not
# any JDK version. Same reason, same command, across all five families.
NOT_INSTALLED_PERMANENT = [
    "CK", "Grype", "OWASP Dependency-Check", "PIT", "PMD", "Spoon",
    "SpotBugs", "ba-dua",
]

# Untouched: git-history / language-agnostic tools, exactly like the
# diff-cover/pydriller pair in the sibling Python and JavaScript corpora.
UNVERSIONED = ["diff-cover", "pydriller"]

# Genuine, reproduced, documented findings -- not patched around, because
# there is no fix: ASM 9.7 is the latest release apt/Maven-Central-reachable
# in this sandbox and it does not support Java 24/25 class files (that
# support lands in ASM 9.8, unreleased/unreachable here).
FINDINGS = {
    ("ASM-DefUse", "java24"): (
        "apt's libasm-java 9.7 (also what org.jacoco.core links against on "
        "Debian) cannot parse a Java-24-targeted class file: "
        "`ClassReader.<init>` throws `IllegalArgumentException: Unsupported "
        "class file major version 68`. ASM 9.7 is the latest release "
        "reachable in this sandbox (Maven Central, where 9.8 would come "
        "from, is blocked at the egress proxy); no upgrade path exists here."
    ),
    ("ASM-DefUse", "java25"): (
        "Same root cause one major version further: major version 69 "
        "(Java 25) is also past ASM 9.7's ceiling. Same "
        "`IllegalArgumentException: Unsupported class file major version "
        "69`, same unreachable fix."
    ),
    ("JaCoCo", "java24"): (
        "JaCoCo's offline instrumenter (`org.jacoco.core.instr.Instrumenter`) "
        "uses the same apt-installed ASM 9.7 internally (Debian's jacoco "
        "package links the system libasm-java rather than bundling its "
        "own copy). Instrumenting a Java-24-targeted KilnMonitor.class "
        "throws the identical `IllegalArgumentException: Unsupported "
        "class file major version 68` inside "
        "`InstrSupport.classReaderFor`. Confirmed two ways: via the "
        "in-process `Instrumenter` API and via the real `-javaagent` "
        "runtime jar attached to a live JVM, both fail identically."
    ),
    ("JaCoCo", "java25"): (
        "Same ASM 9.7 ceiling at major version 69 (Java 25). Identical "
        "failure, identical unreachable fix."
    ),
}
