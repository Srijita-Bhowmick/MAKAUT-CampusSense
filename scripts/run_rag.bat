@echo off
rem ==============================================================================
rem MAKAUT CampusSense — RAG Inference & Guardrail Service Runner (Windows CMD)
rem Executes scripts\rag_service.py strictly inside .venv
rem ==============================================================================
setlocal
set "ROOT_DIR=%~dp0.."
if exist "%ROOT_DIR%\.venv\Scripts\python.exe" (
    "%ROOT_DIR%\.venv\Scripts\python.exe" "%ROOT_DIR%\scripts\rag_service.py" %*
) else if exist "%ROOT_DIR%\.venv\bin\python.exe" (
    "%ROOT_DIR%\.venv\bin\python.exe" "%ROOT_DIR%\scripts\rag_service.py" %*
) else (
    echo [ERROR] Python virtual environment (.venv) not found.
    echo Please initialize .venv first.
    exit /b 1
)
