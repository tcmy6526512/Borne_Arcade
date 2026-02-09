# Montée de version vers Raspberry Pi OS

Objectif : repartir d'un Raspberry Pi OS moderne (basé Debian) et rendre l'installation reproductible.

## 1) Choix de version

Privilégier une version supportée (LTS côté Debian).

Points à valider :
- Java disponible (OpenJDK 17)
- support X11 / environnement graphique pour MG2D
- audio (ALSA/Pulse) pour la musique de fond

## 2) Installation OS

1. Flasher Raspberry Pi OS avec Raspberry Pi Imager.
2. Créer un utilisateur (ex: `pi` ou un utilisateur dédié `borne`).
3. Activer SSH si besoin.
4. Paramétrer la résolution écran (viser 1280×1024).

## 3) Installation dépendances

```bash
sudo apt-get update
sudo apt-get install -y git openjdk-17-jdk x11-xkb-utils
```

## 4) Installer MG2D et le projet

Voir [Installation](installation.md).

## 5) Démarrage automatique

Commencer par autostart desktop (fichier `.desktop`), puis migrer vers service systemd utilisateur.

## 6) Validation

À valider sur la vraie borne :
- performance (Pi 3)
- plein écran
- input (encodeur clavier)
- audio
