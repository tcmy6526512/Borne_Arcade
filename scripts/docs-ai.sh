#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

BASE_REF="${1:-HEAD~1}"
DIFF_FILE=".tmp-doc-diff.patch"
OUTPUT_FILE="documentations/ia-suggestions.md"
OLLAMA_HOST="${OLLAMA_HOST:-http://10.22.28.190:11434}"
OLLAMA_MODEL="${OLLAMA_MODEL:-llama3.1}"
OLLAMA_USE_WRAPPER="${OLLAMA_USE_WRAPPER:-1}"
OLLAMA_WRAPPER_PATH="${OLLAMA_WRAPPER_PATH:-tools/ollama_wrapper_iut.py}"

echo "[docs-ai] Host Ollama: ${OLLAMA_HOST}"
echo "[docs-ai] Modèle Ollama: ${OLLAMA_MODEL}"
echo "[docs-ai] Wrapper IUT: ${OLLAMA_USE_WRAPPER} (${OLLAMA_WRAPPER_PATH})"

if ! command -v python3 >/dev/null 2>&1; then
  echo "[docs-ai] Erreur: python3 introuvable." >&2
  exit 1
fi

if command -v curl >/dev/null 2>&1; then
  if ! curl -fsS "${OLLAMA_HOST}/api/tags" >/dev/null 2>&1; then
    echo "[docs-ai] Avertissement: Ollama semble indisponible sur ${OLLAMA_HOST}." >&2
    echo "[docs-ai] Vérifie le Wi-Fi IUT et l'adresse OLLAMA_HOST." >&2
  fi
fi

if [ "${BASE_REF}" = "--working-tree" ]; then
  git diff > "${DIFF_FILE}"
else
  git diff "${BASE_REF}"...HEAD > "${DIFF_FILE}" || true
fi

set +e
python3 ./tools/ai_doc_patch.py \
  --diff "${DIFF_FILE}" \
  --docs-dir "documentations" \
  --output "${OUTPUT_FILE}" \
  --model "${OLLAMA_MODEL}" \
  --host "${OLLAMA_HOST}" \
  --use-wrapper
RC=$?
set -e

rm -f "${DIFF_FILE}"

if [ ${RC} -ne 0 ]; then
  echo "[docs-ai] Échec génération IA. Détails:"
  if [ -f "${OUTPUT_FILE}" ]; then
    sed -n '1,80p' "${OUTPUT_FILE}"
  fi
  exit ${RC}
fi

echo "[docs-ai] Terminé. Voir ${OUTPUT_FILE}"
