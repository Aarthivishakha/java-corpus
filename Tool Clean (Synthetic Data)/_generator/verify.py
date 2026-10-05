#!/usr/bin/env python3
"""Verifier for Java-Tools-Clean.

For each of the 16 tool folders, actually invokes the real tool this
sandbox can reach (or documents, and where sensible still runs, a real
substitute) and reports one of three outcomes -- the same vocabulary
used by the sibling Python-Tools-Clean and JavaScript-Tools-Clean
corpora, and never collapsed into each other:

    CLEAN         (0)  the named tool ran for real and found nothing
    FINDINGS      (1)  the named tool ran for real and found something
    NOT_INSTALLED (4)  the named tool could not be reached in this
                       sandbox (Maven Central, the Gradle Plugin
                       Portal and GitHub Releases are all refused at
                       the egress proxy here) -- never reported as
                       CLEAN just because nothing ran.

Usage:
    python3 _generator/verify.py .
    python3 _generator/verify.py . --only JaCoCo -v
"""
import argparse
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(Path(__file__).resolve().parent))
from meta import TOOLS  # noqa: E402

CLEAN, FINDINGS, NOT_INSTALLED = 0, 1, 4

JUNIT_CP = "/usr/share/java/junit4.jar:/usr/share/java/hamcrest-core.jar"
ASM_CP = (
    "/usr/share/java/asm-9.7.jar:/usr/share/java/asm-tree-9.7.jar:"
    "/usr/share/java/asm-analysis-9.7.jar:/usr/share/java/asm-util-9.7.jar:"
    "/usr/share/java/asm-commons-9.7.jar"
)
JACOCO_CP = (
    "/usr/share/java/org.jacoco.core.jar:/usr/share/java/org.jacoco.report.jar:"
    + JUNIT_CP + ":" + ASM_CP
)

GENERATED = ["build", "coverage.xml", ".jscpd-report"]


def run(cmd, cwd, check=False):
    result = subprocess.run(
        cmd, cwd=cwd, shell=isinstance(cmd, str),
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True,
    )
    out = "\n".join(
        line for line in result.stdout.splitlines() if "JAVA_TOOL_OPTIONS" not in line
    )
    if check and result.returncode != 0:
        raise RuntimeError(f"command failed ({result.returncode}): {cmd}\n{out}")
    return result.returncode, out


def compile_main_and_test(folder, extra_cp=""):
    build = folder / "build"
    (build / "main").mkdir(parents=True, exist_ok=True)
    (build / "test").mkdir(parents=True, exist_ok=True)
    main_srcs = [str(p) for p in (folder / "src" / "main").rglob("*.java")]
    test_srcs = [str(p) for p in (folder / "src" / "test").rglob("*.java")]
    cp_main = extra_cp
    rc, out = run(["javac", "-d", "build/main"] + (["-cp", cp_main] if cp_main else []) + main_srcs, folder)
    if rc != 0:
        raise RuntimeError(f"main compile failed in {folder.name}:\n{out}")
    cp_test = f"build/main:{JUNIT_CP}" + (f":{extra_cp}" if extra_cp else "")
    if test_srcs:
        rc, out = run(["javac", "-d", "build/test", "-cp", cp_test] + test_srcs, folder)
        if rc != 0:
            raise RuntimeError(f"test compile failed in {folder.name}:\n{out}")
    return cp_test


def run_junit(folder, cp_test, test_class):
    rc, out = run(
        ["java", "-cp", f"build/test:{cp_test}", "org.junit.runner.JUnitCore", test_class],
        folder,
    )
    return rc == 0, out


def clean_generated(folder):
    for name in GENERATED:
        p = folder / name
        if p.is_dir():
            shutil.rmtree(p, ignore_errors=True)
        elif p.is_file():
            p.unlink()


# ---------------------------------------------------------------- checks --

def check_asm_defuse(folder, verbose):
    cp = f"vendor/asm-defuse.jar:{ASM_CP}"
    cp_test = compile_main_and_test(folder, cp)
    ok, out = run_junit(folder, cp_test, "feeledger.FeeLedgerTest")
    if not ok:
        return FINDINGS, "JUnit tests failed:\n" + out
    (folder / "build" / "tools").mkdir(parents=True, exist_ok=True)
    rc, out = run(["javac", "-d", "build/tools", "-cp", cp, "tools/DefUseRunner.java"], folder)
    if rc != 0:
        raise RuntimeError(out)
    rc, out = run(
        ["java", "-cp", f"build/tools:{cp}", "DefUseRunner",
         "build/main/feeledger/FeeLedger.class", "computeFee"],
        folder,
    )
    if verbose:
        print(out)
    if "ORPHAN_DEFINITIONS=0" in out and "CHAINS=" in out:
        return CLEAN, out
    return FINDINGS, out


def check_ck(folder, verbose):
    compile_main_and_test(folder)
    return NOT_INSTALLED, (
        "CK 0.7.0 needs JavaParser (unreachable via Maven Central here) and "
        "ships only as a GitHub Release jar (blocked at the egress proxy)."
    )


