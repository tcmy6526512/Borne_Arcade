#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"
source ./scripts/env.sh

# Evite d'executer un .class obsolet apres un git pull.
javac -cp ".:${MG2D_HOME}" projet/HelloBorne/*.java

java -cp ".:projet/HelloBorne:${MG2D_HOME}" HelloBorne
