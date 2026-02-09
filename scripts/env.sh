#!/bin/bash
set -euo pipefail

# Resolves where MG2D is located.
#
# Historical installs used /home/pi/git/MG2D.
# For a portable setup, set MG2D_HOME, or keep MG2D as a sibling folder.

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