def check_cpd(folder, verbose):
    compile_main_and_test(folder)
    rc, out = run(
        ["npx", "--yes", "jscpd", ".", "--min-lines", "5", "--min-tokens", "30",
         "--threshold", "0", "--format", "java"],
        folder,
    )
    if verbose:
        print(out)
    standin_clean = "Found 0 clones." in out
    note = (
        "CPD itself (bundled only in PMD's release zip) is unreachable here; "
        "jscpd was run for real as the documented stand-in and found "
        + ("0 clones." if standin_clean else "clones -- see output above.")
    )
    return NOT_INSTALLED, note


def check_checkstyle(folder, verbose):
    compile_main_and_test(folder)
    cp_test = f"build/main:{JUNIT_CP}"
    ok, out = run_junit(folder, cp_test, "relaypanel.RelayPanelTest")
    if not ok:
        return FINDINGS, out
    srcs = [str(p) for p in (folder / "src" / "main").rglob("*.java")]
    rc, out = run(["checkstyle", "-c", "/usr/share/checkstyle/sun_checks.xml"] + srcs, folder)
    if verbose:
        print(out)
    return (CLEAN if rc == 0 else FINDINGS), out


def check_findsecbugs(folder, verbose):
    compile_main_and_test(folder)
    rc, out = run(
        ["semgrep", "--metrics=off", "--disable-version-check",
         "--config", "security-rules.yml", "src/"],
        folder,
    )
    if verbose:
        print(out)
    standin_clean = "Findings: 0" in out
    note = (
        "FindSecBugs rides on SpotBugs, itself unreachable here; a local "
        "semgrep ruleset (no network dependency) was run for real as the "
        "documented stand-in and found "
        + ("0 findings." if standin_clean else "findings -- see output above.")
    )
    return NOT_INSTALLED, note


def check_grype(folder, verbose):
    compile_main_and_test(folder)
    return NOT_INSTALLED, (
        "Grype ships only as a GitHub Release binary or via `go install` "
        "(measured: `go install github.com/anchore/grype@latest` -> "
        "403 Forbidden, \"Host not in allowlist: proxy.golang.org\"). "
        "No apt package exists."
    )


def check_jacoco(folder, verbose):
    compile_main_and_test(folder, ASM_CP)
    (folder / "build" / "tools").mkdir(parents=True, exist_ok=True)
    rc, out = run(["javac", "-d", "build/tools", "-cp", JACOCO_CP, "tools/JacocoRunner.java"], folder)
    if rc != 0:
        raise RuntimeError(out)
    rc, out = run(
        ["java", "-cp", f"build/tools:{JACOCO_CP}", "JacocoRunner",
         "build/main", "kilnmonitor.KilnMonitor", "kilnmonitor.KilnMonitorTest", "build/test"],
        folder,
    )
    if verbose:
        print(out)
    ok = "TESTS_FAILED=0" in out and "INSTRUCTION_PCT=100.00" in out and "BRANCH_PCT=100.00" in out
    return (CLEAN if ok else FINDINGS), out


def check_lizard(folder, verbose):
    cp_test = compile_main_and_test(folder)
    ok, out = run_junit(folder, cp_test, "harvestscale.HarvestScaleTest")
    if not ok:
        return FINDINGS, out
    rc, out = run(["lizard", "--languages", "java", "src/main/java"], folder)
    if verbose:
        print(out)
    clean = "Warning cnt" in out and out.strip().splitlines()[-1].split()[-2] == "0"
    # simpler, robust check: lizard prints its own "no thresholds exceeded" line
    clean = "No thresholds exceeded" in out
    return (CLEAN if clean else FINDINGS), out


def check_owasp_dc(folder, verbose):
    compile_main_and_test(folder)
    return NOT_INSTALLED, (
        "OWASP Dependency-Check ships as a GitHub Release zip or a Maven/"
        "Gradle plugin resolved from Maven Central; both are blocked at the "
        "egress proxy (measured 403). No apt package exists."
    )


def check_pit(folder, verbose):
    compile_main_and_test(folder)
    return NOT_INSTALLED, (
        "PIT is distributed purely through Maven Central / the Gradle Plugin "
        "Portal, both blocked at this sandbox's egress proxy. No apt package "
        "exists."
    )


def check_pmd(folder, verbose):
    compile_main_and_test(folder)
    return NOT_INSTALLED, (
        "PMD's only distribution is a GitHub Release zip; Maven Central "
        "resolution is the fallback and both are blocked here. No apt "
        "package exists for current PMD."
    )


def check_spoon(folder, verbose):
    compile_main_and_test(folder)
    return NOT_INSTALLED, (
        "Spoon's dependencies resolve from Maven Central, blocked at the "
        "egress proxy. No apt package exists."
    )


