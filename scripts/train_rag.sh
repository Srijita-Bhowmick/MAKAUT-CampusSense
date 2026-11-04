#!/usr/bin/env bash
# ==============================================================================
# MAKAUT CampusSense — Training & Ingestion Wrapper
# Executes scripts/ingest_makaut_knowledge.py strictly inside .venv
# ==============================================================================
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [ ! -f "$ROOT_DIR/.venv/bin/python" ]; then
    echo "[ERROR] Python virtual environment (.venv) not found. Run ./scripts/setup_env.sh first." >&2
    exit 1
fi

"$ROOT_DIR/.venv/bin/python" "$ROOT_DIR/scripts/ingest_makaut_knowledge.py" "$@"
