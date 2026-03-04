# Automatisation documentation avec Ollama

Objectif: proposer des mises à jour de documentation à partir des changements de code, sans imposer un merge automatique.

## Principe

- La doc reste **Docs-as-Code** dans le dépôt.
- L'IA propose un patch (fichier de suggestions), un humain valide.
- Aucun merge direct sans relecture.

## Pré-requis

- Python 3 installé
- Accès réseau au serveur Ollama de l'IUT (Wi-Fi IUT)
- Wrapper présent dans le dépôt: `tools/ollama_wrapper_iut.py`

## Générer une proposition de doc

Depuis la racine du dépôt:

```bash
chmod +x scripts/docs-ai.sh
bash scripts/docs-ai.sh
```

Le script utilise par défaut:

- `OLLAMA_USE_WRAPPER=1`
- `OLLAMA_WRAPPER_PATH=tools/ollama_wrapper_iut.py`
- `OLLAMA_HOST=http://10.22.28.190:11434`

Par défaut, le script compare `HEAD~1...HEAD`.

Pour analyser les changements non commités:

```bash
bash scripts/docs-ai.sh --working-tree
```

## Résultat

Le script écrit:

- `documentations/ia-suggestions.md`

Ce fichier contient:

1. résumé des changements de code,
2. pages docs à mettre à jour,
3. propositions de patch markdown,
4. checklist de validation humaine.

## Variables utiles

- `OLLAMA_HOST` (défaut: `http://10.22.28.190:11434`)
- `OLLAMA_MODEL` (défaut: `llama3.1`)
- `OLLAMA_USE_WRAPPER` (défaut: `1`)
- `OLLAMA_WRAPPER_PATH` (défaut: `tools/ollama_wrapper_iut.py`)

Exemple:

```bash
OLLAMA_MODEL=llama3.1 OLLAMA_HOST=http://10.22.28.190:11434 bash scripts/docs-ai.sh
```

## Diagnostic rapide

- Si tu n'es pas connecté au Wi-Fi IUT: l'appel échoue.
- Le détail de l'erreur est écrit dans `documentations/ia-suggestions.md`.
- Test réseau simple:

```bash
curl -fsS http://10.22.28.190:11434/api/version
```

## Workflow conseillé

1. Développer une feature.
2. Lancer `bash scripts/docs-ai.sh`.
3. Appliquer/adapter les suggestions dans les pages de `documentations/`.
4. Générer la doc (`bash scripts/docs-build.sh`) et vérifier.
5. Commit du code + doc.