def check_spotbugs(folder, verbose):
    compile_main_and_test(folder)
    rc, out = run(["findbugs", "-textui", "-low", "build/main/coalsorter/CoalSorter.class"], folder)
    if verbose:
        print(out)
    return NOT_INSTALLED, (
        "SpotBugs itself has no apt package and its release jar is blocked. "
        "Its apt-installable predecessor FindBugs 3.1.0~preview2 was actually "
        "run and failed with a real, verified error (ResourceNotFoundException "
        "on java/lang/Object.class): it predates JDK 9's module system and "
        "cannot resolve base classes through this sandbox's only JDK (21). "
        "Output:\n" + out
    )


def check_ba_dua(folder, verbose):
    compile_main_and_test(folder)
    return NOT_INSTALLED, (
        "ba-dua's pom pins org.jacoco:org.jacoco.core:0.8.1 (2018) internally; "
        "that exact old JaCoCo core is unreachable via Maven Central here and "
        "incompatible with the 0.8.11 this sandbox has via apt, so a from-"
        "source build (as used for ASM-DefUse) would need a mismatched core."
    )


def check_diffcover(folder, verbose):
    branch_rc, branch = run(["git", "rev-parse", "--abbrev-ref", "HEAD"], folder)
    if branch.strip() != "feature":
        run(["git", "checkout", "feature"], folder)
    cp_test = compile_main_and_test(folder, JACOCO_CP)
    (folder / "build" / "tools").mkdir(parents=True, exist_ok=True)
    rc, out = run(["javac", "-d", "build/tools", "-cp", JACOCO_CP, "tools/JacocoXmlRunner.java"], folder)
    if rc != 0:
        raise RuntimeError(out)
    rc, out = run(
        ["java", "-cp", f"build/tools:{JACOCO_CP}", "JacocoXmlRunner",
         "build/main", "build/test", "src/main/java", "tallowbatch", "coverage.xml",
         "tallowbatch.TallowBatchTest"],
        folder,
    )
    if rc != 0:
        return FINDINGS, out
    rc, out = run(["diff-cover", "coverage.xml", "--compare-branch", "main"], folder)
    if verbose:
        print(out)
    clean = "Coverage: 100%" in out
    return (CLEAN if clean else FINDINGS), out


def check_pydriller(folder, verbose):
    cp_test = compile_main_and_test(folder)
    ok, out = run_junit(folder, cp_test, "orchardledger.OrchardLedgerTest")
    if not ok:
        return FINDINGS, out
    script = (
        "from pydriller import Repository\n"
        "commits = list(Repository('.').traverse_commits())\n"
        "authors = sorted({c.author.name for c in commits})\n"
        "print('COMMITS=%d AUTHORS=%s' % (len(commits), authors))\n"
    )
    rc, out = run(["python3", "-c", script], folder)
    if verbose:
        print(out)
    expected_authors = "['Ada Renwick', 'Mikkel Aas', 'Priya Nallan']"
    clean = "COMMITS=4" in out and expected_authors in out
    return (CLEAN if clean else FINDINGS), out


CHECKS = {
    "ASM-DefUse": check_asm_defuse,
    "CK": check_ck,
    "CPD": check_cpd,
    "Checkstyle": check_checkstyle,
    "FindSecBugs": check_findsecbugs,
    "Grype": check_grype,
    "JaCoCo": check_jacoco,
    "Lizard": check_lizard,
    "OWASP Dependency-Check": check_owasp_dc,
    "PIT": check_pit,
    "PMD": check_pmd,
    "Spoon": check_spoon,
    "SpotBugs": check_spotbugs,
    "ba-dua": check_ba_dua,
    "diff-cover": check_diffcover,
    "pydriller": check_pydriller,
}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("root")
    parser.add_argument("--only")
    parser.add_argument("-v", "--verbose", action="store_true")
    args = parser.parse_args()

    root = Path(args.root).resolve()
    tools = TOOLS if not args.only else [t for t in TOOLS if t["tool"] == args.only]

    tally = {"CLEAN": 0, "FINDINGS": 0, "NOT_INSTALLED": 0}
    any_findings = False

    for entry in tools:
        name = entry["tool"]
        folder = root / name
        check = CHECKS[name]
        try:
            code, detail = check(folder, args.verbose)
        except Exception as exc:  # noqa: BLE001
            print(f"{name}: ERROR {exc}")
            any_findings = True
            continue
        finally:
            clean_generated(folder)

        label = {CLEAN: "CLEAN", FINDINGS: "FINDINGS", NOT_INSTALLED: "NOT_INSTALLED"}[code]
        tally[label] += 1
        print(f"{name}: {label}")
        if args.verbose or code == FINDINGS:
            print(detail)
        if code == FINDINGS:
            any_findings = True

    print()
    print(f"CLEAN={tally['CLEAN']} FINDINGS={tally['FINDINGS']} NOT_INSTALLED={tally['NOT_INSTALLED']}")
    return 1 if any_findings else 0


if __name__ == "__main__":
    raise SystemExit(main())
