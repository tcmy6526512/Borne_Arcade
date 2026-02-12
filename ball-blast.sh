#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"
cd projet/ball-blast

if ! python3 -c "import pygame" >/dev/null 2>&1; then
	echo "[ball-blast] Installation des dépendances Python..."
	python3 -m pip install --user -r requirements.txt
fi

python3 ./src