# Validation borne réelle

Objectif: valider sur la borne physique l'installation initiale, le lancement automatique, puis l'auto-update apres `git pull`.

## Prérequis

- Borne avec Raspberry Pi OS (session graphique active).
- Depot clone sur la borne (ex: `~/git/borne_arcade`).
- MG2D disponible (ex: `~/git/MG2D`).

## Installation borne (one-shot)

```bash
cd ~/git/borne_arcade
chmod +x *.sh scripts/*.sh scripts/git-hooks/*
./scripts/install-borne.sh --apt
```

## Verification service menu

```bash
systemctl --user daemon-reload
systemctl --user enable borne-arcade.service
systemctl --user restart borne-arcade.service
systemctl --user status borne-arcade.service --no-pager
```

Attendu:
- service `active (running)`
- menu lance en plein ecran

## Validation auto-update apres git pull

Verifier que le flag auto-deploy existe:

```bash
cd ~/git/borne_arcade
test -f .borne-auto-deploy && echo OK || echo NOK
```

Simulation recommandee:

```bash
cd ~/git/borne_arcade
git pull
```

Attendu:
- hook `post-merge` declenche `scripts/deploy-local.sh`
- `clean.sh` + `compilation.sh` executes sans erreur
- `systemctl --user restart borne-arcade.service` effectif

Commande de controle:

```bash
journalctl --user -u borne-arcade.service -n 80 --no-pager
```

## Checklist de recette borne

- [ ] Menu visible en plein ecran apres boot/login
- [ ] Navigation J1 haut/bas OK
- [ ] Bouton A lance un jeu
- [ ] Retour menu apres fermeture jeu
- [ ] Bouton Z sortie menu OK
- [ ] `git pull` applique la MAJ automatiquement
- [ ] Service redemarre automatiquement

## Resultat (a remplir sur la borne)

- Date test borne:
- Operateur:
- Version commit testee:
- Statut final: `OK` / `NOK`
- Notes:
