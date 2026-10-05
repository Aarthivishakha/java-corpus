#!/usr/bin/env python3
"""Single source-of-truth boundary-family table for Java-Tools-Invalid.

Mirrors Java-Tools-Clean's own family_table.py exactly -- same five
boundary JDKs, same host-JDK/--release mapping, same tool
classification -- since the inverse corpus uses the identical
toolchain mechanics; only the domain fixtures differ (engineered to
find something genuinely wrong instead of nothing).

Boundary versions: java8, java9, java16, java24, java25 (2 earliest +
1 middle + 2 latest of the corpus's own five-JDK span) -- unchanged
from Clean.
"""

FAMILIES = ["java8", "java9", "java16", "java24", "java25"]

HOST_JDK = {
    "java8": {"jdk": "/usr/lib/jvm/java-8-openjdk-amd64", "release": None, "expect_major": 52},
    "java9": {"jdk": "/usr/lib/jvm/java-11-openjdk-amd64", "release": "9", "expect_major": 53},
    "java16": {"jdk": "/usr/lib/jvm/java-17-openjdk-amd64", "release": "16", "expect_major": 60},
    "java24": {"jdk": "/usr/lib/jvm/java-25-openjdk-amd64", "release": "24", "expect_major": 68},
    "java25": {"jdk": "/usr/lib/jvm/java-25-openjdk-amd64", "release": None, "expect_major": 69},
}

# Fixed JDK used to compile+run the bytecode-level driver programs
# (DefUseRunner, JacocoRunner) -- same as Clean, same reason: both link
# against classes built at bytecode major 65, unreadable by javac older
# than JDK21.
TOOL_JDK = "/usr/lib/jvm/java-25-openjdk-amd64"

# JDK-version-blind: pure source (or source-level stand-in) analysis --
# same 4 tools as Clean, each fixture now engineered to genuinely fail
# that tool's own real check.
JDK_BLIND_LIVE = {
    "Checkstyle": {
        "domain_dir": "valvestation",
        "command": "checkstyle -c sun_checks.xml src/main/java/**/*.java",
    },
    "Lizard": {
        "domain_dir": "siloweigher",
        "command": "lizard --languages java src/main/java -C 10 -L 30 -a 3 -w",
    },
    "CPD": {
        "domain_dir": "parceltally",
        "command": "npx jscpd . --min-lines 5 --min-tokens 30 --threshold 0",
        "standin": "jscpd",
    },
    "FindSecBugs": {
        "domain_dir": "strongroomaccess",
        "command": "semgrep --config security-rules.yml src/",
        "standin": "semgrep (local ruleset, not the registry p/java+p/security-audit packs)",
    },
}

# Bytecode-version-sensitive: real tool, real dependency on being able to
# *parse* the compiled .class file, capped by the apt-installed ASM 9.7's
# own version ceiling -- same ceiling as Clean (confirmed directly
# against class files of major 52/53/60/68/69), independent of this
# corpus's own planted defects.
BYTECODE_SENSITIVE_LIVE = {
    "ASM-DefUse": {
        "domain_dir": "tariffledger",
        "domain_class": "tariffledger.TariffLedger",
        "domain_file": "TariffLedger",
        "test_class": "tariffledger.TariffLedgerTest",
        "test_file": "TariffLedgerTest",
        "command": "java -cp <asm-defuse.jar>:<asm jars> DefUseRunner TariffLedger.class computeTariff",
    },
    "JaCoCo": {
        "domain_dir": "furnacegauge",
        "domain_class": "furnacegauge.FurnaceGauge",
        "domain_file": "FurnaceGauge",
        "test_class": "furnacegauge.FurnaceGaugeTest",
        "test_file": "FurnaceGaugeTest",
        "command": (
            "java -javaagent:jacocoagent.jar=destfile=jacoco.exec -cp ... "
            "org.junit.runner.JUnitCore FurnaceGaugeTest && "
            "java -jar jacococli.jar report jacoco.exec --classfiles ... --xml coverage.xml"
        ),
    },
}

# Permanently not installed, regardless of JDK family -- unchanged from
# Clean: the blocking factor is this sandbox's egress proxy (Maven
# Central / Gradle Plugin Portal / GitHub Releases / the Go module
# proxy all refused), not any JDK version, and not anything about this
# corpus being Clean or Invalid. Same reason, same command, across all
# five families, in both corpora.
NOT_INSTALLED_PERMANENT = [
    "CK", "Grype", "OWASP Dependency-Check", "PIT", "PMD", "Spoon",
    "SpotBugs", "ba-dua",
]

UNVERSIONED = ["diff-cover", "pydriller"]

# Genuine, reproduced, documented findings for the two bytecode-sensitive
# tools' java24/java25 cells -- identical root cause to Clean's own
# FINDINGS (ASM 9.7's version ceiling), independent of this corpus's own
# planted defects, which is why these two cells are "FINDING" rather
# than the planted-defect result the other three families show.
FINDINGS = {
    ("ASM-DefUse", "java24"): (
        "apt's libasm-java 9.7 (also what org.jacoco.core links against on "
        "Debian) cannot parse a Java-24-targeted class file: "
        "`ClassReader.<init>` throws `IllegalArgumentException: Unsupported "
        "class file major version 68`. Same ceiling as the sibling Clean "
        "corpus's own ASM-DefUse folder -- independent of which domain "
        "source was compiled."
    ),
    ("ASM-DefUse", "java25"): (
        "Same root cause one major version further: major version 69 "
        "(Java 25) is also past ASM 9.7's ceiling. Same "
        "`IllegalArgumentException: Unsupported class file major version "
        "69`, same unreachable fix."
    ),
    ("JaCoCo", "java24"): (
        "JaCoCo's offline instrumenter uses the same apt-installed ASM 9.7 "
        "internally. Instrumenting a Java-24-targeted FurnaceGauge.class "
        "throws the identical `IllegalArgumentException: Unsupported "
        "class file major version 68` inside `InstrSupport.classReaderFor`."
    ),
    ("JaCoCo", "java25"): (
        "Same ASM 9.7 ceiling at major version 69 (Java 25). Identical "
        "failure, identical unreachable fix."
    ),
}
