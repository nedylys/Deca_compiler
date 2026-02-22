---
output:
  word_document: default
  html_document: default
  pdf_document: default
---
# Projet GL - Compilateur Deca

**Projet Génie Logiciel — ENSIMAG 2025-2026**

> Compilateur pour le langage Deca, ciblant la machine abstraite IMA.

---

## 🔴 Résultats des Tests du Pipeline

Les résultats des tests sont disponibles [ici](https://gl2026.pages.ensimag.fr/gr8/gl43/index.html).
---

## Équipe 43 — Groupe 8

| Membre | Rôle principal |
|--------|----------------|
| **Fatima-azzahra ARDAN** (FA) | Analyse contextuelle, Extensions |
| **Mohammed EL ARABI** (MA) | Génération de code, Tests, consolidation |
| **Faical EL GOUIJ** (FG) | Analyse contextuelle, Extensions, CI/CD |
| **Hamza MOUNTASSIR** (HM) | Analyse lexicosyntaxique, Compilation langage complet |
| **Ayoub TOUATI** (AT) | Génération de code, Tests, Consolidation |

**Tuteur :** M. Xavier NICOLLIN

---

## Présentation du Projet

Le compilateur Deca prend en charge :
- *Hello World** — Constructions élémentaires du langage
- *Sans objet** — Types primitifs, expressions, structures de contrôle
- *Avec objet** — Classes, héritage, méthodes, cast, instanceof
- *Extensions** — Optimisations et fonctionnalités supplémentaires

---

## Utilisation Rapide

```bash
# Compilation d'un fichier Deca
./src/main/bin/decac fichier.deca

# Compilation avec optimisations
./src/main/bin/decac -o fichier.deca

# Exécution sur IMA
ima fichier.ass
```

---

## Structure du Dépôt

```
gl43/
├── src/                    # Code source du compilateur
│   ├── main/
│   │   ├── antlr4/         # Grammaires ANTLR (Lexer/Parser)
│   │   ├── bin/            # Exécutable decac
│   │   └── java/           # Code Java du compilateur
│   └── test/
│       ├── deca/           # Fichiers de tests (.deca)
│       └── script/         # Scripts de tests automatisés
├── docs/                   # Documentation
├── planning/               # Planning et réalisation
├── examples/               # Exemples de programmes Deca
└── tools/                  # Outils annexes
```

---

## Documentation

Toute la documentation détaillée se trouve dans le répertoire [`docs/`](docs/) :

| Document | Description |
|----------|-------------|
| [Manuel Utilisateur](docs/Manuel-Utilisateur.pdf) | Guide complet d'utilisation du compilateur, options, commandes et scripts de tests |
| [Documentation de Conception](docs/Documentation-Conception.pdf) | Architecture et implémentation du compilateur |
| [Documentation de Validation](docs/Documentation-Validation.pdf) | Stratégie et résultats des tests |
| [Documentation des Extensions](docs/Documentation-Extensions.pdf) | Extensions implémentées et leur utilisation |
| [Analyse Énergétique](docs/Analyse-Energetique.pdf) | Impact énergétique du projet |
| [Bilan du Projet](docs/Bilan_Projet.pdf) | Bilan de gestion d'équipe et de projet |

---

## Planning

Les fichiers de planning sont disponibles dans [`planning/`](planning/) :

| Fichier | Description |
|---------|-------------|
| [Planning.pdf](planning/Planning.pdf) | Planning prévisionnel (Gantt) |
| [Realisation.pdf](planning/Realisation.pdf) | Suivi de réalisation |

---

## Tests

Les scripts de tests sont dans [`src/test/script/`](src/test/script/) :

```bash
# Lancer tous les tests (énergétiquement responsables)
./src/test/script/test_all_energy.sh

# Tests par phase
./src/test/script/test_lex_energy.sh        # Analyse lexicale
./src/test/script/test_synt_energy.sh       # Analyse syntaxique
./src/test/script/test_context_energy.sh    # Analyse contextuelle
./src/test/script/test_gencode_energy.sh    # Génération de code

# Tests avec optimisations
./src/test/script/not_basic-gencode.sh -o
```

> Le fonctionnement détaillé des tests est expliqué dans le [Manuel Utilisateur](docs/Manuel-Utilisateur.pdf), section 4.

---

## Optimisations

Les optimisations implémentées sont activables avec l'option `-o` :

```bash
./src/main/bin/decac -o fichier.deca
```

> Les optimisations sont détaillées dans la [Documentation des Extensions](docs/Documentation-Extensions.pdf) et le code correspondant se trouve dans [`src/main/java/fr/ensimag/deca/`](src/main/java/fr/ensimag/deca/).

---

## Intégration Continue

Le projet utilise **GitLab CI/CD** pour exécuter automatiquement les tests à chaque push.

> La configuration CI est décrite dans le [Manuel Utilisateur](docs/Manuel-Utilisateur.pdf), section 6.

---

## Couverture de Code (Jacoco)

```bash
# Lancer les tests et générer le rapport
./src/test/script/test_all.sh

# Visualiser le rapport
firefox target/site/jacoco/index.html
```

---

## Dépôt Git

```
git@gitlab.ensimag.fr:gl2026/gr8/gl43.git
```

---

<p align="center">
  <i>Équipe 43 — Projet GL 2025-2026</i>
</p>
