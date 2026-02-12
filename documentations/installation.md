# Installation

Cette page te guide pour installer et lancer la borne sur **Ubuntu/Debian** (ta VM) puis sur **Raspberry Pi OS**.

## Prérequis

### Système
- Linux Debian/Ubuntu (VM) ou Raspberry Pi OS (Debian).
- Résolution cible recommandée : **1280×1024 (4:3)** (écran de la borne).

### Paquets

Sur Ubuntu/Debian/Raspberry Pi OS :

```bash
sudo apt-get update
sudo apt-get install -y \
  git \
  openjdk-17-jdk \
  x11-xkb-utils \
  xdotool \
  python3 python3-venv
```

Notes :
- Le dépôt historique utilisait Java 8, mais sur Debian/Raspberry Pi OS récents, **Java 17** est le meilleur compromis (LTS et largement disponible).
- `x11-xkb-utils` fournit `setxkbmap`.

## Récupérer le code

Tu peux cloner ton dépôt Git sur la machine cible (VM ou borne) dans un dossier de travail (ex: `~/git`).

Exemple :
```bash
mkdir -p ~/git
cd ~/git
git clone <URL_DE_TON_DEPOT> borne_arcade
```

## Dépendance MG2D

Le menu Java dépend de la bibliothèque **MG2D** (`import MG2D.*`).

Note : ce dépôt utilise très largement `MG2D.geometrie.Couleur` (NOIR, BLANC, etc.). Si tu clones une version de MG2D qui ne fournit pas cette classe, la compilation échouera.

Mise à jour : la version GitHub de MG2D place généralement `Couleur` dans le package `MG2D` (ex: `MG2D/Couleur.java`). Le code de ce dépôt a été adapté en conséquence (imports `MG2D.Couleur`). Si tu as une erreur de compilation sur `MG2D.geometrie.Couleur`, commence par faire un `git pull` sur `borne_arcade`.

Deux stratégies possibles :
1. **Cloner MG2D à côté du dépôt** (recommandé pour rester proche de l'existant).
2. **Vendre/emballer MG2D** dans le dépôt (si vous voulez un dépôt auto-suffisant).

### Option 1 — Cloner MG2D à côté

```bash
cd ~/git
git clone https://github.com/synave/MG2D.git
```

Tu dois alors avoir :
- `~/git/borne_arcade`
- `~/git/MG2D`

Si `./compilation.sh` échoue avec `cannot find symbol: class Couleur` :
- fais d'abord `git pull` dans `~/git/borne_arcade` (pour récupérer les imports compatibles GitHub)
- vérifie ensuite que `Couleur.java` existe : `find ~/git/MG2D -name Couleur.java`
- si vraiment ta MG2D ne fournit pas `Couleur`, essaye une autre version (ex: miroir GitLab IUT si accessible)

## Installer le layout clavier “borne” (optionnel mais conseillé)

Le script de lancement historique fait :

```bash
setxkbmap borne
```

Ce layout est dans le fichier `borne` à la racine du dépôt. Pour que `setxkbmap borne` marche :

```bash
cd ~/git/borne_arcade
sudo cp ./borne /usr/share/X11/xkb/symbols/borne
sudo chmod 644 /usr/share/X11/xkb/symbols/borne
setxkbmap borne
```

## Compiler et lancer en manuel

### Compilation

Le dépôt fournit `compilation.sh` (compilation du menu + des jeux Java sous `projet/`).

Le script utilise désormais une détection portable de MG2D :
- soit en exportant `MG2D_HOME=/chemin/vers/MG2D`
- soit en plaçant MG2D à côté du dépôt (`../MG2D`)

Puis :

```bash
cd ~/git/borne_arcade
./clean.sh
./compilation.sh
```

### Lancement

```bash
cd ~/git/borne_arcade
./lancerBorne.sh
```

En VM (pour éviter l'extinction), lance plutôt :

```bash
cd ~/git/borne_arcade
NO_SHUTDOWN=1 ./lancerBorne.sh
```

## Lancement automatique au démarrage (desktop)

Le dépôt contient `borne.desktop`, historiquement copié dans `~/.config/autostart/`.

Sur Raspberry Pi OS avec un environnement type LXDE/PIXEL, cette approche fonctionne.

```bash
mkdir -p ~/.config/autostart
cp ~/git/borne_arcade/borne.desktop ~/.config/autostart/
```

Le dépôt fournit un `borne.desktop` qui exécute `lxterminal` et lance `lancerBorne.sh` depuis `"$HOME/git/borne_arcade"`.
Pour une installation plus robuste (restart, logs, mise à jour), voir plutôt [Déploiement & mises à jour](deploiement.md) et le service systemd user.

## Générer la documentation (MkDocs)

La doc est dans `documentations/`. Pour générer un site HTML :

```bash
cd ~/git/borne_arcade
python3 -m venv .venv-docs
source .venv-docs/bin/activate
pip install -r documentations/requirements.txt
mkdocs build -f documentations/mkdocs.yml
```

Le site statique est produit dans `documentations/site/`.

