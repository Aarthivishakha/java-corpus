#!/usr/bin/env bash
# Runner for spoon. Exit contract: 0 ran / 1 failed / 3 skipped-cannot-run / 4 not-installed.
#
# This tool is named by the metric roster but cannot run on this branch:
#   Spoon 11.5.1 requires JDK 17+; this family runs on JDK 11
#
# It ships anyway, exiting 3, so the metrics that name it are visibly accounted for
# rather than silently missing. See tools/spoon/README.md.
set -u
cd "$(dirname "$0")/../.." || exit 1
command -v java >/dev/null 2>&1 || exit 4
exit 3
