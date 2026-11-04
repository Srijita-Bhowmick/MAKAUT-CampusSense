# ==============================================================================
# MAKAUT CampusSense — RAG Inference & Guardrail Service Runner (PowerShell)
# Executes scripts/rag_service.py strictly inside .venv
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

$RagScript = Join-Path $RootDir "scripts\rag_service.py"
& $PythonExe $RagScript @args
