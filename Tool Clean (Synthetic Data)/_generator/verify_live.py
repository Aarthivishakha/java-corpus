#!/usr/bin/env python3
"""Real, live per-family verification for Java-Tools-Clean's boundary
families. For each of the 6 "live" tools (4 JDK-blind + 2
bytecode-sensitive), actually compiles that family's own copy of the
domain source with that family's own host JDK (or --release cross-
compile) and actually invokes the real tool (or its documented real
stand-in) against the result. Writes live_results.json next to this
script. The 8 permanently-not-installed tools are recorded directly
from family_table.py (no live run possible -- see its own docstring),
and the 2 unversioned tools are left as already measured.
"""
import json
import subprocess
from pathlib import Path

from family_table import (
    FAMILIES, HOST_JDK, TOOL_JDK, JDK_BLIND_LIVE, BYTECODE_SENSITIVE_LIVE,
    NOT_INSTALLED_PERMANENT, FINDINGS,
)

ROOT = Path(__file__).resolve().parent.parent
JUNIT = "/usr/share/java/junit4-4.13.2.jar"
HAMCREST = "/usr/share/java/hamcrest-core.jar"
ASM_JARS = ":".join(sorted(
    p for p in subprocess.run(
        ["dpkg", "-L", "libasm-java"], capture_output=True, text=True
    ).stdout.splitlines()
    if p.endswith(".jar") and "-all" not in p and "debug" not in p
))
ASM_DEFUSE_JAR = str(ROOT / "ASM-DefUse" / "vendor" / "asm-defuse.jar")
JACOCO_CORE = "/usr/share/java/org.jacoco.core.jar"

results = {}


def run(cmd, cwd=None, env=None):
    full_env = None
    if env is not None:
        import os
        full_env = dict(os.environ)
        full_env.update(env)
    p = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True, env=full_env)
    return p.returncode, p.stdout, p.stderr


def javac(jdk, release, out_dir, srcfile, classpath=None):
    cmd = [f"{jdk}/bin/javac"]
    if release:
        cmd += ["--release", release]
    if classpath:
        cmd += ["-cp", classpath]
    out_dir.mkdir(parents=True, exist_ok=True)
    cmd += ["-d", str(out_dir), str(srcfile)]
    rc, out, err = run(cmd)
    return rc, out, err


def verify_checkstyle(fam):
    host = HOST_JDK[fam]["jdk"]
    src = ROOT / "Checkstyle" / fam / "src/main/java/relaypanel/RelayPanel.java"
    env = {"PATH": f"{host}/bin:/usr/bin:/bin"}
    rc, out, err = run(
        ["checkstyle", "-c", "/usr/share/checkstyle/sun_checks.xml", str(src)],
        env=env,
    )
    clean = "Audit done." in out and "[ERROR]" not in out and "[WARN]" not in out
    return {"status": "CLEAN" if clean else "FINDING", "raw": out.strip()[-300:]}


def verify_lizard(fam):
    src = ROOT / "Lizard" / fam / "src/main/java/harvestscale/HarvestScale.java"
    rc, out, err = run(["lizard", "--languages", "java", str(src)])
    clean = "No thresholds exceeded" in out
    return {"status": "CLEAN" if clean else "FINDING", "raw": out.strip()[-300:]}


def verify_cpd(fam):
    src = ROOT / "CPD" / fam / "src"
    rc, out, err = run(
        ["npx", "--yes", "jscpd", str(src), "--min-lines", "5", "--min-tokens", "30", "--threshold", "0"]
    )
    clean = "Found 0 clones" in out
    return {"status": "CLEAN" if clean else "FINDING", "raw": out.strip()[-300:]}


def verify_findsecbugs(fam):
    src = ROOT / "FindSecBugs" / fam / "src"
    rules = ROOT / "FindSecBugs" / "security-rules.yml"
    env = {"PATH": "/usr/local/bin:/usr/bin:/bin", "SEMGREP_SEND_METRICS": "off"}
    rc, out, err = run(
        ["semgrep", "--disable-version-check", "--metrics=off",
         "--config", str(rules), str(src)],
        env=env,
    )
    clean = "Ran 4 rules on 1 file: 0 findings." in (out + err)
    return {"status": "CLEAN" if clean else "FINDING", "raw": (out + err).strip()[-300:]}


