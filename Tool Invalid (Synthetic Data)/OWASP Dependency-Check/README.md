# OWASP Dependency-Check

Inverse-corpus counterpart to the sibling Java-Tools-Clean's **OWASP
Dependency-Check** folder. Stays **NOT_INSTALLED** here too -- the
blocker is infrastructure (Maven Central / the tool's own NVD data
feed both blocked), not anything about this corpus being Clean or
Invalid, so there is nothing to invert.

Package: org.owasp:dependency-check-maven / -cli (Maven Central)

Domain: supply-component registry auditing (SupplyRegistry)

**Not installed here**: see Notes for why, and what was checked instead.

## What a result would look like

A Dependency-Check scan of SupplyRegistry's declared components against
the NVD CVE feed would report known vulnerabilities. Not measurable
here: see Notes.

## Command

```bash
dependency-check.sh --project SupplyRegistry --scan .
```

## Notes

Dependency-Check's CLI distribution and its Maven plugin both resolve
from Maven Central, and its own vulnerability database update pulls
from NVD's own feed servers -- all unreachable at this sandbox's egress
proxy. Identical to the sibling Clean corpus's own finding.

## Per-family results

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | NOT INSTALLED |
| `java9` | JDK 11 host, `--release 9` | NOT INSTALLED |
| `java16` | JDK 17 host, `--release 16` | NOT INSTALLED |
| `java24` | JDK 25 host, `--release 24` | NOT INSTALLED |
| `java25` | JDK 25 (native) | NOT INSTALLED |
