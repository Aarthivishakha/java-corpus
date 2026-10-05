#!/usr/bin/env bash
# Runner for asm-defuse. Exit contract: 0 ran / 1 failed / 3 skipped-cannot-run / 4 not-installed.
#
# This tool is named by the metric roster but cannot run on this branch:
#   no jar published under a stable coordinate - place one in tools/asm-defuse/
#
# It ships anyway, exiting 4, so the metrics that name it are visibly accounted for
# rather than silently missing. See tools/asm-defuse/README.md.
set -u
cd "$(dirname "$0")/../.." || exit 1
command -v java >/dev/null 2>&1 || exit 4
exit 4
