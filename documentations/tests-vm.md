# Tests sur VM Ubuntu (VirtualBox)

Objectif : reproduire un environnement “proche borne” pour itérer vite.

## 1) Préparer la VM

Recommandations VirtualBox :
- Activer l'accélération 3D si disponible.
- Donner 2 CPU et 2–4 Go RAM.
- Installer un Ubuntu Desktop (ou Debian + environnement graphique).

## 2) Fixer la résolution (1280×1024)

La borne cible est 4:3 en 1280×1024. Dans la VM :
- Mets la fenêtre VirtualBox en 1280×1024 si possible.
- Sinon, configure un mode d'affichage 1280×1024 via les “Guest Additions”.

Le but : vérifier que l'UI ne déborde pas.

## 3) Installer les dépendances

Dans la VM :

```bash
sudo apt-get update
sudo apt-get install -y git openjdk-17-jdk x11-xkb-utils
```

## 4) Cloner le projet et MG2D

```bash
mkdir -p ~/git
cd ~/git
git clone <URL_DE_TON_DEPOT> borne_arcade
git clone https://github.com/synave/MG2D.git
```

Si la compilation échoue avec `MG2D.geometrie.Couleur` manquant :
- fais d'abord `git pull` dans `~/git/borne_arcade`
- puis relance `./compilation.sh`

En dernier recours (si ta version MG2D ne fournit pas `Couleur`), tu peux essayer le miroir GitLab IUT :
```bash
rm -rf ~/git/MG2D
git clone http://iut.univ-littoral.fr/gitlab/synave/MG2D.git
```

## 5) Installer le layout clavier “borne”

```bash
cd ~/git/borne_arcade
sudo cp ./borne /usr/share/X11/xkb/symbols/borne
setxkbmap borne
```

Test : lance `TestClavierBorneArcade` une fois compilé, et vérifie que les touches déclenchent bien les pastilles.

## 6) Lancer la borne sans éteindre la VM

Le script `lancerBorne.sh` termine par `sudo halt`.

Pour la VM, utilise le flag prévu :

```bash
cd ~/git/borne_arcade
NO_SHUTDOWN=1 ./lancerBorne.sh
```

MG2D est détecté automatiquement si tu as cloné `../MG2D`.
Sinon, force le chemin :

```bash
cd ~/git/borne_arcade
MG2D_HOME=~/git/MG2D NO_SHUTDOWN=1 ./lancerBorne.sh
```

## 7) Test fonctionnel minimal

- Le menu s'ouvre en plein écran.
- J1 Haut/Bas change la sélection.
- La description et l'image changent.
- Bouton A lance un jeu (le script `.sh` s'exécute).
- Retour au menu après fermeture du jeu.
- Bouton Z ouvre la confirmation de sortie.

## 8) Journaliser les erreurs

En VM, lance le menu depuis un terminal pour voir :
- erreurs de fichiers manquants (`photo_small.png`, `description.txt`, `sound/bg/`)
- erreurs MG2D (classpath)
