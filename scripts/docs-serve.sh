#!/bin/bash
set -euo pipefail

if [[ "${DEBUG:-}" == "1" ]]; then
	set -x
fi

cd "$(dirname "$0")/.."

python3 -m venv .venv-docs
source .venv-docs/bin/activate

python -m pip install --upgrade pip setuptools wheel

# Avoid building from source on i386 images.
pip install --only-binary=:all: MarkupSafe==3.0.0
pip install -r documentations/requirements.txt

DOCS_HOST="${DOCS_HOST:-127.0.0.1}"
DOCS_PORT="${DOCS_PORT:-8000}"

echo "Docs: http://${DOCS_HOST}:${DOCS_PORT}/"
echo "Running: mkdocs serve -f documentations/mkdocs.yml -a ${DOCS_HOST}:${DOCS_PORT}"

mkdocs serve -f documentations/mkdocs.yml -a "${DOCS_HOST}:${DOCS_PORT}"