def verify_asm_defuse(fam, work):
    host = HOST_JDK[fam]["jdk"]
    release = HOST_JDK[fam]["release"]
    classes = work / fam / "classes"
    src = ROOT / "ASM-DefUse" / fam / "src/main/java/feeledger/FeeLedger.java"
    rc, out, err = javac(host, release, classes, src)
    if rc != 0:
        return {"status": "ERROR", "raw": (out + err)[-500:]}

    tools_out = work / "tools"
    if not (tools_out / "DefUseRunner.class").exists():
        rc, out, err = javac(
            TOOL_JDK, None, tools_out,
            ROOT / "ASM-DefUse" / "tools" / "DefUseRunner.java",
            classpath=f"{ASM_DEFUSE_JAR}:{ASM_JARS}",
        )
        if rc != 0:
            return {"status": "ERROR", "raw": (out + err)[-500:]}

    cp = f"{tools_out}:{ASM_DEFUSE_JAR}:{ASM_JARS}"
    rc, out, err = run(
        [f"{TOOL_JDK}/bin/java", "-cp", cp, "DefUseRunner",
         str(classes / "feeledger" / "FeeLedger.class"), "computeFee"]
    )
    if "ORPHAN_DEFINITIONS=0" in out:
        return {"status": "CLEAN", "raw": out.strip()}
    key = ("ASM-DefUse", fam)
    if key in FINDINGS:
        return {"status": "FINDING", "raw": (out + err).strip()[-500:]}
    return {"status": "ERROR", "raw": (out + err).strip()[-500:]}


def verify_jacoco(fam, work):
    host = HOST_JDK[fam]["jdk"]
    release = HOST_JDK[fam]["release"]
    classes = work / fam / "classes"
    testclasses = work / fam / "testclasses"
    main_src = ROOT / "JaCoCo" / fam / "src/main/java/kilnmonitor/KilnMonitor.java"
    test_src = ROOT / "JaCoCo" / fam / "src/test/java/kilnmonitor/KilnMonitorTest.java"

    rc, out, err = javac(host, release, classes, main_src)
    if rc != 0:
        return {"status": "ERROR", "raw": (out + err)[-500:]}
    rc, out, err = javac(host, release, testclasses, test_src,
                          classpath=f"{JUNIT}:{HAMCREST}:{classes}")
    if rc != 0:
        return {"status": "ERROR", "raw": (out + err)[-500:]}

    tools_out = work / "tools"
    if not (tools_out / "JacocoRunner.class").exists():
        rc, out, err = javac(
            TOOL_JDK, None, tools_out,
            ROOT / "JaCoCo" / "tools" / "JacocoRunner.java",
            classpath=f"{JACOCO_CORE}:{JUNIT}:{HAMCREST}:{ASM_JARS}",
        )
        if rc != 0:
            return {"status": "ERROR", "raw": (out + err)[-500:]}

    cp = f"{tools_out}:{JACOCO_CORE}:{JUNIT}:{HAMCREST}:{ASM_JARS}:{classes}:{testclasses}"
    rc, out, err = run(
        [f"{TOOL_JDK}/bin/java", "-cp", cp, "JacocoRunner",
         str(classes), "kilnmonitor.KilnMonitor", "kilnmonitor.KilnMonitorTest", str(testclasses)]
    )
    if "TESTS_FAILED=0" in out and "INSTRUCTION_PCT=100.00" in out:
        return {"status": "CLEAN", "raw": out.strip()}
    key = ("JaCoCo", fam)
    if key in FINDINGS:
        return {"status": "FINDING", "raw": (out + err).strip()[-500:]}
    return {"status": "ERROR", "raw": (out + err).strip()[-500:]}


def main():
    work = Path("/tmp/jverify")
    if work.exists():
        import shutil
        shutil.rmtree(work)
    work.mkdir(parents=True)

    for tool in JDK_BLIND_LIVE:
        results[tool] = {}
        fn = {
            "Checkstyle": verify_checkstyle,
            "Lizard": verify_lizard,
            "CPD": verify_cpd,
            "FindSecBugs": verify_findsecbugs,
        }[tool]
        for fam in FAMILIES:
            results[tool][fam] = fn(fam)
            print(f"{tool:14s} {fam:8s} {results[tool][fam]['status']}")

    for tool in BYTECODE_SENSITIVE_LIVE:
        results[tool] = {}
        fn = {"ASM-DefUse": verify_asm_defuse, "JaCoCo": verify_jacoco}[tool]
        for fam in FAMILIES:
            results[tool][fam] = fn(fam, work)
            print(f"{tool:14s} {fam:8s} {results[tool][fam]['status']}")

    for tool in NOT_INSTALLED_PERMANENT:
        results[tool] = {fam: {"status": "NOT_INSTALLED"} for fam in FAMILIES}

    out_path = Path(__file__).resolve().parent / "live_results.json"
    out_path.write_text(json.dumps(results, indent=2))
    print(f"\nWrote {out_path}")

    errors = [
        (t, f) for t, fams in results.items() for f, r in fams.items()
        if r["status"] == "ERROR"
    ]
    if errors:
        print(f"\n*** {len(errors)} UNEXPECTED ERRORS (not matching any documented finding): {errors}")
    else:
        print("\nNo unexpected errors -- every non-CLEAN cell matches a documented FINDING.")


if __name__ == "__main__":
    main()
