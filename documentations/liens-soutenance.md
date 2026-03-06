# Liens soutenance (utilises dans ce depot)

## Docs + MkDocs + archive finale

- Generation de la doc: `scripts/docs-build.sh`
- Preview local de la doc: `scripts/docs-serve.sh`
- Configuration MkDocs: `documentations/mkdocs.yml`
- Archive finale: `scripts/archive-final.sh`

## Workflow IA (Ollama)

- Script de lancement workflow IA doc: `scripts/docs-ai.sh`
- Code metier qui construit le prompt et appelle Ollama: `tools/ai_doc_patch.py`
- Wrapper reseau Ollama (IUT): `tools/ollama_wrapper_iut.py`
- Page de doc du workflow IA: `documentations/automation-doc-ollama.md`

## Auto-install / auto-deploy

- Installation borne: `scripts/install-borne.sh`
- Installation hooks Git: `scripts/install-hooks.sh`
- Deploiement apres pull: `scripts/deploy-local.sh`
- Hooks Git: `scripts/git-hooks/post-merge`, `scripts/git-hooks/post-checkout`
- Service user systemd: `deploy/systemd-user/borne-arcade.service`

## Validation / recette

- Tests VM Ubuntu: `documentations/tests-vm.md`
- Validation borne reelle: `documentations/validation-borne-reelle.md`
- Rapport final: `documentations/rapport-avancement-final.md`