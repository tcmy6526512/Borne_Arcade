#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"
cd projet/PianoTile
python3 app/game.py