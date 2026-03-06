1) Automatiser la génération de la documentation (MkDocs) — pas à pas
1.1 Installer les prérequis (dans Debian)

Ouvre un terminal
Exécute:
sudo apt-get update
sudo apt-get install -y python3 python3-venv
1.2 Récupérer le dépôt (dans Debian)

Dans le terminal:
mkdir -p ~/git
cd ~/git
git clone <URL_DE_TON_DEPOT> borne_arcade
cd borne_arcade
1.3 Lancer la génération HTML

Toujours dans ~/git/borne_arcade:
chmod +x [docs-build.sh](http://_vscodecontentref_/0) scripts/docs-serve.sh
bash scripts/docs-build.sh
Résultat attendu:
un site HTML généré dans documentations/site/
Source/config MkDocs: mkdocs.yml
1.4 Lancer le “live preview”

Dans ~/git/borne_arcade:
bash scripts/docs-serve.sh
Ouvre ton navigateur dans Debian:
Va sur http://127.0.0.1:8000
1.5 Si ça casse (dépannage rapide)

Si mkdocs introuvable: vérifie que le script a bien créé le venv .venv-docs et installé requirements.txt
Si Python manque: python3 --version doit répondre
2) Tester sur ta VM Debian 12 — pas à pas (avec checks)
2.1 Installer les dépendances (Debian)
Dans un terminal Debian:

sudo apt-get update
sudo apt-get install -y git openjdk-17-jdk x11-xkb-utils xdotool
2.2 Cloner MG2D + projet (Debian)
Toujours dans Debian:

mkdir -p ~/git
cd ~/git
git clone <URL_DE_TON_DEPOT> borne_arcade
git clone https://github.com/synave/MG2D.git
Important: MG2D doit être accessible, idéalement ici:

~/git/MG2D
~/git/borne_arcade
C’est exactement ce que la détection de env.sh sait gérer.
2.3 Installer le layout clavier “borne” (Debian)
cd ~/git/borne_arcade
sudo cp ./borne /usr/share/X11/xkb/symbols/borne
sudo chmod 644 /usr/share/X11/xkb/symbols/borne
setxkbmap borne
Check simple:

Lance un petit test clavier (optionnel mais conseillé):
cd ~/git/borne_arcade
compilation.sh
java -cp ".:$(cd ../MG2D && pwd)" TestClavierBorneArcade
Appuie sur les touches (flèches, f/g/h, r/t/y, q/s/d, a/z/e, o/k/l/m) et vérifie que les cercles changent de couleur.
2.4 Lancer la borne sans éteindre la VM
cd ~/git/borne_arcade
chmod +x *.sh scripts/*.sh
NO_SHUTDOWN=1 ./lancerBorne.sh
Checks “borne OK”:

Le menu s’affiche (plein écran / presque)
Navigation J1 Haut/Bas fonctionne
La description change (lit description.txt)
L’image s’affiche (lit photo_small.png)
Appui sur A (J1) lance un jeu et revient au menu
Z (J1) ouvre la confirmation de sortie
Le jeu “DungeonBlitz” apparaît et se lance (script DungeonBlitz.sh, dossier DungeonBlitz)
2.5 Erreurs fréquentes
“MG2D introuvable”: vérifie que MG2D est bien cloné en ~/git/MG2D. Sinon force:
MG2D_HOME=~/git/MG2D NO_SHUTDOWN=1 ./lancerBorne.sh
Problème d’affichage: revois “Guest Additions” + résolution
Un jeu non-Java ne se compile pas: normal, compilation.sh skip les dossiers sans .java
3) Déploiement sur la vraie borne + auto-update après git pull — pas à pas
3.1 Pré-requis côté borne
Raspberry Pi OS installé + session graphique fonctionnelle (indispensable pour MG2D)
Réseau OK
Git + Java 17
3.2 Installer dépendances + cloner (sur la borne)
Sur la borne:

sudo apt-get update
sudo apt-get install -y git openjdk-17-jdk x11-xkb-utils xdotool
Puis:

mkdir -p ~/git
cd ~/git
git clone <URL_DE_TON_DEPOT> borne_arcade
git clone https://github.com/synave/MG2D.git
cd borne_arcade
3.3 Installation automatisée (hooks + XKB + service)
Sur la borne, dans ~/git/borne_arcade:

chmod +x *.sh scripts/*.sh scripts/git-hooks/*
.[install-borne.sh](http://_vscodecontentref_/13) --apt
Ce script:

installe les hooks Git (post-merge / post-checkout)
active l’auto-deploy (fichier .borne-auto-deploy)
installe le layout XKB borne
installe/active le service systemd utilisateur borne-arcade.service
3.4 Démarrer le menu (borne)
systemctl --user start borne-arcade.service
Pour voir l’état:
systemctl --user status borne-arcade.service
3.5 Auto-update après git pull (borne)
Dans ~/git/borne_arcade:
git pull
Attendu:
hook Git déclenche deploy-local.sh
ça compile puis redémarre borne-arcade.service (si le service est présent)
Point important:

Les “systemd user services” démarrent quand l’utilisateur a une session. Sur une borne, c’est généralement OK car il y a auto-login graphique.
Si tu veux un démarrage même sans login (option avancée): loginctl enable-linger <ton_user>
Suite du travail (grandes étapes + comment faire)
A) Mettre en place une vraie stratégie de tests

Objectif: éviter de “tester à la main” à chaque fois.
À faire:
Smoke test compilation: compilation.sh doit passer sur Debian + sur Pi OS
Smoke test lancement: script qui lance le menu 5–10s puis quitte (ou lance DungeonBlitz)
Tests unitaires Java (si vous choisissez JUnit): migration vers un build tool (Maven/Gradle) ou harness minimal
Si tu veux, je peux te proposer une approche “minimale et réaliste” compatible MG2D (sans refactor massif).
B) Montée de version Raspberry Pi OS + mises à jour dépendances

Objectif: installation propre, maintenue, reproductible.
À faire:
installer Raspberry Pi OS récent
revalider: vidéo, plein écran, audio, input
vérifier jeux non-Java: Python (requirements), Lua, etc. (au cas par cas)
C) Automatisation installation (propre)

Objectif: en 1 commande, la borne est prête.
À faire:
durcir install-borne.sh (vérifs MG2D, messages d’erreur, éventuellement clonage MG2D si absent)
standardiser le dossier d’installation (ex: ~/git/borne_arcade)
D) Automatisation déploiement via Git (compléter les cas)

Objectif: “pull = update” même si git pull --rebase, ou si on change de branche.
À faire:
ajouter (si besoin) d’autres hooks (ex: post-rewrite) ou imposer une stratégie merge sur la borne
ajouter un petit log de déploiement
E) Ajout d’un vrai nouveau jeu

DungeonBlitz est un exemple technique déjà intégré.
Pour un “vrai” jeu:
créer projet/NouveauJeu/ + description.txt, bouton.txt, photo_small.png
créer NouveauJeu.sh à la racine
définir ses dépendances (Python venv, etc.) et documenter
Dis-moi quel environnement de bureau tu as sur Debian 12 (XFCE, GNOME, LXDE) et si ta VM a déjà l’affichage 1280×1024 : je te donne ensuite une checklist “validation complète” (avec exactement quoi cliquer et quoi lancer) jusqu’à obtenir un menu stable.