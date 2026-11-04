@echo off
rem ==============================================================================
rem MAKAUT CampusSense — Training & Ingestion Wrapper (Windows CMD)
rem Executes scripts\ingest_makaut_knowledge.py strictly inside .venv
rem ==============================================================================
setlocal
set "ROOT_DIR=%~dp0.."
if exist "%ROOT_DIR%\.venv\Scripts\python.exe" (
    "%ROOT_DIR%\.venv\Scripts\python.exe" "%ROOT_DIR%\scripts\ingest_makaut_knowledge.py" %*
) else if exist "%ROOT_DIR%\.venv\bin\python.exe" (
    "%ROOT_DIR%\.venv\bin\python.exe" "%ROOT_DIR%\scripts\ingest_makaut_knowledge.py" %*
) else (
    echo [ERROR] Python virtual environment (.venv) not found.
    echo Please initialize .venv first.
    exit /b 1
)
