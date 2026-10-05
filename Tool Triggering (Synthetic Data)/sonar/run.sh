#!/usr/bin/env bash
# Runner for sonar. Exit contract: 0 ran / 1 failed / 3 skipped-cannot-run / 4 not-installed.
#
# This tool is named by the metric roster but cannot run on this branch:
#   needs a running SonarQube server; set SONAR_HOST_URL to enable
#
# It ships anyway, exiting 3, so the metrics that name it are visibly accounted for
# rather than silently missing. See tools/sonar/README.md.
set -u
cd "$(dirname "$0")/../.." || exit 1
command -v sonar-scanner >/dev/null 2>&1 || exit 4
exit 3
