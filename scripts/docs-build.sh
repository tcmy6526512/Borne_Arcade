#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

python3 -m venv .venv-docs
source .venv-docs/bin/activate

python -m pip install --upgrade pip setuptools wheel
pip install -r documentations/requirements.txt
mkdocs build -f documentations/mkdocs.yml

echo "Docs générées dans documentations/site/"
