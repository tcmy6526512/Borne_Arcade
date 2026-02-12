#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"
cd projet/CursedWare

if ! command -v love >/dev/null 2>&1; then
	echo "[CursedWare] Love2D absent, tentative d'installation (sudo apt-get install -y love)..."
	sudo apt-get update
	sudo apt-get install -y love
fi

love .
