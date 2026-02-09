# Ajouter un nouveau jeu

Le menu détecte automatiquement les jeux en listant les dossiers dans `projet/`.
Le lancement, lui, se fait via un script à la racine : `./<NomDuJeu>.sh`.

Donc pour ajouter un jeu **NomDuJeu** :

## 1) Créer le dossier de jeu

Créer :

```
projet/NomDuJeu/
```

Le menu s'attend à trouver (au minimum) :
- `projet/NomDuJeu/description.txt` (jusqu'à 10 lignes affichées)
- `projet/NomDuJeu/bouton.txt` (une seule ligne, format ci-dessous)
- `projet/NomDuJeu/photo_small.png` (image affichée dans le menu)
- `projet/NomDuJeu/highscore` (optionnel ; si absent, le menu affiche `/`)

### Format `bouton.txt`

Une ligne avec 7 champs séparés par `:` :

```
<texte_joystick>:<btn1>:<btn2>:<btn3>:<btn4>:<btn5>:<btn6>
```

Exemple :
```
Déplacer:Tirer:Sauter:Dash:Menu:Pause:Quitter
```

## 2) Ajouter le script de lancement

Créer à la racine du dépôt :

```
NomDuJeu.sh
```

Exemple réel dans ce dépôt
- Un jeu de test a été ajouté : `HelloBorne`.
- Dossier : `projet/HelloBorne/`
- Script : `HelloBorne.sh`

Ce script doit :
- se lancer depuis la racine (car le menu fait `Runtime.exec("./NomDuJeu.sh")`)
- démarrer le jeu (Java/Python/Lua/…)
- **bloquer** jusqu'à la fin du jeu (sinon le menu reprend la main pendant que le jeu tourne)

### Exemple (jeu Java)

Si le jeu Java a une classe `Main` dans `projet/NomDuJeu/` :

```bash
#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"

source ./scripts/env.sh

# Exemple: exécuter un Main.java compilé dans projet/NomDuJeu
java -cp ".:projet/NomDuJeu:${MG2D_HOME}" Main
```

Tu adapteras la classpath pour MG2D selon l'installation (voir [Déploiement & mises à jour](deploiement.md)).

## 3) Ajouter la compilation (si nécessaire)

Si ton jeu est en Java et que tu veux qu'il soit compilé par `compilation.sh`, il doit être dans `projet/NomDuJeu/` et contenir ses `.java`.
Le script compile tous les dossiers sous `projet/`.

## 4) Vérifier dans le menu

Relance la borne, puis :
- le jeu doit apparaître dans la liste (nom = nom du dossier)
- la description, l'image et les contrôles doivent s'afficher
- le lancement doit fonctionner
