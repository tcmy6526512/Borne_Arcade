# Script de diaporama (moins de 5 min)

## Diapo 1 - Idee generale (30 s)

A dire:
- "On a un pipeline docs en 2 etapes: IA pour proposer, MkDocs pour publier."
- "L'IA n'ecrit pas directement la doc finale: validation humaine obligatoire."

A afficher:
- `documentations/liens-soutenance.md`

## Diapo 2 - Generation IA des suggestions (1 min)

A dire:
- "Point d'entree: `scripts/docs-ai.sh`."
- "Le script cree un diff Git et lance `tools/ai_doc_patch.py`."
- "Sortie: `documentations/ia-suggestions.md`."

A afficher:
- `scripts/docs-ai.sh`

Commande demo:
```bash
bash scripts/docs-ai.sh
```

## Diapo 3 - Prompt et structure de sortie (1 min)

A dire:
- "Dans `tools/ai_doc_patch.py`, le prompt est construit dans le `main` (bloc `prompt = textwrap.dedent(...)`)."
- "Le prompt impose: resume, pages impactees, patchs markdown, checklist humaine."
- "Le fichier final est ecrit avec `output_path.write_text(...)`."

A afficher:
- `tools/ai_doc_patch.py`

## Diapo 4 - Role du wrapper Ollama (45 s)

A dire:
- "`tools/ollama_wrapper_iut.py` encapsule l'appel reseau Ollama."
- "Il centralise la connexion, les erreurs lisibles et la validation JSON."
- "Donc code principal plus simple et plus robuste."

A afficher:
- `tools/ollama_wrapper_iut.py`

## Diapo 5 - Generation du site docs (45 s)

A dire:
- "La publication HTML ne passe pas par Ollama."
- "`scripts/docs-build.sh` installe l'environnement puis execute MkDocs avec `documentations/mkdocs.yml`."
- "Sortie: dossier `site/`."

A afficher:
- `scripts/docs-build.sh`
- `documentations/mkdocs.yml`

Commande demo:
```bash
bash scripts/docs-build.sh
```

## Diapo 6 - Conclusion + DungeonBlitz (45 s)

A dire:
- "Pipeline clair: proposer (IA) -> relire (humain) -> publier (MkDocs)."
- "Le jeu ajoute est `DungeonBlitz`, lance via `DungeonBlitz.sh` et code dans `projet/DungeonBlitz/`."
- "Le systeme est simple a expliquer et serieux car la decision finale reste humaine."

A afficher:
- `DungeonBlitz.sh`
- `projet/DungeonBlitz/DungeonBlitz.java`

## Questions probables (reponses ultra-courtes)

Q: "Ollama genere le site ?"
R: "Non. Ollama propose du texte, MkDocs genere le site HTML."

Q: "Ou est le prompt ?"
R: "Dans `tools/ai_doc_patch.py`, dans le `main`."

Q: "Pourquoi wrapper ?"
R: "Pour fiabiliser l'appel reseau et avoir des erreurs propres."
