#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"
cd projet/PianoTile

if ! python3 -c "import pygame, librosa" >/dev/null 2>&1; then
	echo "[PianoTile] Installation des dépendances Python (pygame, librosa)..."
	python3 -m pip install --user pygame librosa
fi

python3 app/game.py