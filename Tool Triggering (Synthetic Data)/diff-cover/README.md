# diff-cover — Coverage Delta

Primary for 4 Coverage Delta metrics, alternative for 6. Python; reads the JaCoCo XML
report, so the `jacoco` runner must have produced one first.

    diff-cover <jacoco.xml> --compare-branch=main --html-report tools/diff-cover/out/report.html

Class C: source-language agnostic. It parses a coverage report and a git diff, so it
cannot distinguish one Java version from another, in principle or in practice.
