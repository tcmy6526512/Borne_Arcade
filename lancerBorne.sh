#!/bin/bash

set -euo pipefail

cd "$(dirname "$0")"

source ./scripts/env.sh

if [ "${SKIP_XKB:-0}" != "1" ]; then
    setxkbmap borne || true
fi
echo "nettoyage des répertoires"
echo "Veuillez patienter"
./clean.sh
./compilation.sh

echo "Lancement du  Menu"
echo "Veuillez patienter"

java -cp ".:${MG2D_HOME}" Main

./clean.sh

if [ "${NO_SHUTDOWN:-0}" = "1" ]; then
    exit 0
fi

for i in {30..1}
do
    echo Extinction de la borne dans $i secondes
    sleep 1
done

sudo halt
