#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"
source ./scripts/env.sh

command -v xdotool >/dev/null 2>&1 && xdotool mousemove 1280 1024 || true
cd projet/Columns
touch highscore
java -cp ".:../..:${MG2D_HOME}" Main
