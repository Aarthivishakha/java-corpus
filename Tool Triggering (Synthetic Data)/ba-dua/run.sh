#!/usr/bin/env bash
# Runner for ba-dua. Exit contract: 0 ran / 1 failed / 3 skipped-cannot-run / 4 not-installed.
#
# This tool is named by the metric roster but cannot run on this branch:
#   ba-dua 0.8.0 links JaCoCo 0.8.1, which stops at Java 10 class files
#
# It ships anyway, exiting 3, so the metrics that name it are visibly accounted for
# rather than silently missing. See tools/ba-dua/README.md.
set -u
cd "$(dirname "$0")/../.." || exit 1
command -v java >/dev/null 2>&1 || exit 4
exit 3
