# restore-invalid-git.ps1
#
# Restores the real git history for diff-cover and pydriller inside
# Java-Tools-Invalid. The device-bridge transfer that copied this corpus
# cannot carry ".git" directories, so these two tools' git repos
# (diff-cover: a main/feature branch pair; pydriller: a 9-commit
# single-branch history) were packed into .tar.gz bundles instead and
# sent alongside the corpus tarball. This script unpacks each bundle
# into the matching folder's .git directory.
#
# Usage: extract Java-Tools-Invalid.tar.gz first (so the diff-cover/
# pydriller folders exist), then from the "Java Tools" folder that holds
# both the extracted corpus and the _git-bundles folder, run:
#   .\restore-invalid-git.ps1
#
# Requires Windows 10/11's built-in tar.exe (bsdtar), which ships by
# default -- no extra install needed.

$ErrorActionPreference = "Stop"

$root = $PSScriptRoot
if (-not $root) { $root = Get-Location }

$bundleDir = Join-Path $root "_git-bundles"
$corpusDir = Join-Path $root "Java-Tools-Invalid"

if (-not (Test-Path $bundleDir)) {
    Write-Error "Could not find '_git-bundles' folder next to this script."
    exit 1
}
if (-not (Test-Path $corpusDir)) {
    Write-Error "Could not find 'Java-Tools-Invalid' folder next to this script. Extract Java-Tools-Invalid.tar.gz first."
    exit 1
}

$tools = @("diff-cover", "pydriller")
$okCount = 0
$failCount = 0

foreach ($name in $tools) {
    $target = Join-Path $corpusDir $name
    $bundle = Join-Path $bundleDir "$name-gitdata.tar.gz"

    Write-Host "== $name ==" -ForegroundColor Cyan

    if (-not (Test-Path $bundle)) {
        Write-Warning "  Bundle not found: $bundle - skipping."
        $failCount++
        continue
    }
    if (-not (Test-Path $target)) {
        Write-Warning "  Target folder not found: $target - skipping."
        $failCount++
        continue
    }

    $existingGit = Join-Path $target ".git"
    if (Test-Path $existingGit) {
        Write-Host "  Removing existing .git at $existingGit"
        Remove-Item -Recurse -Force $existingGit
    }

    Write-Host "  Extracting $bundle -> $target"
    tar -xzf $bundle -C $target

    if (Test-Path $existingGit) {
        Write-Host "  OK - .git restored" -ForegroundColor Green
        $okCount++
    } else {
        Write-Warning "  Extraction did not produce a .git folder - check manually."
        $failCount++
    }
}

Write-Host ""
Write-Host "Done. $okCount of $($tools.Count) repos restored." -ForegroundColor Cyan
if ($failCount -gt 0) {
    Write-Warning "$failCount repo(s) need manual attention - see warnings above."
}

Write-Host ""
Write-Host "Verify with, e.g.:" -ForegroundColor Yellow
Write-Host "  cd Java-Tools-Invalid\pydriller; git log --oneline; git branch -a"
Write-Host "  cd Java-Tools-Invalid\diff-cover; git log --oneline --all; git branch -a"
