#!/usr/bin/env bash
# Runner for custom-def-use. Exit contract: 0 ran / 1 failed / 3 skipped-cannot-run / 4 not-installed.
#
# This tool is named by the metric roster but cannot run on this branch:
#   not a tool - the sheet names no package, version or vendor
#
# It ships anyway, exiting 3, so the metrics that name it are visibly accounted for
# rather than silently missing. See tools/custom-def-use/README.md.
set -u
cd "$(dirname "$0")/../.." || exit 1
exit 3
