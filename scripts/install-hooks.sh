#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

if [ ! -d .git ]; then
  echo "Erreur: ce script doit être exécuté dans un dépôt git (dossier .git introuvable)." 1>&2
  exit 1
fi

install -m 0755 scripts/git-hooks/post-merge .git/hooks/post-merge
install -m 0755 scripts/git-hooks/post-checkout .git/hooks/post-checkout

echo "Hooks installés dans .git/hooks/ (post-merge, post-checkout)."
