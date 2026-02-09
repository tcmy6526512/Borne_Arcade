# Documentation technique

## Vue d'ensemble

Le cœur du projet est le **menu** en Java.

Boucle principale : la classe `Main` instancie `Graphique` puis appelle en boucle `selectionJeu()`.

Le menu :
- ouvre une fenêtre plein écran MG2D (1280×1024)
- liste les dossiers dans `projet/` pour déterminer le nombre de jeux
- affiche un panneau de sélection + une zone image + une zone description
- charge l'image `projet/<jeu>/photo_small.png`
- lit `projet/<jeu>/description.txt` (jusqu'à 10 lignes)
- lit `projet/<jeu>/bouton.txt` (1 ligne, séparateur `:`)
- lit les highscores depuis `projet/<jeu>/highscore` si le fichier existe

## Lancement des jeux

Le lancement est fait par `Pointeur.lancerJeu()` :

- Quand le joueur 1 appuie sur **A**, le menu exécute un process externe :
  - commande : `./<NomDuJeu>.sh`
  - répertoire de travail : la racine du dépôt (celle dans laquelle tourne la JVM)
- Le menu appelle `process.waitFor()` : il attend la fin du jeu avant de reprendre la main.

Conséquence :
- chaque jeu doit avoir un **script shell** à la racine du dépôt
- ce script doit exécuter le jeu et se terminer quand le jeu se termine

## Dépendances

### MG2D

La bibliothèque MG2D fournit :
- Fenêtrage (Fenêtre et plein écran)
- Primitives (Texture, Rectangle, Texte, etc.)
- Audio (bruitages)

Le dépôt ne contient pas MG2D : il faut la fournir à l'exécution et à la compilation.

### Audio

Le menu lit des musiques de fond dans `sound/bg/`.

## Scripts importants

### `lancerBorne.sh`

- applique le layout clavier : `setxkbmap borne`
- nettoie + compile
- lance `java -cp ... Main`
- après fermeture, nettoie et éteint la machine

### `compilation.sh`

- compile les `.java` du menu
- compile chaque jeu Java présent dans `projet/*` (un niveau)

Les scripts `compilation.sh` / `lancerBorne.sh` détectent maintenant MG2D via `scripts/env.sh`.

## Layout clavier

Le fichier `borne` est un layout XKB.
Une fois installé dans `/usr/share/X11/xkb/symbols/borne`, la commande `setxkbmap borne` active le mapping.
