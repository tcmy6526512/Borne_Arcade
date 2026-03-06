#!/usr/bin/env python3
import argparse
import importlib.util
import json
import os
import pathlib
import sys
import textwrap
import urllib.request
import urllib.error


def read_text(path: pathlib.Path) -> str:
    if not path.exists():
        return ""
    return path.read_text(encoding="utf-8", errors="replace")


def collect_docs_snapshot(docs_dir: pathlib.Path, max_files: int = 6, max_chars_per_file: int = 1400) -> str:
    # Echantillonne quelques pages docs pour donner du contexte a l'IA.
    if not docs_dir.exists():
        return "(docs_dir introuvable)"

    files = sorted([p for p in docs_dir.glob("*.md") if p.is_file()])[:max_files]
    chunks = []
    for file in files:
        content = read_text(file)[:max_chars_per_file]
        chunks.append(f"## {file.name}\n{content}")
    return "\n\n".join(chunks) if chunks else "(aucun fichier markdown détecté)"


def call_ollama(model: str, prompt: str, host: str, timeout_s: float) -> str:
    # Appel HTTP direct de l'API Ollama (/api/generate).
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

    with urllib.request.urlopen(req, timeout=timeout_s) as resp:
        data = json.loads(resp.read().decode("utf-8", errors="replace"))
    return data.get("response", "")


def list_models(host: str) -> list[str]:
    req = urllib.request.Request(
        f"{host.rstrip('/')}/api/tags",
        headers={"Accept": "application/json"},
        method="GET",
    )
    with urllib.request.urlopen(req, timeout=30) as resp:
        payload = json.loads(resp.read().decode("utf-8", errors="replace"))
    models = payload.get("models", []) if isinstance(payload, dict) else []
    names = []
    for item in models:
        if isinstance(item, dict) and isinstance(item.get("name"), str):
            names.append(item["name"])
    return names


def pick_generation_model(preferred: str, available_models: list[str]) -> str:
    if not available_models:
        return preferred

    def is_embedding(name: str) -> bool:
        lowered = name.lower()
        return "embed" in lowered or "embedding" in lowered

    if preferred in available_models and not is_embedding(preferred):
        return preferred

    candidates = [m for m in available_models if not is_embedding(m)]
    if not candidates:
        return preferred

    for good in ["qwen3:8b", "qwen2:latest", "gemma2:latest", "qwen2.5vl:7b"]:
        if good in candidates:
            return good

    return candidates[0]


def call_ollama_with_iut_wrapper(model: str, prompt: str, host: str, timeout_s: float) -> str:
    # Charge dynamiquement le wrapper pour centraliser la gestion reseau/erreurs.
    wrapper_path = os.getenv("OLLAMA_WRAPPER_PATH", "tools/ollama_wrapper_iut.py")
    path = pathlib.Path(wrapper_path)
    if not path.exists():
        raise FileNotFoundError(f"Wrapper introuvable: {wrapper_path}")

    spec = importlib.util.spec_from_file_location("ollama_wrapper_iut", str(path))
    if spec is None or spec.loader is None:
        raise RuntimeError(f"Impossible de charger le wrapper: {wrapper_path}")

    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)

    if not hasattr(module, "OllamaWrapper"):
        raise RuntimeError("Le wrapper ne contient pas la classe OllamaWrapper")

    wrapper = module.OllamaWrapper(base_url=host, timeout_s=timeout_s)
    result = wrapper.generate_text(model=model, prompt=prompt)
    response = getattr(result, "response", None)
    if not isinstance(response, str):
        raise RuntimeError("Réponse invalide du wrapper Ollama")
    return response


def main() -> int:
    parser = argparse.ArgumentParser(description="Génère une proposition de mise à jour de doc via Ollama.")
    parser.add_argument("--diff", required=True, help="Chemin vers un fichier diff (git diff).")
    parser.add_argument("--docs-dir", default="documentations", help="Dossier docs markdown.")
    parser.add_argument("--output", default="documentations/ia-suggestions.md", help="Fichier de sortie markdown.")
    parser.add_argument("--model", default=os.getenv("OLLAMA_MODEL", "llama3.1"), help="Modèle Ollama")
    parser.add_argument("--host", default=os.getenv("OLLAMA_HOST", "http://10.22.28.190:11434"), help="URL Ollama")
    parser.add_argument("--use-wrapper", action="store_true", help="Utiliser tools/ollama_wrapper_iut.py")
    parser.add_argument("--timeout", type=float, default=float(os.getenv("OLLAMA_TIMEOUT_S", "240")), help="Timeout réseau en secondes")
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

    # Prompt principal: impose un format de sortie actionnable pour la doc.
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
        {diff[:12000]}
        ---

        Extraits docs actuels:
        ---
        {docs_snapshot[:8000]}
        ---
        """
    ).strip()

    selected_model = args.model
    try:
        # Selectionne automatiquement un modele de generation si celui demande est indisponible.
        available_models = list_models(args.host)
        fallback = pick_generation_model(selected_model, available_models)
        if fallback != selected_model:
            print(f"[ai-doc] Modèle '{selected_model}' introuvable, fallback auto -> '{fallback}'")
            selected_model = fallback
    except Exception:
        available_models = []

    try:
        # Par defaut, le projet passe par le wrapper IUT (OLLAMA_USE_WRAPPER=1).
        if args.use_wrapper or os.getenv("OLLAMA_USE_WRAPPER", "1") == "1":
            response = call_ollama_with_iut_wrapper(selected_model, prompt, args.host, args.timeout)
        else:
            response = call_ollama(selected_model, prompt, args.host, args.timeout)
    except Exception as exc:
        extra = ""
        if available_models:
            extra = "\n\nModèles disponibles: " + ", ".join(available_models)
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text(
            "# Suggestions IA (Ollama)\n\n"
            f"Erreur d'appel Ollama: {exc}\n\n"
            "Vérifie que tu es sur le Wi-Fi IUT et que OLLAMA_HOST/OLLAMA_WRAPPER_PATH sont corrects."
            + extra
            + "\n",
            encoding="utf-8",
        )
        print(f"[ai-doc] Erreur Ollama, rapport d'erreur écrit: {output_path}")
        return 1

    # Sortie finale: suggestions Markdown relues ensuite par un humain.
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("# Suggestions IA (Ollama)\n\n" + response + "\n", encoding="utf-8")
    print(f"[ai-doc] Rapport généré: {output_path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
