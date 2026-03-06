# Rapport d'avancement final (Lot 4)

Date: 2026-03-06

## Synthese

Le projet est finalise sur les axes SAE demandes:

- Lot 1: nouveau jeu `Dungeon Blitz` integre et jouable depuis le menu.
- Lot 2: installation borne + auto-update industrialises via scripts + hooks + service user systemd.
- Lot 3: documentation nettoyee, site MkDocs generable, procedure d'archive finale ajoutee.
- Lot 4: present rapport de cloture.

## Lot 1 - Nouveau jeu

Remplacement de `HelloBorne` par un jeu complet `Dungeon Blitz`:

- gameplay top-down (deplacement, attaque, dash, nova)
- ennemis par vagues, progression conditionnee a l'elimination complete
- collisions decor + obstacles
- score et best score persistants
- menu in-game `Jouer / Quitter`

## Lot 2 - Borne reelle (install + auto-update)

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

## Lot 3 - Documentation + site + archive finale

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

## Commandes de cloture

```bash
cd ~/git/borne_arcade
./scripts/docs-build.sh
./scripts/archive-final.sh
```

Livrables attendus:

- site HTML dans `documentations/site/`
- archive finale dans `dist/`
