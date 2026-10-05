#!/usr/bin/env python3
"""Mines PasturePlot's real git history with pydriller and checks
whether each commit's [area:X] tag is corroborated by the files it
actually touched. A commit's claim is corroborated when at least one
of the files it modified lives under that area's expected directory;
"meta" has no corroborating directory at all, so any [area:meta]
commit is, by construction, a mismatch.
"""
import re

from pydriller import Repository

AREA_PATTERN = re.compile(r"\[area:(\w+)\]")
AREA_DIR = {"ledger": "src/", "docs": "docs/"}


def main():
    total_tagged = 0
    mismatches = 0
    for commit in Repository(".").traverse_commits():
        match = AREA_PATTERN.search(commit.msg)
        if not match:
            continue
        total_tagged += 1
        area = match.group(1)
        touched = [mf.new_path or mf.old_path for mf in commit.modified_files]
        expected_dir = AREA_DIR.get(area)
        corroborated = expected_dir is not None and any(
            t and t.replace("\\", "/").startswith(expected_dir) for t in touched
        )
        status = "OK" if corroborated else "MISMATCH"
        if not corroborated:
            mismatches += 1
        print(f"{commit.hash[:8]} [area:{area}] {status} touched={touched}")

    pct = 100.0 * mismatches / total_tagged if total_tagged else 0.0
    print(f"\nTotal tagged commits: {total_tagged}")
    print(f"Mismatches: {mismatches} ({pct:.1f}%)")


if __name__ == "__main__":
    main()
