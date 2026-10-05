# Checkstyle

Synthetic, clean-by-design Java project for **Checkstyle**.

Domain: relay-panel signal control (RelayPanel)

**Measured**: installed (or built from real source) and actually invoked in the build environment; the result below is real, not asserted.

## What a passing result looks like

`checkstyle -c /usr/share/checkstyle/sun_checks.xml` (Sun/Oracle conventions, the jar's own bundled ruleset) reports zero errors and zero warnings against RelayPanel's source.

## Command

```bash
checkstyle -c sun_checks.xml src/main/java/**/*.java
```

## Notes

Installed via `apt install checkstyle`, which resolves entirely from the Ubuntu archive (not Maven Central) and drops a real `/usr/bin/checkstyle` wrapper around `checkstyle-8.36.1.jar`. Genuinely older than current Checkstyle (14.x), but a real, installed, invoked release.

## Per-family results

Boundary families this tool was exploded into, and what each one's own real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | CLEAN |
| `java9` | JDK 11 host, `--release 9` | CLEAN |
| `java16` | JDK 17 host, `--release 16` | CLEAN |
| `java24` | JDK 25 host, `--release 24` | CLEAN |
| `java25` | JDK 25 (native) | CLEAN |

