import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.xml.XMLFormatter;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Drives real org.jacoco.core + org.jacoco.report classes over a whole
 * source tree and writes a real JaCoCo XML coverage report for
 * diff-cover to read.
 *
 * Unlike the single-class JacocoRunner used by the sibling JaCoCo
 * folder (which serves instrumented bytes from an in-memory
 * classloader override), this driver takes the offline-instrumentation
 * route JaCoCo's own documentation describes for build tools: it
 * writes each instrumented class to a scratch directory that shadows
 * the real compiled classes on the classpath, then loads everything
 * through an ordinary URLClassLoader. Same real org.jacoco.core API,
 * a genuinely different (and more build-tool-realistic) wiring.
 *
 * Usage: java JacocoXmlRunner <mainClassesDir> <testClassesDir>
 *            <sourceDir> <bundleName> <outputXml> <testClass1> [testClass2 ...]
 */
public final class JacocoXmlRunner {

    public static void main(String[] args) throws Exception {
        final String mainClassesDir = args[0];
        final String testClassesDir = args[1];
        final String sourceDir = args[2];
        final String bundleName = args[3];
        final String outputXml = args[4];
        final List<String> testClassNames = new ArrayList<>();
        for (int i = 5; i < args.length; i++) {
            testClassNames.add(args[i]);
        }

        final Path scratchDir = Files.createTempDirectory("jacoco-instrumented-");
        final IRuntime runtime = new LoggerRuntime();
        final Instrumenter instrumenter = new Instrumenter(runtime);
        final RuntimeData data = new RuntimeData();
        runtime.startup(data);

        for (final String className : classNamesUnder(mainClassesDir)) {
            final byte[] original = Files.readAllBytes(classFilePath(mainClassesDir, className));
            final byte[] instrumented = instrumenter.instrument(original, className);
            writeClassFile(scratchDir, className, instrumented);
        }

        final URLClassLoader isolated = new URLClassLoader(
                new URL[] {
                        scratchDir.toUri().toURL(),
                        Paths.get(testClassesDir).toUri().toURL(),
                },
                JacocoXmlRunner.class.getClassLoader());

        boolean anyFailure = false;
        for (final String testClassName : testClassNames) {
            final Class<?> testClass = Class.forName(testClassName, true, isolated);
            final Result result = JUnitCore.runClasses(testClass);
            if (!result.wasSuccessful()) {
                anyFailure = true;
                for (final Failure failure : result.getFailures()) {
                    System.err.println(failure.toString());
                }
            }
        }

        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        data.collect(executionData, sessionInfos, false);
        runtime.shutdown();

        if (anyFailure) {
            System.err.println("TESTS_FAILED");
            System.exit(1);
        }

        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        for (final String className : classNamesUnder(mainClassesDir)) {
            analyzer.analyzeClass(Files.readAllBytes(classFilePath(mainClassesDir, className)), className);
        }

        final IBundleCoverage bundle = coverageBuilder.getBundle(bundleName);

        try (FileOutputStream out = new FileOutputStream(outputXml)) {
            final XMLFormatter formatter = new XMLFormatter();
            final IReportVisitor visitor = formatter.createVisitor(out);
            visitor.visitInfo(sessionInfos.getInfos(), executionData.getContents());
            visitor.visitBundle(bundle, new DirectorySourceFileLocator(new File(sourceDir), "UTF-8", 4));
            visitor.visitEnd();
        }

        final int instrTotal = bundle.getInstructionCounter().getTotalCount();
        final int branchTotal = bundle.getBranchCounter().getTotalCount();
        final double instrPct = instrTotal == 0 ? 100.0
                : 100.0 * bundle.getInstructionCounter().getCoveredCount() / instrTotal;
        final double branchPct = branchTotal == 0 ? 100.0
                : 100.0 * bundle.getBranchCounter().getCoveredCount() / branchTotal;

        System.out.printf(
                "INSTRUCTION_PCT=%.2f BRANCH_PCT=%.2f LINE_MISSED=%d LINE_COVERED=%d%n",
                instrPct, branchPct,
                bundle.getLineCounter().getMissedCount(),
                bundle.getLineCounter().getCoveredCount());
    }

    private static List<String> classNamesUnder(final String classesDir) throws IOException {
        final Path root = Paths.get(classesDir);
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(p -> p.toString().endsWith(".class"))
                    .map(p -> root.relativize(p).toString())
                    .map(rel -> rel.substring(0, rel.length() - 6).replace(File.separatorChar, '.'))
                    .collect(Collectors.toList());
        }
    }

    private static Path classFilePath(final String classesDir, final String className) {
        return Paths.get(classesDir, className.replace('.', '/') + ".class");
    }

    private static void writeClassFile(final Path scratchDir, final String className, final byte[] bytes)
            throws IOException {
        final Path target = scratchDir.resolve(className.replace('.', '/') + ".class");
        Files.createDirectories(target.getParent());
        Files.write(target, bytes);
    }
}
