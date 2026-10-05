# Restores the live .git folder for diff-cover and pydriller from _git-bundles/.
# Run from inside "Tool Clean (Synthetic Data)" or "Tool Invalid (Synthetic Data)".
param([string[]]$Tools = @("diff-cover", "pydriller"))
foreach ($tool in $Tools) {
    $bundle = Join-Path "_git-bundles" "$tool-gitdata.tar.gz"
    if (Test-Path $bundle) {
        tar -xzf $bundle -C $tool
        Write-Host "Restored .git for $tool"
    } else {
        Write-Host "No bundle found for $tool" -ForegroundColor Red
    }
}
