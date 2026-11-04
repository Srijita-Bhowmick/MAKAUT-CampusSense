#!/usr/bin/env bash
# MAKAUT CampusSense - Dedicated Virtual Environment Python Runner
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if [ -f "$ROOT_DIR/.venv/bin/python" ]; then
    exec "$ROOT_DIR/.venv/bin/python" "$@"
elif [ -f "$ROOT_DIR/.venv/Scripts/python.exe" ]; then
    exec "$ROOT_DIR/.venv/Scripts/python.exe" "$@"
else
    echo "Error: Virtual environment (.venv) not found. Run python3 -m venv .venv" >&2
    exit 1
fi
