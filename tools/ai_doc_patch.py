#!/usr/bin/env python3
import argparse
import json
import os
import pathlib
import sys
import textwrap
import urllib.request


def read_text(path: pathlib.Path) -> str:
    if not path.exists():
        return ""
    return path.read_text(encoding="utf-8", errors="replace")


def collect_docs_snapshot(docs_dir: pathlib.Path, max_files: int = 8, max_chars_per_file: int = 2500) -> str:
    if not docs_dir.exists():
        return "(docs_dir introuvable)"

    files = sorted([p for p in docs_dir.glob("*.md") if p.is_file()])[:max_files]
    chunks = []
    for file in files:
        content = read_text(file)[:max_chars_per_file]
        chunks.append(f"## {file.name}\n{content}")
    return "\n\n".join(chunks) if chunks else "(aucun fichier markdown détecté)"


def call_ollama(model: str, prompt: str, host: str) -> str:
    body = json.dumps(
        {
            "model": model,
            "prompt": prompt,
            "stream": False,
            "options": {
                "temperature": 0.2,
            },
        }
    ).encode("utf-8")

    req = urllib.request.Request(
        f"{host.rstrip('/')}/api/generate",
        data=body,
        headers={"Content-Type": "application/json"},
        method="POST",
    )

    with urllib.request.urlopen(req, timeout=120) as resp:
        data = json.loads(resp.read().decode("utf-8", errors="replace"))
    return data.get("response", "")


def main() -> int:
    parser = argparse.ArgumentParser(description="Génère une proposition de mise à jour de doc via Ollama.")
    parser.add_argument("--diff", required=True, help="Chemin vers un fichier diff (git diff).")
    parser.add_argument("--docs-dir", default="documentations", help="Dossier docs markdown.")
    parser.add_argument("--output", default="documentations/ia-suggestions.md", help="Fichier de sortie markdown.")
    parser.add_argument("--model", default=os.getenv("OLLAMA_MODEL", "llama3.1"), help="Modèle Ollama")
    parser.add_argument("--host", default=os.getenv("OLLAMA_HOST", "http://127.0.0.1:11434"), help="URL Ollama")
    args = parser.parse_args()

    diff_path = pathlib.Path(args.diff)
    docs_dir = pathlib.Path(args.docs_dir)
    output_path = pathlib.Path(args.output)

    diff = read_text(diff_path)
    if not diff.strip():
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text(
            "# Suggestions IA (Ollama)\n\nAucun changement détecté dans le diff fourni.\n",
            encoding="utf-8",
        )
        print(f"[ai-doc] Aucun diff. Rapport vide écrit: {output_path}")
        return 0

    docs_snapshot = collect_docs_snapshot(docs_dir)

    prompt = textwrap.dedent(
        f"""
        Tu es un assistant documentation pour un projet de borne d'arcade.
        Réponds en français, en markdown, de façon concise et actionnable.

        Contraintes:
        - Proposer des changements docs uniquement (pas de code).
        - Produire des sections:
          1) Résumé des changements de code
          2) Pages docs impactées (fichiers + raison)
          3) Patchs proposés (blocs markdown prêts à copier)
          4) Checklist de validation humaine avant merge

        Diff de code:
        ---
        {diff[:50000]}
        ---

        Extraits docs actuels:
        ---
        {docs_snapshot[:20000]}
        ---
        """
    ).strip()

    try:
        response = call_ollama(args.model, prompt, args.host)
    except Exception as exc:
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text(
            "# Suggestions IA (Ollama)\n\n"
            f"Erreur d'appel Ollama: {exc}\n\n"
            "Vérifie que le service est lancé (`ollama serve`) et que le modèle est disponible.\n",
            encoding="utf-8",
        )
        print(f"[ai-doc] Erreur Ollama, rapport d'erreur écrit: {output_path}")
        return 1

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("# Suggestions IA (Ollama)\n\n" + response + "\n", encoding="utf-8")
    print(f"[ai-doc] Rapport généré: {output_path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
