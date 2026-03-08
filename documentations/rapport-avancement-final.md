# Rapport d'avancement final (SAE)

Date: 2026-03-06

## Synthese

Le projet couvre les points demandes dans la mission SAE:

- ajout d'un nouveau jeu (`Dungeon Blitz`) integre au menu
- automatisation de la documentation (MkDocs + suggestions IA)
- automatisation installation/deploiement sur la borne
- procedure de test VM et protocole de validation borne reelle
- procedure d'archive finale pour le rendu

## 1) Ajout d'un nouveau jeu

Integration d'un jeu complet `Dungeon Blitz`:

- gameplay top-down (deplacement, attaque, dash, nova)
- ennemis par vagues, progression conditionnee a l'elimination complete
- collisions decor + obstacles
- score et best score persistants
- menu in-game `Jouer / Quitter`

## 2) Installation borne + auto-update apres pull

Elements livres:

- `scripts/install-borne.sh`
- `scripts/install-hooks.sh`
- `scripts/deploy-local.sh`
- `scripts/git-hooks/post-merge`
- `scripts/git-hooks/post-checkout`
- `deploy/systemd-user/borne-arcade.service`

Comportement vise:

- installation one-shot des prerequis
- activation hooks + auto-deploy (`.borne-auto-deploy`)
- redemarrage automatique service apres `git pull`

Recette borne detaillee dans:

- `documentations/validation-borne-reelle.md`

## 3) Documentation, site MkDocs et archive finale

Documentation:

- structure MkDocs centralisee dans `documentations/`
- pages techniques, installation, deploiement, tests VM, upgrade OS
- ajout pages: validation borne reelle + rapport final

Site:

- generation via `scripts/docs-build.sh`
- preview locale via `scripts/docs-serve.sh`

Archive finale:

- script `scripts/archive-final.sh`
- production `dist/borne_arcade_final_<timestamp>.tar.gz`
- checksum `sha256` associe

## Risques residuels / points de controle

- Validation physique finale depend de l'acces a la borne cible.
- En cas de changement d'image OS, verifier XKB (`borne`) et session graphique user.
- Confirmer que le service user demarre bien apres login graphique sur la borne reelle.

## 4) Statut global de la mission

- Documentation technique/installation/utilisateur/ajout jeu: livree
- Tests VM: procedure livree
- Montee de version Raspberry Pi OS: plan de migration livre
- Installation et deploiement automatique: scripts et hooks livres
- Validation borne reelle: checklist prete, execution physique a tracer

## Commandes de cloture

```bash
cd ~/git/borne_arcade
./scripts/docs-build.sh
./scripts/archive-final.sh
```

Livrables attendus:

- site HTML dans `site/`
- archive finale dans `dist/`
