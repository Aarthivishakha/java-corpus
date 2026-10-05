#!/usr/bin/env python3
"""Explodes each versionable Java-Tools-Clean tool into five boundary-
family subfolders (java8/java9/java16/java24/java25), mirroring the
existing flat src/main+src/test tree into each -- unmodified, since the
domain source uses no version-gated syntax and is identical across all
five targets (see family_table.py's docstring). Shared tooling
(driver .java files under tools/, the vendor jar, FindSecBugs's local
ruleset) stays at the tool's own root, since it is compiled/invoked the
same way regardless of family.

diff-cover and pydriller are left untouched (unversioned, exactly like
their counterparts in the sibling Python/JavaScript corpora).
"""
import shutil
from pathlib import Path

from family_table import (
    FAMILIES, JDK_BLIND_LIVE, BYTECODE_SENSITIVE_LIVE, NOT_INSTALLED_PERMANENT,
)

ROOT = Path(__file__).resolve().parent.parent

VERSIONABLE = sorted(
    list(JDK_BLIND_LIVE) + list(BYTECODE_SENSITIVE_LIVE) + NOT_INSTALLED_PERMANENT
)


def explode(tool: str) -> None:
    tool_dir = ROOT / tool
    src_dir = tool_dir / "src"
    if not src_dir.is_dir():
        print(f"  SKIP {tool}: no src/ (already exploded or missing)")
        return

    for family in FAMILIES:
        fam_dir = tool_dir / family
        fam_src = fam_dir / "src"
        if fam_src.exists():
            shutil.rmtree(fam_src)
        fam_dir.mkdir(exist_ok=True)
        shutil.copytree(src_dir, fam_src)
    print(f"  OK {tool}: {len(FAMILIES)} family folders created")


def main() -> None:
    print(f"{len(VERSIONABLE)} versionable tools to explode:")
    for tool in VERSIONABLE:
        explode(tool)
    print("Unversioned (untouched): diff-cover, pydriller")


if __name__ == "__main__":
    main()
