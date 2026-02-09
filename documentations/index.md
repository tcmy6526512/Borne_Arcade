# Borne d'arcade — Documentation

Ce dépôt contient le **menu de la borne d'arcade** (Java + bibliothèque MG2D) et un ensemble de jeux (Java, Python, Lua, …) lancés depuis ce menu.

Objectifs du projet (SAE)
- Automatiser la génération de la documentation.
- Tester la procédure sur une machine proche (VM Debian/Ubuntu), puis sur la borne.
- Monter de version vers Raspberry Pi OS (Debian) et mettre à jour les dépendances.
- Automatiser l'installation sur la borne.
- Automatiser le déploiement via Git (après `git pull`, mise à jour appliquée automatiquement).
- Ajouter un nouveau jeu.

Commencer ici
1. Lire [Installation](installation.md) pour installer les dépendances et exécuter le menu.
2. Lire [Tests sur VM Ubuntu](tests-vm.md) pour reproduire un environnement de borne sur VirtualBox.
3. Lire [Déploiement & mises à jour](deploiement.md) pour préparer l'installation “borne” + mises à jour automatiques.

Génération de la documentation
- Les sources sont dans `documentations/`.
- Génération avec MkDocs : voir [Installation](installation.md#generer-la-documentation-mkdocs).
