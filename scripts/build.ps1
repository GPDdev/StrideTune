param([switch]$Check)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$env:GRADLE_USER_HOME = Join-Path $projectRoot '.tools/gradle-home'
$gradle = Join-Path $projectRoot '.tools/gradle-8.11.1/bin/gradle.bat'
if (-not (Test-Path -LiteralPath $gradle)) { throw 'Run python scripts/bootstrap.py first.' }
Push-Location $projectRoot
try {
    $tasks = @('assembleDebug')
    if ($Check) { $tasks += 'testDebugUnitTest'; $tasks += 'lintDebug' }
    & $gradle @tasks --console=plain --no-daemon
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed: $LASTEXITCODE" }
    New-Item -ItemType Directory -Path outputs -Force | Out-Null
    Copy-Item -LiteralPath 'app/build/outputs/apk/debug/app-debug.apk' -Destination 'outputs/RunMusic-0.1.0.apk'
} finally { Pop-Location }
