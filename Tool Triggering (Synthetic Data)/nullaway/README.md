# NullAway / Checker Framework — null-dereference analysis

Named in the sheet's prose for 1 All-Definition-Coverage metric, alongside SpotBugs'
`NP_*` detectors. NullAway runs as an Error Prone plugin at compile time rather than as a
standalone analyser, so it changes the compiler invocation rather than adding a step.

    -XepOpt:NullAway:AnnotatedPackages=com.pramora.testable

The runner exits 3 (skipped-cannot-run) unless the build is configured with Error Prone.
Wiring Error Prone into all four build systems is a separate decision, not a default.
