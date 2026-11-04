#!/usr/bin/env bash
# ==============================================================================
# MAKAUT CampusSense - Python Virtual Environment Setup (macOS & Linux)
# ==============================================================================
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "=== [1/3] Detecting Python 3 installation ==="
if command -v python3 >/dev/null 2>&1; then
    PYTHON_CMD="python3"
elif command -v python >/dev/null 2>&1; then
    PYTHON_CMD="python"
else
    echo "[ERROR] Python 3 was not found on your system PATH." >&2
    echo "Please install Python 3.10+ from https://www.python.org/ or via brew install python@3." >&2
    exit 1
fi
echo "Using: $($PYTHON_CMD --version)"

echo "=== [2/3] Setting up virtual environment in .venv ==="
if [ ! -d ".venv" ]; then
    $PYTHON_CMD -m venv .venv
    echo "Created .venv successfully."
else
    echo ".venv already exists."
fi

echo "=== [3/3] Installing dependencies from requirements.txt ==="
"$ROOT_DIR/.venv/bin/python" -m pip install --upgrade pip --quiet
"$ROOT_DIR/.venv/bin/python" -m pip install -r requirements.txt --quiet

echo ""
echo "======================================================================"
echo " [SUCCESS] Python virtual environment configured successfully!"
echo " Activate anytime using:"
echo "   source .venv/bin/activate"
echo " Or run scripts directly using:"
echo "   ./scripts/run_python.sh your_script.py"
echo "======================================================================"
