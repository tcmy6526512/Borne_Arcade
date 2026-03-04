# Automatisation documentation avec Ollama

Objectif: proposer des mises à jour de documentation à partir des changements de code, sans imposer un merge automatique.

## Principe

- La doc reste **Docs-as-Code** dans le dépôt.
- L'IA propose un patch (fichier de suggestions), un humain valide.
- Aucun merge direct sans relecture.

## Pré-requis

- Python 3 installé
- Ollama installé et lancé (`ollama serve`)
- Un modèle disponible (exemple):

```bash
ollama pull llama3.1
```

## Générer une proposition de doc

Depuis la racine du dépôt:

```bash
chmod +x scripts/docs-ai.sh
bash scripts/docs-ai.sh
```

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

- `OLLAMA_HOST` (défaut: `http://127.0.0.1:11434`)
- `OLLAMA_MODEL` (défaut: `llama3.1`)

Exemple:

```bash
OLLAMA_MODEL=mistral OLLAMA_HOST=http://127.0.0.1:11434 bash scripts/docs-ai.sh
```

## Workflow conseillé

1. Développer une feature.
2. Lancer `bash scripts/docs-ai.sh`.
3. Appliquer/adapter les suggestions dans les pages de `documentations/`.
4. Générer la doc (`bash scripts/docs-build.sh`) et vérifier.
5. Commit du code + doc.
