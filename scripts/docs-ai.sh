#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

BASE_REF="${1:-HEAD~1}"
DIFF_FILE=".tmp-doc-diff.patch"
OUTPUT_FILE="documentations/ia-suggestions.md"

if [ "${BASE_REF}" = "--working-tree" ]; then
  git diff > "${DIFF_FILE}"
else
  git diff "${BASE_REF}"...HEAD > "${DIFF_FILE}" || true
fi

python3 ./tools/ai_doc_patch.py \
  --diff "${DIFF_FILE}" \
  --docs-dir "documentations" \
  --output "${OUTPUT_FILE}"

rm -f "${DIFF_FILE}"

echo "[docs-ai] Terminé. Voir ${OUTPUT_FILE}"
