#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

TS="$(date +%Y%m%d_%H%M%S)"
DIST_DIR="dist"
ARCHIVE_BASENAME="borne_arcade_final_${TS}"
ARCHIVE_PATH="${DIST_DIR}/${ARCHIVE_BASENAME}.tar.gz"

mkdir -p "${DIST_DIR}"

echo "[archive] Generation documentation"
./scripts/docs-build.sh

echo "[archive] Creation archive ${ARCHIVE_PATH}"
tar \
  --exclude=".git" \
  --exclude=".venv-docs" \
  --exclude="dist" \
  --exclude="site" \
  -czf "${ARCHIVE_PATH}" \
  .

echo "[archive] Checksum"
sha256sum "${ARCHIVE_PATH}" > "${ARCHIVE_PATH}.sha256"

echo "[archive] Termine"
echo "- Archive : ${ARCHIVE_PATH}"
echo "- SHA256  : ${ARCHIVE_PATH}.sha256"
