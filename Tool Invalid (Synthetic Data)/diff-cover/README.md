# diff-cover

Inverse-corpus counterpart to the sibling Java-Tools-Clean's
**diff-cover** folder. Engineered so the real tool finds something
genuinely wrong, confirmed by actually running it -- not asserted.

Domain: granary-sweep yield records (`GranarySweep`).

**Measured**: installed and actually invoked in the build environment;
the result below is real, not asserted.

## What a failing result looks like

`diff-cover coverage.xml --compare-branch main` reports real,
**45% diff coverage** on GranarySweep's `feature` branch: the branch
adds a 6-way `classifyFillLevel(double)` method, but the feature
commit's own test exercises only one of its six branches, leaving 6 of
the diff's 11 new lines (55%) uncovered -- read from a real JaCoCo XML
report over real git history.

## Command

```bash
git checkout feature
javac -d build/main src/main/java/granarysweep/GranarySweep.java
javac -d build/test -cp build/main:<junit jars> src/test/java/granarysweep/GranarySweepTest.java
javac -d build/tools -cp <jacoco+asm+junit jars> tools/JacocoXmlRunner.java
java -cp build/tools:<jacoco+asm+junit jars> JacocoXmlRunner \
    build/main build/test src/main/java granarysweep coverage.xml granarysweep.GranarySweepTest
diff-cover coverage.xml --compare-branch main
```

Measured: `Coverage: 45%` (`Missing lines 43,45,47,50-51,53`).

## Layout

```text
diff-cover/
  README.md
  src/main/java/granarysweep/GranarySweep.java     sweepYield()/classifyFillLevel() logic
  src/test/java/granarysweep/GranarySweepTest.java
  tools/JacocoXmlRunner.java                        whole-tree offline JaCoCo instrumentation harness
  .git/                                              real repo: main + feature branches
```

## Notes

Real git repository with a `main` base (sweepYield() plus its test) and
a `feature` branch (adds `classifyFillLevel`, a 6-branch handling-tier
classifier, with a test that exercises only its "normal" branch). The
feature commit adds the method and a thin test in the same commit --
exactly like the sibling Clean corpus's own diff-cover folder, so the
coverage gap is a genuine thin-test defect, not an artifact of an
untested diff.

`tools/JacocoXmlRunner.java` is byte-for-byte the same real whole-tree
offline-instrumentation driver Clean's own diff-cover folder uses
(`LoggerRuntime`, `Instrumenter`, `RuntimeData`, `Analyzer`,
`XMLFormatter`, `DirectorySourceFileLocator`) -- only the domain source
and its (deliberately thin) test differ.
