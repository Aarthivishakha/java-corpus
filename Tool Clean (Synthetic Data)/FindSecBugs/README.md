# FindSecBugs

Synthetic, clean-by-design Java project for **FindSecBugs**.

Domain: vault-access control (VaultGate)

**Not installed here**: see Notes for why, and what was checked instead.

## What a passing result looks like

FindSecBugs is a SpotBugs plugin and cannot run without SpotBugs itself (also not installed here -- see the SpotBugs folder). `semgrep --config p/java --config p/security-audit` -- the same engine already used as OpenGrep's stand-in in the sibling JavaScript-Tools-Clean corpus, and the exact alternative the platform's own build-contract doc proposes for this block -- was run for real against VaultGate and found zero findings.

## Command

```bash
semgrep --config p/java --config p/security-audit src/
```

## Notes

FindSecBugs adds detectors to SpotBugs; it cannot run, or disagree with SpotBugs, on its own. With SpotBugs unreachable here, FindSecBugs is unreachable too. semgrep was already installed in this sandbox (from the sibling corpus's OpenGrep stand-in work) and was run for real rather than asserted.

## Per-family results

Boundary families this tool was exploded into, and what each one's own real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | CLEAN |
| `java9` | JDK 11 host, `--release 9` | CLEAN |
| `java16` | JDK 17 host, `--release 16` | CLEAN |
| `java24` | JDK 25 host, `--release 24` | CLEAN |
| `java25` | JDK 25 (native) | CLEAN |

