#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

python3 -m venv .venv-docs
source .venv-docs/bin/activate

python -m pip install --upgrade pip setuptools wheel

# Evite une compilation depuis les sources sur les images i386.
pip install --only-binary=:all: MarkupSafe==3.0.0
pip install -r documentations/requirements.txt
# Genere le site statique a partir de la configuration MkDocs du projet.
python -m mkdocs build -f documentations/mkdocs.yml

echo "Docs generees dans site/"
