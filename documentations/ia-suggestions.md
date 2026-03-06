# Suggestions IA (Ollama)

```markdown
# Changelog Documentation

## 1) Résumé des changements de code
- Ajout de `.gitignore` pour ignorer les dossiers `.venv-docs/`, `site/` et `dist/`

## 2) Pages docs impactées
- **`.gitignore`** : Ajout de règles pour ignorer les dossiers de build et environnements virtuels
- **`ajout-jeu.md`** : Mise à jour des exemples pour refléter les nouvelles règles de `.gitignore`

## 3) Patchs proposés

###  Mise à jour de `.gitignore`
```markdown
+.venv-docs/
+site/
+dist/
```

###  Mise à jour de `ajout-jeu.md`
```markdown
## 1) Créer le dossier de jeu

Créer :

```
projet/NomDuJeu/
```

Le menu s'attend à trouver (au minimum) :
- `projet/NomDuJeu/description.txt`
- `projet/NomDuJeu/bouton.txt`
- `projet/NomDuJeu/photo_small.png`
- `projet/NomDuJeu/highscore` (optionnel)

Note : Les dossiers `site/`, `dist/` et `.venv-docs/` sont désormais ignorés par Git.
```

## 4) Checklist de validation humaine
- Vérifier que `.gitignore` ne bloque pas les dossiers nécessaires au projet
- Valider que les exemples dans `ajout-jeu.md` restent pertinents
- S'assurer que les scripts de déploiement ne sont pas affectés par les nouvelles règles
- Confirmer que les environnements virtuels (`.venv-docs/`) sont bien ignorés
```
