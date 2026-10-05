# SpotBugs

Inverse-corpus counterpart to the sibling Java-Tools-Clean's
**SpotBugs** folder. Stays **NOT_INSTALLED** here too -- the blocker is
infrastructure (Maven Central / GitHub Releases both blocked), not
anything about this corpus being Clean or Invalid, so there is nothing
to invert.

Package: com.github.spotbugs:spotbugs (Maven Central / GitHub Release)

Domain: coastal-beacon signal-band watching (BeaconWatch)

**Not installed here**: see Notes for why, and what was checked instead.

## What a result would look like

A real SpotBugs static-analysis pass over BeaconWatch's compiled
classes would report any matching bug pattern. Not measurable here:
see Notes.

## Command

```bash
spotbugs -textui -effort:max build/main
```

## Notes

SpotBugs's standalone distribution is a GitHub Release zip, and its
Maven plugin resolves from Maven Central -- both unreachable at this
sandbox's egress proxy. Identical to the sibling Clean corpus's own
finding; FindSecBugs, which adds detectors on top of SpotBugs, is
unreachable for the same underlying reason (see this corpus's own
FindSecBugs folder).

## Per-family results

| Family | Host JDK | Result |
|---|---|---|
| `java8` | JDK 8 (native) | NOT INSTALLED |
| `java9` | JDK 11 host, `--release 9` | NOT INSTALLED |
| `java16` | JDK 17 host, `--release 16` | NOT INSTALLED |
| `java24` | JDK 25 host, `--release 24` | NOT INSTALLED |
| `java25` | JDK 25 (native) | NOT INSTALLED |
