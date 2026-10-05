# FindSecBugs

Inverse-corpus counterpart to the sibling Java-Tools-Clean's
**FindSecBugs** folder. Engineered so the real stand-in tool finds
something genuinely wrong, confirmed by actually running it -- not
asserted.

Domain: strongroom access control (StrongroomAccess)

**Not installed here** (FindSecBugs itself): see Notes for why, and
what was checked instead -- the same infrastructure gap as Clean's own
FindSecBugs folder.

## What a failing result looks like

FindSecBugs is a SpotBugs plugin and cannot run without SpotBugs
itself (also not installed here). `semgrep --config security-rules.yml`
-- the same local-ruleset stand-in already used for this purpose in the
sibling Clean corpus -- was run for real against StrongroomAccess and
found **3 of 4 real findings**: `java.util.Random` used for a security
value, MD5 used as a token hash, and a hash comparison done with
`.equals(...)` instead of a constant-time compare. Only the ruleset's
own `hardcoded-secret` rule (whose pattern only matches a literal
`"password..."`/`"secret..."` string value, not a prefix match) stays
silent -- a quirk of the ruleset itself, identical in both corpora.

## Command

```bash
semgrep --config security-rules.yml src/
```

## Notes

FindSecBugs adds detectors to SpotBugs; it cannot run, or disagree with
SpotBugs, on its own. With SpotBugs unreachable here, FindSecBugs is
unreachable too -- identical to the sibling Clean corpus's own finding.
semgrep (already installed from the sibling corpus's own stand-in work)
was run for real rather than asserted, against the exact same
`security-rules.yml` ruleset Clean's own FindSecBugs folder uses,
copied byte-for-byte.

## Per-family results

Boundary families this tool was exploded into, and what each one's own
real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | **FINDING** |
| `java9` | JDK 11 host, `--release 9` | **FINDING** |
| `java16` | JDK 17 host, `--release 16` | **FINDING** |
| `java24` | JDK 25 host, `--release 24` | **FINDING** |
| `java25` | JDK 25 (native) | **FINDING** |

Genuine finding(s) on this tool: 3 of 4 rules fire (75%), identical
across all five families since semgrep reads only source text.
