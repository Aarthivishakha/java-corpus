import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import br.usp.each.saeg.asm.defuse.DefUseAnalyzer;
import br.usp.each.saeg.asm.defuse.DefUseChain;
import br.usp.each.saeg.asm.defuse.DefUseFrame;
import br.usp.each.saeg.asm.defuse.DefUseInterpreter;
import br.usp.each.saeg.asm.defuse.DepthFirstDefUseChainSearch;
import br.usp.each.saeg.asm.defuse.FlowAnalyzer;
import br.usp.each.saeg.asm.defuse.Value;
import br.usp.each.saeg.asm.defuse.Variable;

/**
 * Drives the real, from-source-built br.usp.each.saeg:asm-defuse library
 * (FlowAnalyzer + DefUseAnalyzer + DepthFirstDefUseChainSearch) against
 * one compiled method's real bytecode and reports how many def-use
 * chains it finds, and whether every definition reaches at least one
 * use -- not a reimplementation of the tool, its real analysis classes
 * run against real class files.
 *
 * Usage: java DefUseRunner <classFile> <methodName>
 */
public final class DefUseRunner {

    public static void main(String[] args) throws Exception {
        String classFile = args[0];
        String methodName = args[1];

        ClassNode cn = new ClassNode();
        try (InputStream in = new FileInputStream(classFile)) {
            new ClassReader(in).accept(cn, 0);
        }

        MethodNode target = null;
        for (Object o : cn.methods) {
            MethodNode mn = (MethodNode) o;
            if (mn.name.equals(methodName)) {
                target = mn;
                break;
            }
        }
        if (target == null) {
            System.err.println("METHOD_NOT_FOUND");
            System.exit(1);
        }

        DefUseInterpreter interpreter = new DefUseInterpreter();
        FlowAnalyzer<Value> flowAnalyzer = new FlowAnalyzer<Value>(interpreter);
        DefUseAnalyzer analyzer = new DefUseAnalyzer(flowAnalyzer, interpreter);

        DefUseFrame[] frames = analyzer.analyze(cn.name, target);
        Variable[] variables = analyzer.getVariables();

        DepthFirstDefUseChainSearch search = new DepthFirstDefUseChainSearch();
        DefUseChain[] chains = search.search(
                frames, variables, flowAnalyzer.getSuccessors(), flowAnalyzer.getPredecessors());

        Set<Variable> defined = new HashSet<Variable>();
        for (DefUseFrame f : frames) {
            if (f != null) {
                defined.addAll(f.getDefinitions());
            }
        }

        Set<Integer> varsWithChain = new HashSet<Integer>();
        for (DefUseChain c : chains) {
            varsWithChain.add(c.var);
        }

        int orphanDefinitions = 0;
        for (int i = 0; i < variables.length; i++) {
            if (defined.contains(variables[i]) && !varsWithChain.contains(i)) {
                orphanDefinitions++;
            }
        }

        System.out.printf(
                "VARIABLES=%d CHAINS=%d ORPHAN_DEFINITIONS=%d%n",
                variables.length, chains.length, orphanDefinitions);
    }
}
