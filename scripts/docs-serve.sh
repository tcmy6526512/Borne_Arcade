#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

python3 -m venv .venv-docs
source .venv-docs/bin/activate

python -m pip install --upgrade pip setuptools wheel
pip install -r documentations/requirements.txt
mkdocs serve -f documentations/mkdocs.yml -a 127.0.0.1:8000
