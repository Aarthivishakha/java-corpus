# CK

Inverse-corpus counterpart to the sibling Java-Tools-Clean's **CK**
folder. Stays **NOT_INSTALLED** here too -- the blocker is
infrastructure (Maven Central / GitHub Releases both blocked), not
anything about this corpus being Clean or Invalid, so there is nothing
to invert.

Package: CK 0.7.0 (GitHub Release jar, depends on JavaParser via Maven Central)

Domain: ridge-trail routing (RidgeSurveyor) -- static OO metrics target

**Not installed here**: see Notes for why, and what was checked instead.

## What a result would look like

CK's per-class/per-method static metrics (WMC, CBO, LCOM, RFC) would
read against RidgeSurveyor. Not measurable here: see Notes.

## Command

```bash
java -jar ck.jar <src> false 0 false <out>
```

## Notes

CK 0.7.0 depends on JavaParser (`com.github.javaparser:javaparser-core`),
which is not reachable via Maven Central here, and CK's own release jar
is a GitHub Release asset -- both channels return 403 at this sandbox's
egress proxy. Identical to the sibling Clean corpus's own finding.

## Per-family results

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | NOT INSTALLED |
| `java9` | JDK 11 host, `--release 9` | NOT INSTALLED |
| `java16` | JDK 17 host, `--release 16` | NOT INSTALLED |
| `java24` | JDK 25 host, `--release 24` | NOT INSTALLED |
| `java25` | JDK 25 (native) | NOT INSTALLED |

CK 0.7.0 needs JavaParser, resolved from Maven Central, which is
blocked at this sandbox's egress proxy; CK's own release jar is a
GitHub Release asset too. No JDK family changes this.
