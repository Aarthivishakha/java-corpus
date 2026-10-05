# SonarQube / SonarScanner — new-code coverage metrics

Named in the sheet's prose for 2 Coverage Delta metrics ("Sonar new-code metrics").
Requires a running SonarQube server, so it cannot be exercised offline or in CI without
one.

    sonar-scanner -Dsonar.projectKey=<key> -Dsonar.coverage.jacoco.xmlReportPaths=<jacoco.xml>

The runner exits 4 (not-installed) when `sonar-scanner` is absent and 3
(skipped-cannot-run) when no `SONAR_HOST_URL` is set. It is here so the two metrics that
name it are visibly accounted for rather than silently dropped.
