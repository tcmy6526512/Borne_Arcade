#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

python3 -m venv .venv-docs
source .venv-docs/bin/activate

python -m pip install --upgrade pip setuptools wheel

# Avoid building from source on i386 images.
pip install --only-binary=:all: MarkupSafe==3.0.0
pip install --only-binary=:all: -r documentations/requirements.txt
mkdocs build -f documentations/mkdocs.yml

echo "Docs générées dans documentations/site/"
