#!/usr/bin/env bash
# Runner for ck. Exit contract: 0 ran / 1 failed / 3 skipped-cannot-run / 4 not-installed.
#
# This tool is named by the metric roster but cannot run on this branch:
#   CK 0.7.0 bundles Eclipse JDT 3.26.0, which parses only up to Java 16 - it cannot read this branch's records or sealed types
#
# It ships anyway, exiting 3, so the metrics that name it are visibly accounted for
# rather than silently missing. See tools/ck/README.md.
set -u
cd "$(dirname "$0")/../.." || exit 1
command -v java >/dev/null 2>&1 || exit 4
exit 3
