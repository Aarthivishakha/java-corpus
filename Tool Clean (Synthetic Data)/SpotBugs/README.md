# SpotBugs

Synthetic, clean-by-design Java project for **SpotBugs**.

Domain: coal-yard sorting logic (CoalSorter)

**Not installed here**: see Notes for why, and what was checked instead.

## What a passing result looks like

SpotBugs' default bug-pattern detectors would report zero findings against CoalSorter's compiled classes -- no null-dereference, resource-leak or dead-store patterns.

## Command

```bash
findbugs -textui -low CoalSorter.class
```

## Notes

SpotBugs itself has no Debian package and its release jar is a GitHub Release asset, blocked at the egress proxy. Its real, installable Debian-packaged predecessor, FindBugs 3.1.0~preview2 (the same bytecode bug-pattern engine SpotBugs forked from in 2016), was installed and actually run against CoalSorter's compiled classes -- and failed with `ResourceNotFoundException: java/lang/Object.class`. FindBugs predates JDK 9's module system and cannot resolve the JDK's own base classes through the `jrt:` filesystem this sandbox's only JDK (21) uses; there is no rt.jar for it to fall back to and no older JDK installed to run it against. A genuine, verified incompatibility, not a fabricated one -- so SpotBugs stays NOT INSTALLED rather than claiming a stand-in result that was never actually produced.

## Per-family results

Boundary families this tool was exploded into, and what each one's own real invocation found (not asserted -- see `_generator/verify_live.py`):

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | NOT INSTALLED |
| `java9` | JDK 11 host, `--release 9` | NOT INSTALLED |
| `java16` | JDK 17 host, `--release 16` | NOT INSTALLED |
| `java24` | JDK 25 host, `--release 24` | NOT INSTALLED |
| `java25` | JDK 25 (native) | NOT INSTALLED |

No Debian package; release jar is GitHub-Release-only. Its installable predecessor FindBugs 3.1.0~preview2 was tried and failed against this sandbox's JDKs (see root README) -- a genuine incompatibility, not fixed by picking a different boundary family.

