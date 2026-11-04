@echo off
rem MAKAUT CampusSense - Dedicated Virtual Environment Python Runner (Windows CMD)
set "ROOT_DIR=%~dp0.."
if exist "%ROOT_DIR%\.venv\Scripts\python.exe" (
    "%ROOT_DIR%\.venv\Scripts\python.exe" %*
) else if exist "%ROOT_DIR%\.venv\bin\python.exe" (
    "%ROOT_DIR%\.venv\bin\python.exe" %*
) else (
    echo [ERROR] Virtual environment (.venv) not found.
    echo Run scripts\setup_env.bat to initialize your Python environment.
    exit /b 1
)
