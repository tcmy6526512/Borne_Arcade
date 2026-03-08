#!/bin/bash
set -euo pipefail

# Determine automatiquement l'emplacement de MG2D.
#
# Les installations historiques utilisaient /home/pi/git/MG2D.
# Pour un setup portable: definir MG2D_HOME ou cloner MG2D a cote du depot.

if [ -n "${MG2D_HOME:-}" ] && [ -d "${MG2D_HOME}" ]; then
  export MG2D_HOME
  return 0 2>/dev/null || exit 0
fi

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

for candidate in \
  "${REPO_ROOT}/../MG2D" \
  "${REPO_ROOT}/MG2D" \
  "${HOME}/git/MG2D" \
  "/home/pi/git/MG2D"
do
  if [ -d "${candidate}" ]; then
    export MG2D_HOME="${candidate}"
    return 0 2>/dev/null || exit 0
  fi
done

cat 1>&2 <<'EOF'
MG2D introuvable.

Solutions :
  - exporter MG2D_HOME=/chemin/vers/MG2D
  - ou cloner MG2D à côté du dépôt :
      cd .. && git clone https://github.com/synave/MG2D.git

Chemins testés :
  - ../MG2D (frère du dépôt)
  - ./MG2D (dans le dépôt)
  - ~/git/MG2D
  - /home/pi/git/MG2D
EOF
exit 1
