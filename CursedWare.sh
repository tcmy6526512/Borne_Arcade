#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"
cd projet/CursedWare
love .
