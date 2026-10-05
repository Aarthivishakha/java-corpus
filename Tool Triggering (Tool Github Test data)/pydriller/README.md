# pydriller

Inverse-corpus counterpart to the sibling Java-Tools-Clean's
**pydriller** folder. Engineered so the real tool finds something
genuinely wrong, confirmed by actually running it -- not asserted.

Domain: pasture-plot grazing-rotation records (`PasturePlot`).

**Measured**: installed and actually invoked in the build environment;
the result below is real, not asserted.

## What a failing result looks like

Each commit's message carries an `[area:X]` tag claiming which part of
the codebase it touches. `driver.py` walks PasturePlot's real git
history with pydriller's own `Repository(...).traverse_commits()` and
checks whether each tagged commit's claim is corroborated by the files
it actually modified (`ledger` should touch `src/`, `docs` should touch
`docs/`; `meta` has no corroborating directory at all). **5 of 8
tagged commits (62.5%) mismatch their own claimed area** -- a real,
majority-wrong result mined from real commit history, not asserted.

## Command

```bash
python3 driver.py
```

Measured: `Total tagged commits: 8` / `Mismatches: 5 (62.5%)`.

## Layout

```text
pydriller/
  README.md
  driver.py                                          real pydriller.Repository(...).traverse_commits() walk
  src/main/java/pastureplot/PasturePlot.java
  .git/                                               real repo: 9 commits (1 untagged init + 8 tagged)
```

## Notes

PyDriller is a Python tool that mines any git repository regardless of
the language committed to it, so this folder's real git history is
what it inspects -- not Java source parsing. The repository has 9 real
commits: an untagged initial commit, then 8 `[area:ledger|docs|meta]`
-tagged commits, 5 of which deliberately touch a different part of the
tree than their own tag claims (and every `[area:meta]` commit is, by
construction, a mismatch -- "meta" names no corroborating directory at
all). This mirrors the tagged-commit-area-mismatch convention already
used for this exact purpose in the sibling Python-Tools-Invalid,
TypeScript-Tools-Invalid and CSharp-Tools-Invalid corpora's own
pydriller folders.
