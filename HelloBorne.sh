#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"
source ./scripts/env.sh

java -cp ".:projet/HelloBorne:${MG2D_HOME}" HelloBorne
