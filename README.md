# Java Tool-Evaluation Corpus

`main` carries only this overview -- it holds no project code. Every other
branch in this repository is a complete, independently buildable project for
one exact combination of Java version, build system (`Maven`/`Ant + Ivy`/`Gradle Groovy DSL`/`Gradle Kotlin DSL`), packaging (`Thin jar`/`WAR`/`Shaded uber-jar`) and architecture (`Monolith`/`Microservices`).

This repository consolidates what used to be 90 separate small repositories
into one repository holding all 432 branches, so the full corpus for this
language lives in a single place.

## Branch naming

Every branch name encodes its own identity, so the name alone tells you what
it is without needing to open it:

```
JV_V{ver}_{BUNDLER}_{PKGMGR}_{ARCH}
```

## Versions in this corpus

18 Java versions, 24 branches each (one per bundler x package manager x
architecture combination):

| Java version | Branches |
|---|---|
| 8 | 24 |
| 9 | 24 |
| 10 | 24 |
| 11 | 24 |
| 12 | 24 |
| 13 | 24 |
| 14 | 24 |
| 15 | 24 |
| 16 | 24 |
| 17 | 24 |
| 18 | 24 |
| 19 | 24 |
| 20 | 24 |
| 21 | 24 |
| 22 | 24 |
| 23 | 24 |
| 24 | 24 |
| 25 | 24 |

## Finding a branch

Each branch's own `README.md` describes its exact cell (version, bundler,
package manager, architecture) and its full tool roster. `dataset.json` on
each branch is the machine-readable answer key for that cell.

## History

Branches were moved into this repository by relocating their existing git
history under a new name -- every commit, author, date and
`Co-authored-by:` trailer is unchanged from before consolidation. Nothing
was squashed or rewritten, so tools that read commit history (churn,
ownership, `pydriller`, etc.) see the same signal they always did.
