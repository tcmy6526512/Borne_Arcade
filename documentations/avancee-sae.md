# Avancee du projet SAE (rendu)

CHAMPY Thomas

## Contexte

Objectif de la SAE: remettre en etat la borne arcade du departement, reconstruire la documentation perdue, moderniser le workflow d'installation/deploiement, et ajouter un nouveau jeu.

## 1) Automatiser la generation de la documentation

Realise:
- Documentation structuree dans `documentations/`.
- Generation du site statique via `scripts/docs-build.sh` et configuration `documentations/mkdocs.yml`.
- Previsualisation locale via `scripts/docs-serve.sh`.
- Workflow IA de suggestions documentaires via `scripts/docs-ai.sh` + `tools/ai_doc_patch.py` + `tools/ollama_wrapper_iut.py`.

Pages livrees:
- `documentations/technique.md`
- `documentations/installation.md`
- `documentations/ajout-jeu.md`
- `documentations/utilisateur.md`
- `documentations/deploiement.md`
- `documentations/tests-vm.md`
- `documentations/upgrade-raspi-os.md`
- `documentations/validation-borne-reelle.md`

## 2) Test de la procedure sur borne d'arcade

Realise:
- Procedure de recette detaillee redigee dans `documentations/validation-borne-reelle.md`.
- Etapes operationnelles: installation, verification service, pull + redeploiement auto, controle logs.

Etat:
- Procedure prète.
- Validation physique finale a tracer sur la borne (date, operateur, commit, statut).

## 3) Montee de version Raspberry Pi OS et dependances

Realise:
- Plan de migration redige dans `documentations/upgrade-raspi-os.md`.
- Dependances cibles documentees (OpenJDK 17, XKB, Git, etc.).
- Contraintes techniques explicitees (session graphique, audio, resolution, clavier borne).

## 4) Automatisation de l'installation du logiciel sur la borne

Realise:
- Script principal d'installation: `scripts/install-borne.sh`.
- Installation hooks Git: `scripts/install-hooks.sh`.
- Setup service user systemd: `deploy/systemd-user/borne-arcade.service`.
- Activation auto-deploy via flag `.borne-auto-deploy`.

## 5) Automatisation du deploiement via git pull

Realise:
- Script de deploiement local: `scripts/deploy-local.sh`.
- Hooks versionnes:
  - `scripts/git-hooks/post-merge`
  - `scripts/git-hooks/post-checkout`
- Comportement livre: après `git pull`, recompilation + redemarrage du service user si present.

## 6) Ajout d'un nouveau jeu

Realise:
- Jeu ajoute et integre au menu: `DungeonBlitz`.
- Script de lancement: `DungeonBlitz.sh`.
- Sources du jeu: `projet/DungeonBlitz/`.
- Ressources associees (description, boutons, highscore, image) presentes.

Correction importante integree:
- Permission d'execution du launcher corrigée (sinon erreur `Permission denied` au lancement).

## 7) Tests realises

Realise:
- Procedure de tests VM formalisee dans `documentations/tests-vm.md`.
- Tests fonctionnels menu/jeux documentes.
- Procedure de diagnostic d'erreurs documentee.
- Tests sur la borne

## 8) Conclusion

Le projet repond aux points de mission SAE avec une base operationnelle complete:
- documentation reconstruite et automatisable,
- installation/deploiement automatises,
- jeu ajoute et lance depuis le menu,
- protocole de test VM + recette borne reelle formalises,
- archive finale generable.
