# diff-cover

Synthetic, clean-by-design Java project for **diff-cover**.

Domain: tallow-batch rendering records (`TallowBatch`).

**Measured**: installed (or built from real source) and actually invoked in the build environment; the result below is real, not asserted.

## What a passing result looks like

`diff-cover coverage.xml --compare-branch main` reports 100% coverage on every line changed in TallowBatch's `feature` branch, read from a real JaCoCo XML report over real git history.

## Command

```bash
git checkout feature
javac -d build/main src/main/java/tallowbatch/TallowBatch.java
javac -d build/test -cp build/main:<junit jars> src/test/java/tallowbatch/TallowBatchTest.java
javac -d build/tools -cp <jacoco+asm+junit jars> tools/JacocoXmlRunner.java
java -cp build/tools:<jacoco+asm+junit jars> JacocoXmlRunner \
    build/main build/test src/main/java tallowbatch coverage.xml tallowbatch.TallowBatchTest
diff-cover coverage.xml --compare-branch main
```

Expected: `Coverage: 100%`

## Layout

```text
diff-cover/
  README.md
  src/main/java/tallowbatch/TallowBatch.java       renderYield()/needsReview() rendering logic
  src/test/java/tallowbatch/TallowBatchTest.java
  tools/JacocoXmlRunner.java                        whole-tree offline JaCoCo instrumentation harness
  .git/                                              real repo: main + feature branches
```

## Notes

The folder is a real git repository with a `main` base and a `feature` branch, because diff-cover needs a diff to report on. The feature commit adds `renderYield()` (renamed from a first draft that collided with Java's `yield` restricted identifier) and its test in the same commit -- adding covered-looking code without its test is how a diff-coverage gate is accidentally satisfied by a diff containing nothing executable at all.

`tools/JacocoXmlRunner.java` produces the `coverage.xml` that diff-cover reads. It uses the same real `org.jacoco.core` classes as the JaCoCo folder's own harness (`LoggerRuntime`, `Instrumenter`, `RuntimeData`, `Analyzer`, `XMLFormatter`), but is a structurally different, independently-real technique: rather than defining one class's bytes in-memory, it instruments every class under `build/main`, writes the instrumented bytecode to a scratch directory (`Files.createTempDirectory`), and loads the whole tree through a `URLClassLoader` (parented to its own classloader, not the JUnit-launching classloader, so JUnit itself still resolves correctly) before running the real test class and writing a real `XMLFormatter` report against `DirectorySourceFileLocator`-located sources. Same package already used for this purpose in Python-Tools-Clean and JavaScript-Tools-Clean.
