#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

REPO_ROOT="$(pwd)"
HOME_GIT_DIR="${HOME}/git"

echo "[install] Répertoire projet: ${REPO_ROOT}"

if [ "${1:-}" = "--apt" ] || [ -z "${1:-}" ]; then
  if command -v apt-get >/dev/null 2>&1; then
    echo "[install] Installation des dépendances (sudo)"
    sudo apt-get update
    sudo apt-get install -y git openjdk-17-jdk x11-xkb-utils
  fi
fi

echo "[install] Hooks Git"
./scripts/install-hooks.sh

echo "[install] Activation auto-deploy (.borne-auto-deploy)"
touch ./.borne-auto-deploy

echo "[install] Layout clavier XKB (sudo)"
if [ -f ./borne ]; then
  sudo cp ./borne /usr/share/X11/xkb/symbols/borne
  sudo chmod 644 /usr/share/X11/xkb/symbols/borne
fi

echo "[install] Installation systemd user service (menu)"
mkdir -p ~/.config/systemd/user

# Default template assumes repo is in ~/git/borne_arcade
# If not, we generate a service with explicit paths.
SERVICE_PATH=~/.config/systemd/user/borne-arcade.service

cat > "${SERVICE_PATH}" <<EOF
[Unit]
Description=Borne Arcade - Menu

[Service]
Type=simple
WorkingDirectory=${REPO_ROOT}
ExecStart=${REPO_ROOT}/scripts/run-menu.sh
Restart=on-failure
RestartSec=2

[Install]
WantedBy=default.target
EOF

systemctl --user daemon-reload || true
systemctl --user enable borne-arcade.service || true

echo "[install] Terminé."
echo "- Pour démarrer: systemctl --user start borne-arcade.service"
echo "- Après un git pull: le hook compile automatiquement si .borne-auto-deploy existe"
