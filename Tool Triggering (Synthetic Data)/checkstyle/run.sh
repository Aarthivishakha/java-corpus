#!/usr/bin/env bash
# Runner for checkstyle on the ANT build. Exit contract: 0 ran / 1 failed / 3 skipped-cannot-run / 4 not-installed.
set -u
cd "$(dirname "$0")/../.." || exit 1
command -v ant >/dev/null 2>&1 || exit 4
ant -q checkstyle
rc=$?
[ $rc -eq 0 ] && exit 0
exit 1
