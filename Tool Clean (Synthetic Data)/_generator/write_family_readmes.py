#!/usr/bin/env python3
"""Appends a '## Per-family results' section to each versionable tool's
README.md, driven by family_table.py + live_results.json. Idempotent:
strips any previously-appended section before re-adding it. Mirrors
JavaScript-Tools-Clean's write_family_readmes.py.
"""
import json
from pathlib import Path

from family_table import (
    FAMILIES, HOST_JDK, JDK_BLIND_LIVE, BYTECODE_SENSITIVE_LIVE,
    NOT_INSTALLED_PERMANENT, FINDINGS,
)

ROOT = Path(__file__).resolve().parent.parent
MARKER = "\n## Per-family results\n"

live_results = json.loads((Path(__file__).resolve().parent / "live_results.json").read_text())

VERSIONABLE = sorted(
    list(JDK_BLIND_LIVE) + list(BYTECODE_SENSITIVE_LIVE) + NOT_INSTALLED_PERMANENT
)

NOT_INSTALLED_REASON = {
    "CK": "CK 0.7.0 needs JavaParser, resolved from Maven Central, which is blocked at this sandbox's egress proxy; CK's own release jar is a GitHub Release asset too. No JDK family changes this.",
    "Grype": "Grype ships only as a GitHub Release binary or via `go install`; both are blocked (GitHub releases 403, `proxy.golang.org` not allowlisted). No apt package exists. No JDK family changes this.",
    "OWASP Dependency-Check": "Ships as a GitHub Release zip or a Maven/Gradle plugin, both blocked at the egress proxy; no Debian package exists. No JDK family changes this.",
    "PIT": "Distributed purely through Maven Central / the Gradle Plugin Portal, both blocked at the egress proxy; no apt package exists. No JDK family changes this.",
    "PMD": "Only distribution is a GitHub Release zip (Maven Central is the fallback, also blocked); no apt package exists for current PMD. No JDK family changes this.",
    "Spoon": "Large multi-module Maven artifact resolved entirely from Maven Central, which is blocked; no apt package exists. No JDK family changes this.",
    "SpotBugs": "No Debian package; release jar is GitHub-Release-only. Its installable predecessor FindBugs 3.1.0~preview2 was tried and failed against this sandbox's JDKs (see root README) -- a genuine incompatibility, not fixed by picking a different boundary family.",
    "ba-dua": "ba-dua's own pom pins an old JaCoCo core (0.8.1) unreachable via Maven Central; a from-source build would need that exact old dependency. No JDK family changes this.",
}


def family_row(tool, fam):
    if tool in NOT_INSTALLED_PERMANENT:
        return "NOT INSTALLED"
    r = live_results[tool][fam]
    status = r["status"]
    if status == "CLEAN":
        return "CLEAN"
    if status == "FINDING":
        return "**FINDING**"
    return status


def host_desc(fam):
    h = HOST_JDK[fam]
    jdk_ver = h["jdk"].split("java-")[1].split("-")[0]
    if h["release"]:
        return f"JDK {jdk_ver} host, `--release {h['release']}`"
    return f"JDK {jdk_ver} (native)"


def build_section(tool):
    lines = [MARKER.strip(), ""]
    lines.append(
        "Boundary families this tool was exploded into, and what each one's "
        "own real invocation found (not asserted -- see `_generator/verify_live.py`):"
    )
    lines.append("")
    lines.append("| Family | Host JDK | Result |")
    lines.append("|---|---|---|")
    for fam in FAMILIES:
        lines.append(f"| `{fam}` | {host_desc(fam)} | {family_row(tool, fam)} |")
    lines.append("")

    if tool in NOT_INSTALLED_PERMANENT:
        lines.append(NOT_INSTALLED_REASON[tool])
        lines.append("")
    else:
        findings_here = [(t, f) for (t, f) in FINDINGS if t == tool]
        if findings_here:
            lines.append("Genuine finding(s) on this tool:")
            lines.append("")
            for (_, fam) in findings_here:
                lines.append(f"**{fam}**: {FINDINGS[(tool, fam)]}")
                lines.append("")
    return "\n".join(lines)


def update_readme(tool):
    path = ROOT / tool / "README.md"
    if not path.is_file():
        print(f"  SKIP {tool}: no README.md")
        return
    text = path.read_text(encoding="utf-8")
    idx = text.find(MARKER.strip())
    if idx != -1:
        text = text[:idx].rstrip() + "\n"
    section = build_section(tool)
    new_text = text.rstrip() + "\n\n" + section + "\n"
    path.write_text(new_text, encoding="utf-8")
    print(f"  OK {tool}")


def main():
    print(f"Updating {len(VERSIONABLE)} tool READMEs with per-family sections:")
    for tool in VERSIONABLE:
        update_readme(tool)
    print("Unversioned (untouched): diff-cover, pydriller")


if __name__ == "__main__":
    main()
