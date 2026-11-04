#!/usr/bin/env bash
# ==============================================================================
# MAKAUT CampusSense — RAG Inference & Guardrail Service Runner
# Executes scripts/rag_service.py strictly inside .venv
# ==============================================================================
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [ ! -f "$ROOT_DIR/.venv/bin/python" ]; then
    echo "[ERROR] Python virtual environment (.venv) not found. Run ./scripts/setup_env.sh first." >&2
    exit 1
fi

"$ROOT_DIR/.venv/bin/python" "$ROOT_DIR/scripts/rag_service.py" "$@"
