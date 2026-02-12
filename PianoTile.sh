#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"
cd projet/PianoTile

if ! python3 -c "import pygame, librosa" >/dev/null 2>&1; then
 	 echo "[PianoTile] Installation de pygame..."
 	 python3 -m pip install --user pygame
fi

python3 app/game.py