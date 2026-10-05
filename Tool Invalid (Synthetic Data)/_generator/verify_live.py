#!/usr/bin/env python3
"""Real, live per-family verification for Java-Tools-Invalid's boundary
families. For each of the 6 "live" tools (4 JDK-blind + 2 bytecode-
sensitive), actually compiles that family's own copy of the domain
source with that family's own host JDK (or --release cross-compile)
and actually invokes the real tool (or its documented real stand-in)
against the result. Writes live_results.json next to this script.

Unlike Clean's verify_live.py, a live tool PASSES this corpus's own
intent when it finds something wrong (FINDING), not when it finds
nothing (CLEAN) -- see each verify_* function's own threshold. The 8
permanently-not-installed tools are recorded directly from
family_table.py, and the 2 unversioned tools (diff-cover, pydriller)
are left as already measured by hand.
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
    src = ROOT / "Checkstyle" / fam / "src/main/java/valvestation/ValveStation.java"
    env = {"PATH": f"{host}/bin:/usr/bin:/bin"}
    rc, out, err = run(
        ["checkstyle", "-c", "/usr/share/checkstyle/sun_checks.xml", str(src)],
        env=env,
    )
    finding = "[ERROR]" in out
    return {"status": "FINDING" if finding else "CLEAN", "raw": out.strip()[-500:]}


def verify_lizard(fam):
    src = ROOT / "Lizard" / fam / "src/main/java/siloweigher/SiloWeigher.java"
    rc, out, err = run(
        ["lizard", "--languages", "java", str(src), "-C", "10", "-L", "30", "-a", "3", "-w"]
    )
    finding = "warning:" in (out + err)
    return {"status": "FINDING" if finding else "CLEAN", "raw": (out + err).strip()[-500:]}


def verify_cpd(fam):
    src = ROOT / "CPD" / fam / "src"
    rc, out, err = run(
        ["npx", "--yes", "jscpd", str(src), "--min-lines", "5", "--min-tokens", "30", "--threshold", "0"]
    )
    finding = "Found 0 clones" not in out
    return {"status": "FINDING" if finding else "CLEAN", "raw": out.strip()[-500:]}


def verify_findsecbugs(fam):
    src = ROOT / "FindSecBugs" / fam / "src"
    rules = ROOT / "FindSecBugs" / "security-rules.yml"
    env = {"PATH": "/usr/local/bin:/usr/bin:/bin", "SEMGREP_SEND_METRICS": "off"}
    rc, out, err = run(
        ["semgrep", "--disable-version-check", "--metrics=off",
         "--config", str(rules), str(src)],
        env=env,
    )
    finding = "Ran 4 rules on 1 file: 0 findings." not in (out + err)
    return {"status": "FINDING" if finding else "CLEAN", "raw": (out + err).strip()[-500:]}


def verify_asm_defuse(fam, work):
    host = HOST_JDK[fam]["jdk"]
    release = HOST_JDK[fam]["release"]
    classes = work / fam / "classes"
    src = ROOT / "ASM-DefUse" / fam / "src/main/java/tariffledger/TariffLedger.java"
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
         str(classes / "tariffledger" / "TariffLedger.class"), "computeTariff"]
    )
    if "ORPHAN_DEFINITIONS=0" in out:
        return {"status": "CLEAN", "raw": out.strip()}
    if "ORPHAN_DEFINITIONS=" in out:
        return {"status": "FINDING", "raw": out.strip()}
    key = ("ASM-DefUse", fam)
    if key in FINDINGS:
        return {"status": "FINDING", "raw": (out + err).strip()[-500:]}
    return {"status": "ERROR", "raw": (out + err).strip()[-500:]}


def verify_jacoco(fam, work):
    host = HOST_JDK[fam]["jdk"]
    release = HOST_JDK[fam]["release"]
    classes = work / fam / "classes"
    testclasses = work / fam / "testclasses"
    main_src = ROOT / "JaCoCo" / fam / "src/main/java/furnacegauge/FurnaceGauge.java"
    test_src = ROOT / "JaCoCo" / fam / "src/test/java/furnacegauge/FurnaceGaugeTest.java"

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
         str(classes), "furnacegauge.FurnaceGauge", "furnacegauge.FurnaceGaugeTest", str(testclasses)]
    )
    if "TESTS_FAILED=0" in out and "INSTRUCTION_PCT=100.00" in out:
        return {"status": "CLEAN", "raw": out.strip()}
    if "INSTRUCTION_PCT=" in out:
        return {"status": "FINDING", "raw": out.strip()}
    key = ("JaCoCo", fam)
    if key in FINDINGS:
        return {"status": "FINDING", "raw": (out + err).strip()[-500:]}
    return {"status": "ERROR", "raw": (out + err).strip()[-500:]}


def main():
    work = Path("/tmp/jverify_invalid")
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
    not_finding = [
        (t, f) for t, fams in results.items() for f, r in fams.items()
        if t in list(JDK_BLIND_LIVE) + list(BYTECODE_SENSITIVE_LIVE) and r["status"] != "FINDING"
    ]
    if errors:
        print(f"\n*** {len(errors)} UNEXPECTED ERRORS: {errors}")
    if not_finding:
        print(f"\n*** {len(not_finding)} LIVE CELLS NOT SHOWING FINDING (unexpected CLEAN): {not_finding}")
    if not errors and not not_finding:
        print("\nEvery live cell shows FINDING, no unexpected errors.")


if __name__ == "__main__":
    main()
