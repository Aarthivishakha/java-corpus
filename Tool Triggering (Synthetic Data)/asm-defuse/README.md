# ASM-DefUse — bytecode def-use chains

The third option named in the sheet's Data Flow prose. Builds def-use chains from
bytecode via the ASM analysis library, so its Java-version ceiling is whatever ASM
version it links.

    java -cp tools/asm-defuse/asm-defuse.jar:<classes> br.usp.each.saeg.asm.defuse.Main <class-dir>

Not published to Maven Central under a stable coordinate; the runner exits 4
(not-installed) until a jar is placed here. Documented rather than silently omitted.
