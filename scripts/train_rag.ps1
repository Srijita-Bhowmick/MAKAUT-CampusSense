# ==============================================================================
# MAKAUT CampusSense — Training & Ingestion Wrapper (PowerShell)
# Executes scripts/ingest_makaut_knowledge.py strictly inside .venv
# ==============================================================================
$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Split-Path -Parent $ScriptDir

$PythonExe = Join-Path $RootDir ".venv\Scripts\python.exe"
if (-not (Test-Path $PythonExe)) {
    $PythonExe = Join-Path $RootDir ".venv\bin\python.exe"
}

if (-not (Test-Path $PythonExe)) {
    Write-Error "[ERROR] Python virtual environment (.venv) not found. Run scripts\setup_env.bat first."
    exit 1
}

$IngestScript = Join-Path $RootDir "scripts\ingest_makaut_knowledge.py"
& $PythonExe $IngestScript @args
