# ba-dua — data-flow (def-use) coverage

Named in the sheet's prose as a primary option for 4 All-Definition-Coverage metrics.
A JaCoCo-based def-use coverage tool: the only real Java implementation of the metric.

    java -jar tools/ba-dua/ba-dua-cli.jar report --input target/badua.ser \
         --classes target/classes --show-classes --xml tools/ba-dua/out/badua.xml

**This runner is expected to fail on this branch, and that is a finding, not a bug.**
ba-dua 0.8.0's own pom declares `<jacoco.version>0.8.1</jacoco.version>`, and JaCoCo 0.8.1
tops out at **Java 10** class files. It cannot read java11 bytecode or above.

The runner exits 3 (skipped-cannot-run) on any family above java10 rather than pretending
to have run. This corpus's `active-degraded` tool.
