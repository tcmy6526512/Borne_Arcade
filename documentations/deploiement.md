# Déploiement & mises à jour (objectif “git pull → installé”)

Objectif : sur la borne, après un `git pull`, les mises à jour doivent être appliquées automatiquement (recompilation, redémarrage du menu, etc.).

## Stratégie proposée (simple et robuste)

1. Installer la borne “comme un service” (systemd) qui lance le menu.
2. Installer un **hook Git `post-merge`** (et `post-checkout`) qui lance un script de déploiement.
3. Le script :
   - nettoie
   - recompile
   - redémarre le service

Pourquoi cette approche ?
- Le hook se déclenche **à chaque pull** (post-merge) sans dépendre d'outils externes.
- Systemd standardise le démarrage et le redémarrage.

## Point d'attention : session graphique

Le menu utilise une fenêtre plein écran : il faut une session X/Wayland.
Selon la version de Raspberry Pi OS, le démarrage graphique peut être géré par LightDM (X11) ou autre.

Deux options :
- **Autostart desktop** (facile, proche de l'existant) : `~/.config/autostart/borne.desktop`.
- **Systemd user service** (propre) : service utilisateur qui démarre après login graphique.

Dans cette SAE, commencez par “autostart desktop” pour valider rapidement, puis migrez vers systemd user.

Ce dépôt fournit maintenant une installation systemd user directement (plus simple à maintenir qu'un .desktop hardcodé).

## Installation sur la borne (commande)

Hypothèse : le dépôt est cloné sur la borne (idéalement dans `~/git/borne_arcade`) et MG2D est présent.

1) Rendre les scripts exécutables :

```bash
cd ~/git/borne_arcade
chmod +x *.sh scripts/*.sh scripts/git-hooks/*
```

2) Installer (paquets + hooks + XKB + service) :

```bash
cd ~/git/borne_arcade
./scripts/install-borne.sh --apt
```

3) Démarrer le menu :

```bash
systemctl --user start borne-arcade.service
```

## Auto-update après `git pull`

Avec :
- hooks installés (`scripts/install-hooks.sh`)
- auto-deploy activé (`touch .borne-auto-deploy`)

Alors un `git pull` déclenche automatiquement :
- `scripts/deploy-local.sh` → `clean.sh` + `compilation.sh`
- puis redémarrage `systemctl --user restart borne-arcade.service` (si service présent)

## Hooks Git

Les hooks Git ne sont pas versionnés par défaut. La solution standard :
- versionner des hooks dans `scripts/git-hooks/`
- et les copier dans `.git/hooks/` via un script d'installation.

Hooks recommandés :
- `post-merge`
- `post-checkout`

Ils appellent : `./scripts/deploy-local.sh`.

Ce dépôt fournit :
- des hooks versionnés dans `scripts/git-hooks/`
- un installateur `scripts/install-hooks.sh`

Activation de l'auto-déploiement
- Le script de déploiement ne fait rien si le fichier `./.borne-auto-deploy` n'existe pas.
- Sur la borne, crée-le une fois :

```bash
cd /chemin/vers/borne_arcade
touch .borne-auto-deploy
```

## Ce que devra faire le script de déploiement

- Vérifier la présence de MG2D et échouer proprement sinon.
- Compiler menu + jeux.
- (Optionnel) exécuter une suite de tests.
- Redémarrer le menu.

Implémentation actuelle
- `scripts/deploy-local.sh` compile et tente de redémarrer un service systemd utilisateur `borne-arcade.service` si présent.
- Sinon, il s'arrête après compilation.

## À faire ensuite dans le code

Les scripts existants ont des chemins hardcodés vers `/home/pi/git`.
Pour un déploiement propre, on va les refactoriser (étape suivante de la SAE) pour :
- accepter une variable d'environnement `MG2D_HOME`
- ou détecter MG2D à côté du dépôt (ex: `../MG2D`)
- et éviter `sudo halt` lors des tests (VM), en mettant une option.
