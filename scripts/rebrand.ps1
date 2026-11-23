<#
.SYNOPSIS
    Renames the Kotlin/Java package roots from `com.metrolist` to `io.github.museakheeyyy.muse`.

.DESCRIPTION
    Phase 2 of the Muse rebrand. Moves every `src/<sourceSet>/{kotlin,java}/com/metrolist`
    tree to `.../io/github/museakheeyyy/muse`, then rewrites package references in tracked
    text files. The script is idempotent: running it twice is a no-op.

    Deliberately NOT renamed:
      - `com.metrolist.music.listentogether.proto` - generated from the `metroproto`
        submodule, whose `.proto` files declare `java_package`. The submodule is upstream
        and must not be modified here.
      - Display strings (`Metrolist` -> `Muse`) - handled separately; attribution text
        must keep the upstream name.
      - `LICENSE`, `NOTICE`, `README.md`, `docs/`, `assets/`, issue templates - legal and
        upstream-facing text.

.PARAMETER DryRun
    Report the directory moves without touching anything.

.EXAMPLE
    ./scripts/rebrand.ps1 -DryRun
    ./scripts/rebrand.ps1
#>
[CmdletBinding()]
param(
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
Push-Location $root

try {
    $oldPath = 'com/metrolist'
    $newPath = 'io/github/museakheeyyy/muse'
    $oldDot = 'com.metrolist'
    $newDot = 'io.github.museakheeyyy.muse'
    # Packages that must keep the upstream name: generated proto code (metroproto
    # submodule) and the InnerTubeX dependency (separate repo, included via
    # settings.gradle.kts `includeBuild("../innertubex")`).
    $protected = @(
        'com.metrolist.music.listentogether.proto',
        'com.metrolist.innertubex'
    )

    # ---------------------------------------------------------------- 1. moves
    $trees = git ls-files |
        Where-Object { $_ -match '^(?<module>[^/]+)/src/[^/]+/(?:kotlin|java)/com/metrolist/' } |
        ForEach-Object { $Matches['module'] + '/' + (($_ -split '/com/metrolist/')[0] -replace '^[^/]+/', '') } |
        Sort-Object -Unique

    if (-not $trees) {
        Write-Host 'No com/metrolist source trees left; moves already applied.'
    }

    foreach ($tree in $trees) {
        $from = Join-Path $root "$tree/$oldPath"
        $to = Join-Path $root "$tree/$newPath"
        if (-not (Test-Path -LiteralPath $from)) { continue }
        if (Test-Path -LiteralPath $to) {
            throw "Destination already exists, refusing to merge: $to"
        }
        if ($DryRun) {
            Write-Host "move  $tree/$oldPath -> $tree/$newPath"
            continue
        }
        $parent = Split-Path -Parent $to
        if (-not (Test-Path -LiteralPath $parent)) {
            New-Item -ItemType Directory -Path $parent -Force | Out-Null
        }
        git mv $from $to
        if ($LASTEXITCODE -ne 0) { throw "git mv failed for $from" }
        Write-Host "moved $tree"
    }

    # ------------------------------------------------------------- 2. rewrite
    $extensions = @(
        '.kt', '.kts', '.java', '.xml', '.toml', '.pro', '.yml', '.yaml', '.json',
        '.txt', '.py', '.cjs', '.mjs', '.js', '.sh', '.ps1', '.proto', '.properties',
        '.xcconfig', '.plist', '.pbxproj', '.cfg', '.conf'
    )
    $excluded = @(
        '^metroproto/',
        '^LICENSE',
        '^NOTICE',
        '^README\.md$',
        '^docs/',
        '^assets/',
        '^scripts/rebrand\.ps1$',
        '^\.github/ISSUE_TEMPLATE/',
        '^packaging/',      # release packaging templates, handled in Phase 4
        '^flatpak/',        # Flatpak app id, handled in Phase 4
        '^Makefile$'        # FLATPAK_APP_ID, handled in Phase 4
    )

    $files = git ls-files | Where-Object {
        $path = $_
        if ($excluded | Where-Object { $path -match $_ }) { return $false }
        $ext = [System.IO.Path]::GetExtension($path).ToLowerInvariant()
        return $extensions -contains $ext
    }

    $changed = 0
    foreach ($file in $files) {
        $full = Join-Path $root $file
        if (-not (Test-Path -LiteralPath $full)) { continue }

        $bytes = [System.IO.File]::ReadAllBytes($full)
        $hasBom = $bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF
        $text = [System.IO.File]::ReadAllText($full)

        if (-not $text.Contains($oldDot) -and -not $text.Contains($oldPath)) { continue }

        # Park protected package names behind sentinels, rewrite, then restore.
        $updated = $text
        for ($i = 0; $i -lt $protected.Count; $i++) {
            $updated = $updated.Replace($protected[$i], "@@MUSE_KEEP_$i@@")
        }
        $updated = $updated.
            Replace("$oldDot.music", $newDot).
            Replace($oldDot, $newDot).
            Replace($oldPath, $newPath)
        for ($i = 0; $i -lt $protected.Count; $i++) {
            $updated = $updated.Replace("@@MUSE_KEEP_$i@@", $protected[$i])
        }

        if ($updated -ceq $text) { continue }
        $changed++
        if ($DryRun) { continue }

        $encoding = New-Object System.Text.UTF8Encoding($hasBom)
        [System.IO.File]::WriteAllText($full, $updated, $encoding)
    }

    Write-Host "$(if ($DryRun) { 'would rewrite' } else { 'rewrote' }) $changed file(s)."

    # --------------------------------------------------------------- 3. guard
    $left = git grep -l $oldDot -- . ':(exclude)metroproto' ':(exclude)LICENSE' ':(exclude)NOTICE' ':(exclude)README.md' ':(exclude)docs' ':(exclude)assets' ':(exclude)scripts/rebrand.ps1' 2>$null
    if ($DryRun) { return }

    if ($left) {
        Write-Host "Remaining references ($($left.Count) files):"
        $left | ForEach-Object { Write-Host "  $_" }
    } else {
        Write-Host 'Guard passed: no package references left.'
    }
} finally {
    Pop-Location
}
