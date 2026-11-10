# MEMBER   : IT26101135 - [Name]
# MODULE   : Safe local preview startup
# WHAT IT DOES: Starts the built application with fictional records in a new folder.
# CRUD     : Does not change existing data folders or Git state.
param(
    [int]$Port = 18080,
    [string]$JavaHome,
    [switch]$AllowEarlyAttendance,
    [switch]$FreshShell
)

$ErrorActionPreference = 'Stop'

# Always run the preview in its own PowerShell process.
if (-not $FreshShell) {
    $childArguments = @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $PSCommandPath,
        '-FreshShell', '-Port', $Port)
    if ($JavaHome) { $childArguments += @('-JavaHome', $JavaHome) }
    if ($AllowEarlyAttendance) { $childArguments += '-AllowEarlyAttendance' }
    & powershell.exe @childArguments
    exit $LASTEXITCODE
}

$projectDirectory = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectDirectory
$warPath = Join-Path $projectDirectory 'target\drivespark-academy.war'

# Refuse to compete with an already running DriveSpark app.
$runningApp = Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" |
    Where-Object { $_.CommandLine -match 'com\.drivingschool\.DrivesparkAcademyApplication|drivespark-academy\.war' }
if ($runningApp) { throw 'Stop the running DriveSpark app before starting another preview.' }
if ($Port -lt 1024 -or $Port -gt 65535) { throw 'Choose a port between 1024 and 65535.' }
if (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) {
    throw "Port $Port is already in use. Stop that preview or choose another port."
}
if (-not (Test-Path -LiteralPath $warPath)) {
    throw 'Build the app first: .\mvnw.cmd clean verify. Stop any running app before building.'
}
$warTime = (Get-Item -LiteralPath $warPath).LastWriteTimeUtc
if (Get-ChildItem -LiteralPath (Join-Path $projectDirectory 'src\main') -Recurse -File |
        Where-Object { $_.LastWriteTimeUtc -gt $warTime } | Select-Object -First 1) {
    throw 'The app source is newer than the built WAR. Stop the app and run .\mvnw.cmd clean verify first.'
}
$javaCommand = if ($JavaHome) { Join-Path $JavaHome 'bin\java.exe' }
    elseif ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' }
    else { (Get-Command java.exe -ErrorAction Stop).Source }
if (-not (Test-Path -LiteralPath $javaCommand)) { throw 'Choose an installed Java 17 or later with -JavaHome.' }

$previewDirectory = Join-Path $env:TEMP ('drivespark-preview-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $previewDirectory | Out-Null
try {
    $env:DRIVESPARK_DEMO_SEED_ENABLED = 'true'
    $env:DRIVESPARK_ADMIN_SEED_ENABLED = 'false'
    $env:APP_DATA_DIRECTORY = $previewDirectory
    $env:DRIVESPARK_ATTENDANCE_REQUIRE_LESSON_ENDED = if ($AllowEarlyAttendance) { 'false' } else { 'true' }
    Write-Output "Preview data: $previewDirectory"
    Write-Output "Open http://localhost:$Port after startup. Use the README's fictional login accounts."
    if ($AllowEarlyAttendance) { Write-Output 'Early attendance is enabled for this isolated preview only.' }
    & $javaCommand -jar $warPath "--server.port=$Port"
    $previewExitCode = $LASTEXITCODE
} finally {
    Remove-Item Env:DRIVESPARK_DEMO_SEED_ENABLED, Env:DRIVESPARK_ADMIN_SEED_ENABLED,
        Env:APP_DATA_DIRECTORY, Env:DRIVESPARK_ATTENDANCE_REQUIRE_LESSON_ENDED -ErrorAction SilentlyContinue
    Write-Output 'Preview stopped; run-only demo variables cleared. Its temporary data folder was kept.'
}
exit $previewExitCode
