# pydriller

Synthetic, clean-by-design Java project for **pydriller**.

Domain: orchard-ledger harvest records (`OrchardLedger`).

**Measured**: installed (or built from real source) and actually invoked in the build environment; the result below is real, not asserted.

## What a passing result looks like

PyDriller mines OrchardLedger's real git history and reports the expected commit count, author set, and modified-file list for every commit -- a real repository, not an asserted one.

## Command

```bash
python3 -c "
from pydriller import Repository
commits = list(Repository('.').traverse_commits())
authors = sorted({c.author.name for c in commits})
print('COMMITS=%d AUTHORS=%s' % (len(commits), authors))
"
```

Expected: `COMMITS=4 AUTHORS=['Ada Renwick', 'Mikkel Aas', 'Priya Nallan']`

## Layout

```text
pydriller/
  README.md
  src/main/java/orchardledger/OrchardLedger.java
  src/test/java/orchardledger/OrchardLedgerTest.java
  .git/                                              real repo: 4 commits, 3 authors
```

## Notes

PyDriller is a Python tool that mines any git repository regardless of the language committed to it, so this folder's real git history is what it inspects -- not Java source parsing. The history has 4 real commits from the same three synthetic co-authors already used for this purpose in the sibling Python and JavaScript corpora: Ada Renwick (2026-05-01), Mikkel Aas (2026-05-03), and Priya Nallan (2026-05-06, two commits). PyDriller and diff-cover are each other's cross-check in spirit across this corpus -- both read the same kind of real git history, from different angles (commit/author mining vs. line-diff coverage), so this folder and the diff-cover folder deliberately hold different code and different histories.
