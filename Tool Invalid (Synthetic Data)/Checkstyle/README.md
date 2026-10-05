# Checkstyle

Inverse-corpus counterpart to the sibling Java-Tools-Clean's **Checkstyle**
folder. Engineered so the real tool finds something genuinely wrong,
confirmed by actually running it -- not asserted.

Domain: valve-panel signal control (ValveStation)

**Measured**: installed and actually invoked in the build environment;
the result below is real, not asserted.

## What a failing result looks like

`checkstyle -c /usr/share/checkstyle/sun_checks.xml` reports a real
pile of `[ERROR]` lines against ValveStation's source: a star import, a
badly-cased constant and field, tab characters, missing Javadoc on
every public member, non-final parameters, and an over-length line --
**25 real errors**, confirmed by direct invocation.

## Command

```bash
checkstyle -c sun_checks.xml src/main/java/**/*.java
```

## Notes

Same installed `checkstyle-8.36.1.jar` as the sibling Clean corpus
(`apt install checkstyle`); only ValveStation's own source differs.

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

Genuine finding(s) on this tool: 25 real `[ERROR]` lines against a
single ~35-line class -- a clear majority of ValveStation's own
constructs (every field, every method, the only import, two lines)
trip at least one real sun_checks rule. Identical across all five
families since Checkstyle reads only source text, never bytecode.
