@echo off
rem ==============================================================================
rem MAKAUT CampusSense - Python Virtual Environment Setup (Windows CMD)
rem ==============================================================================
setlocal EnableDelayedExpansion

set "ROOT_DIR=%~dp0.."
cd /d "%ROOT_DIR%"

echo === [1/3] Detecting Python 3 installation ===
where py >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set "PYTHON_CMD=py -3"
) else (
    where python >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        set "PYTHON_CMD=python"
    ) else (
        echo [ERROR] Python was not found on your system PATH.
        echo Please install Python 3.10+ from https://www.python.org/ and check "Add Python to PATH".
        exit /b 1
    )
)
echo Using: %PYTHON_CMD%

echo === [2/3] Setting up virtual environment in .venv ===
if not exist ".venv\Scripts\python.exe" (
    %PYTHON_CMD% -m venv .venv
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Failed to create virtual environment.
        exit /b 1
    )
    echo Created .venv successfully.
) else (
    echo .venv already exists.
)

echo === [3/3] Installing dependencies from requirements.txt ===
".venv\Scripts\python.exe" -m pip install --upgrade pip --quiet
".venv\Scripts\python.exe" -m pip install -r requirements.txt --quiet

echo.
echo ======================================================================
echo  [SUCCESS] Python virtual environment configured successfully!
echo  Activate anytime using:
echo    .venv\Scripts\activate.bat
echo  Or run scripts directly using:
echo    scripts\run_python.bat your_script.py
echo ======================================================================

endlocal
