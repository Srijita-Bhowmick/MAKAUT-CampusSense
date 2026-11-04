# ==============================================================================
# MAKAUT CampusSense - Dedicated Virtual Environment Python Runner (PowerShell)
# ==============================================================================
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Split-Path -Parent $ScriptDir

$PyExeWindows = Join-Path $RootDir ".venv\Scripts\python.exe"
$PyExePosix = Join-Path $RootDir ".venv\bin\python.exe"
$PyExeMac = Join-Path $RootDir ".venv/bin/python"

if (Test-Path $PyExeWindows) {
    & $PyExeWindows $args
} elseif (Test-Path $PyExePosix) {
    & $PyExePosix $args
} elseif (Test-Path $PyExeMac) {
    & $PyExeMac $args
} else {
    Write-Error "[ERROR] Virtual environment (.venv) not found. Run scripts\setup_env.ps1 to initialize your Python environment."
    exit 1
}
