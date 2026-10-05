# Cleanup script for Java-Tools-Clean: removes each versionable tool's
# stale flat-layout src/ folder (the pre-family-split src/main + src/test
# at each tool's own root) now that content lives in java8/9/16/24/25
# subfolders. Mirrors the same cleanup already run for Python-Tools-Clean
# and JavaScript-Tools-Clean.
#
# NOTE: only src/ is stale. Do NOT remove tools/, vendor/, or
# security-rules.yml -- those are shared, family-independent
# infrastructure (the DefUseRunner/JacocoRunner drivers, the built
# asm-defuse.jar, FindSecBugs's local ruleset) still referenced by every
# family.
#
# Run this from PowerShell on the machine holding the corpus.
# A -WhatIf dry run is included first -- inspect its output before
# uncommenting the real deletion loop below it.

$root = "C:\Users\Prajith K\Desktop\Java Tools\Java-Tools-Clean"

$tools = @(
    "ASM-DefUse", "CK", "CPD", "Checkstyle", "FindSecBugs", "Grype",
    "JaCoCo", "Lizard", "OWASP Dependency-Check", "PIT", "PMD", "Spoon",
    "SpotBugs", "ba-dua"
)

# --- DRY RUN (safe to run as-is) ---
foreach ($t in $tools) {
    $srcPath = Join-Path (Join-Path $root $t) "src"
    if (Test-Path $srcPath) {
        Remove-Item -Path $srcPath -Recurse -Force -WhatIf
    }
}

# --- REAL DELETION (uncomment after checking the dry-run output above) ---
# foreach ($t in $tools) {
#     $srcPath = Join-Path (Join-Path $root $t) "src"
#     if (Test-Path $srcPath) {
#         Remove-Item -Path $srcPath -Recurse -Force
#     }
# }
