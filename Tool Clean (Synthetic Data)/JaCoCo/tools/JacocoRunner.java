import java.io.IOException;
import java.io.InputStream;

import org.jacoco.core.analysis.*;
import org.jacoco.core.data.*;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.*;
import org.junit.runner.*;
import org.junit.runner.notification.Failure;

/**
 * Drives real org.jacoco.core classes to offline-instrument one class,
 * run its real JUnit4 test through the instrumented bytecode, and print
 * the resulting per-class instruction/branch coverage -- the same
 * in-process pattern JaCoCo's own CoreTutorial example uses, not a
 * reimplementation of JaCoCo.
 *
 * Usage: java JacocoRunner <classesDir> <targetClassName> <testClassName>
 */
public final class JacocoRunner {

    public static void main(String[] args) throws Exception {
        String classesDir = args[0];
        String targetClassName = args[1];
        String testClassName = args[2];
        String testClassesDir = args.length > 3 ? args[3] : classesDir;

        byte[] originalBytes = readClassBytes(classesDir, targetClassName);
        byte[] testBytes = readClassBytes(testClassesDir, testClassName);

        IRuntime runtime = new LoggerRuntime();
        Instrumenter instrumenter = new Instrumenter(runtime);
        byte[] instrumentedBytes = instrumenter.instrument(originalBytes, targetClassName);

        RuntimeData data = new RuntimeData();
        runtime.startup(data);

        MemoryClassLoader loader = new MemoryClassLoader(JacocoRunner.class.getClassLoader());
        loader.addDefinition(targetClassName, instrumentedBytes);
        loader.addDefinition(testClassName, testBytes);

        Class<?> testClass = loader.loadClass(testClassName);
        Result result = JUnitCore.runClasses(testClass);

        ExecutionDataStore executionData = new ExecutionDataStore();
        SessionInfoStore sessionInfos = new SessionInfoStore();
        data.collect(executionData, sessionInfos, false);
        runtime.shutdown();

        if (!result.wasSuccessful()) {
            for (Failure failure : result.getFailures()) {
                System.err.println(failure.toString());
            }
            System.err.println("TESTS_FAILED");
            System.exit(1);
        }

        CoverageBuilder coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        analyzer.analyzeClass(originalBytes, targetClassName);

        int missedInstr = 0;
        int coveredInstr = 0;
        int missedBranch = 0;
        int coveredBranch = 0;
        for (IClassCoverage cc : coverageBuilder.getClasses()) {
            missedInstr += cc.getInstructionCounter().getMissedCount();
            coveredInstr += cc.getInstructionCounter().getCoveredCount();
            missedBranch += cc.getBranchCounter().getMissedCount();
            coveredBranch += cc.getBranchCounter().getCoveredCount();
        }

        double instrPct = 100.0 * coveredInstr / (coveredInstr + missedInstr);
        double branchPct = (coveredBranch + missedBranch) == 0
                ? 100.0
                : 100.0 * coveredBranch / (coveredBranch + missedBranch);

        System.out.printf(
                "TESTS_RUN=%d TESTS_FAILED=%d INSTRUCTION_PCT=%.2f BRANCH_PCT=%.2f%n",
                result.getRunCount(), result.getFailureCount(), instrPct, branchPct);
    }

    private static byte[] readClassBytes(String classesDir, String className) throws IOException {
        String path = classesDir + "/" + className.replace('.', '/') + ".class";
        try (InputStream in = new java.io.FileInputStream(path)) {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        }
    }

    /** Minimal classloader that serves one class's bytes from memory. */
    static final class MemoryClassLoader extends ClassLoader {
        private final java.util.Map<String, byte[]> definitions = new java.util.HashMap<>();

        MemoryClassLoader(ClassLoader parent) {
            super(parent);
        }

        void addDefinition(String name, byte[] bytes) {
            definitions.put(name, bytes);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            byte[] bytes = definitions.get(name);
            if (bytes != null) {
                Class<?> c = defineClass(name, bytes, 0, bytes.length);
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
            return super.loadClass(name, resolve);
        }
    }
}
