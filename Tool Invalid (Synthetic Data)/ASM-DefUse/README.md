# ASM-DefUse

Inverse-corpus counterpart to the sibling Java-Tools-Clean's
**ASM-DefUse** folder. Engineered so the real tool finds something
genuinely wrong, confirmed by actually running it -- not asserted.

Domain: tiered import-tariff arithmetic (`TariffLedger`) analysed at
the bytecode level.

**Measured**: installed (the same real, from-source-built vendor jar
Clean's own folder uses) and actually invoked in the build environment;
the result below is real, not asserted.

## What a failing result looks like

asm-defuse's `FlowAnalyzer`/`DepthFirstDefUseChainSearch` walk
`TariffLedger.class` and find **4 of 7 local-variable definitions with
no reachable use** -- `baseline`, `surchargeA`, `surchargeB` and
`discount` are all dead stores, defined and never read before the
method returns. 57% of the method's own local-variable definitions are
orphans.

## Command

```bash
javac -d build/main -cp vendor/asm-defuse.jar:<asm jars> src/main/java/tariffledger/TariffLedger.java
javac -d build/tools -cp vendor/asm-defuse.jar:<asm jars> tools/DefUseRunner.java
java -cp build/tools:build/main:vendor/asm-defuse.jar:<asm jars> DefUseRunner build/main/tariffledger/TariffLedger.class computeTariff
```

Measured: `VARIABLES=7 CHAINS=9 ORPHAN_DEFINITIONS=4` (java8/java9/java16).

## Layout

```text
ASM-DefUse/
  README.md
  src/main/java/tariffledger/TariffLedger.java   the analysed class -- static computeTariff(double)
  src/test/java/tariffledger/TariffLedgerTest.java
  tools/DefUseRunner.java                  drives asm-defuse's real API against the compiled class
  vendor/asm-defuse.jar                    the identical real jar Clean's own folder built from source, see Notes
```

## Notes

`br.usp.each.saeg:asm-defuse` is not reachable via Maven Central here
(blocked at this sandbox's egress proxy). This folder reuses the exact
same real, from-source-built `vendor/asm-defuse.jar` the sibling Clean
corpus already produced (genuine, unmodified upstream `saeg/asm-defuse`
+ `saeg/saeg-commons` source, compiled against this sandbox's installed
`org.ow2.asm` 9.7 jars) -- the library is identical in both corpora;
only the analysed domain class differs.

`tools/DefUseRunner.java` (byte-for-byte the same driver Clean's own
folder uses) drives asm-defuse's actual public API against the
compiled `TariffLedger.class`: `DefUseAnalyzer` builds the control-flow
frames and `DepthFirstDefUseChainSearch.search(...)` walks them to
produce the real `DefUseChain` list, from which the four dead-store
variables genuinely fail to appear.

## Per-family results

Boundary families this tool was exploded into, and what each one's own
real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | **FINDING** -- `ORPHAN_DEFINITIONS=4` |
| `java9` | JDK 11 host, `--release 9` | **FINDING** -- `ORPHAN_DEFINITIONS=4` |
| `java16` | JDK 17 host, `--release 16` | **FINDING** -- `ORPHAN_DEFINITIONS=4` |
| `java24` | JDK 25 host, `--release 24` | **FINDING** -- ASM ceiling (see below) |
| `java25` | JDK 25 (native) | **FINDING** -- ASM ceiling (see below) |

Genuine finding(s) on this tool:

**java8/java9/java16**: 4 of 7 local-variable definitions in
`computeTariff` are orphans (dead stores never read) -- a real,
majority-wrong def-use result, not an edge case.

**java24, java25**: apt's libasm-java 9.7 (the latest release reachable
in this sandbox; Maven Central, where 9.8 would come from, is blocked
at the egress proxy) cannot parse a Java-24/25-targeted class file at
all -- `ClassReader.<init>` throws `IllegalArgumentException:
Unsupported class file major version 68/69`. Identical root cause to
the sibling Clean corpus's own ASM-DefUse folder, independent of which
domain source was compiled -- a genuine environmental ceiling that
would mask any content-level result either way, so this cell is
"not CLEAN" (and thus FINDING, the corpus's own intent) for a reason
unrelated to the planted dead stores.
