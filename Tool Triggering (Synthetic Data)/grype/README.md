# Grype — SCA alternative

Alternative for all 8 Dependency Risk metrics, cross-checking OWASP Dependency-Check.
A Go binary, not a JVM tool: version-agnostic, needs no JDK.

    grype dir:. --output json --file tools/grype/out/grype.json

Grype and Dependency-Check are genuinely independent engines with different vulnerability
databases. This is one of the few blocks in the sheet with a real cross-check rather than
the same tool appearing in both columns.
