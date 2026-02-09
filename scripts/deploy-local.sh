#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

if [ ! -f ./.borne-auto-deploy ]; then
  echo "Auto-deploy désactivé (fichier .borne-auto-deploy absent)."
  exit 0
fi

echo "[deploy] Nettoyage"
./clean.sh

echo "[deploy] Compilation"
./compilation.sh

if command -v systemctl >/dev/null 2>&1; then
  if systemctl --user list-unit-files 2>/dev/null | grep -q '^borne-arcade\.service'; then
    echo "[deploy] Redémarrage du service utilisateur borne-arcade"
    systemctl --user restart borne-arcade.service
    exit 0
  fi
fi

echo "[deploy] Compilation terminée. (Service non redémarré : systemd user non configuré)"
