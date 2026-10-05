#!/usr/bin/env python3
"""Code-churn extraction with PyDriller. Alternative tool for 5 Code Churn metrics.

Counts, per file: modifying commits, lines added, lines deleted, and the distinct authors
INCLUDING Co-authored-by trailers - which the plain git author field misses, and which
cost the TypeScript corpus a wrong ownership metric across 120 branches.
"""
import json
import sys
from collections import defaultdict

try:
    from pydriller import Repository
except ImportError:
    sys.exit(4)


def collect(repo_path):
    stats = defaultdict(lambda: {"commits": 0, "added": 0, "deleted": 0, "authors": set()})
    for commit in Repository(repo_path).traverse_commits():
        authors = {commit.author.email}
        for line in (commit.msg or "").splitlines():
            if line.lower().startswith("co-authored-by:") and "<" in line:
                authors.add(line.split("<", 1)[1].rstrip(">").strip())
        for mod in commit.modified_files:
            key = mod.new_path or mod.old_path
            if not key:
                continue
            entry = stats[key]
            entry["commits"] += 1
            entry["added"] += mod.added_lines
            entry["deleted"] += mod.deleted_lines
            entry["authors"] |= authors
    return stats


def main():
    repo = sys.argv[1] if len(sys.argv) > 1 else "."
    out_path = sys.argv[2] if len(sys.argv) > 2 else "tools/pydriller/out/churn.json"
    stats = collect(repo)
    out = {k: {**v, "authors": sorted(v["authors"])} for k, v in stats.items()}
    with open(out_path, "w", encoding="utf-8") as fh:
        json.dump(out, fh, indent=2)
    print("%d files, %d file-modifications"
          % (len(out), sum(v["commits"] for v in out.values())))


if __name__ == "__main__":
    main()
