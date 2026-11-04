# ==============================================================================
# MAKAUT CampusSense - Python Virtual Environment Setup (PowerShell)
# ==============================================================================
$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Split-Path -Parent $ScriptDir
Set-Location $RootDir

Write-Host "=== [1/3] Detecting Python 3 installation ===" -ForegroundColor Cyan
$PythonCmd = $null
if (Get-Command py -ErrorAction SilentlyContinue) {
    $PythonCmd = "py -3"
} elseif (Get-Command python -ErrorAction SilentlyContinue) {
    $PythonCmd = "python"
} elseif (Get-Command python3 -ErrorAction SilentlyContinue) {
    $PythonCmd = "python3"
} else {
    Write-Error "[ERROR] Python was not found on your system PATH. Please install Python 3.10+ from https://www.python.org/."
    exit 1
}

Write-Host "Using: $PythonCmd" -ForegroundColor Gray

Write-Host "=== [2/3] Setting up virtual environment in .venv ===" -ForegroundColor Cyan
$VenvPy = Join-Path $RootDir ".venv\Scripts\python.exe"
if (-not (Test-Path $VenvPy)) {
    Invoke-Expression "$PythonCmd -m venv .venv"
    Write-Host "Created .venv successfully." -ForegroundColor Green
} else {
    Write-Host ".venv already exists." -ForegroundColor Gray
}

Write-Host "=== [3/3] Installing dependencies from requirements.txt ===" -ForegroundColor Cyan
& $VenvPy -m pip install --upgrade pip --quiet
& $VenvPy -m pip install -r requirements.txt --quiet

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Green
Write-Host " [SUCCESS] Python virtual environment configured successfully!" -ForegroundColor Green
Write-Host " Activate anytime using:" -ForegroundColor Yellow
Write-Host "   .venv\Scripts\Activate.ps1" -ForegroundColor White
Write-Host " Or run scripts directly using:" -ForegroundColor Yellow
Write-Host "   powershell -File scripts\run_python.ps1 your_script.py" -ForegroundColor White
Write-Host "======================================================================" -ForegroundColor Green
