#!/usr/bin/env python3
"""Write each non-hand-written tool folder's base README.md from meta.py,
directly into the tool's own root (this corpus keeps tools at
<root>/<Tool>/, not under a staging out/ folder). Skips the three
hand-written READMEs (ASM-DefUse's from-source build story, diff-cover's
and pydriller's real git history), which are edited by hand and merely
carried forward.
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from meta import TOOLS  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
HAND_WRITTEN = {"ASM-DefUse", "diff-cover", "pydriller"}


def render(entry: dict) -> str:
    status_line = (
        "**Measured**: installed (or built from real source) and actually "
        "invoked in the build environment; the result below is real, not "
        "asserted."
        if entry["status"] == "measured"
        else "**Not installed here**: see Notes for why, and what was "
        "checked instead."
    )
    lines = [
        f"# {entry['tool']}",
        "",
        f"Synthetic, clean-by-design Java project for **{entry['tool']}**.",
        "",
        f"Domain: {entry['domain']}",
        "",
        status_line,
        "",
        "## What a passing result looks like",
        "",
        entry["clean_means"],
        "",
        "## Command",
        "",
        "```bash",
        entry["command"],
        "```",
    ]
    if entry["notes"]:
        lines += ["", "## Notes", "", entry["notes"]]
    lines.append("")
    return "\n".join(lines)


def main() -> int:
    written = 0
    for entry in TOOLS:
        if entry["tool"] in HAND_WRITTEN:
            continue
        folder = ROOT / entry["tool"]
        if not folder.is_dir():
            print(f"missing folder: {entry['tool']}", file=sys.stderr)
            return 1
        (folder / "README.md").write_text(render(entry), encoding="utf-8")
        written += 1
    print(f"wrote {written} tool READMEs")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
