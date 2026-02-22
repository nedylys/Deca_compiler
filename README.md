---
output:
  word_document: default
  html_document: default
  pdf_document: default
---
# GL Project - Deca Compiler

**Software Engineering Project — ENSIMAG 2025-2026**

> Compiler for the Deca language, targeting the IMA abstract machine.

---

## 🔴 Pipeline Test Results

The test results are available [here](https://gl2026.pages.ensimag.fr/gr8/gl43/index.html).

---

## Team 43 — Group 8

| Member | Main Role |
|--------|-----------|
| **Fatima-azzahra ARDAN** (FA) | Contextual analysis, Extensions |
| **Mohammed EL ARABI** (MA) | Code generation, Testing, Consolidation |
| **Faical EL GOUIJ** (FG) | Contextual analysis, Extensions, CI/CD |
| **Hamza MOUNTASSIR** (HM) | Lexical and syntax analysis, Full language compilation |
| **Ayoub TOUATI** (AT) | Code generation, Testing, Consolidation |

**Supervisor:** Mr. Xavier NICOLLIN

---

## Project Overview

The Deca compiler supports:
- *Hello World** — Basic language constructs  
- *Without objects** — Primitive types, expressions, control structures  
- *With objects** — Classes, inheritance, methods, cast, instanceof  
- *Extensions** — Optimizations and additional features  

---

## Quick Start

```bash
# Compile a Deca file
./src/main/bin/decac fichier.deca

# Compile with optimizations
./src/main/bin/decac -o fichier.deca

# Run on IMA
ima fichier.ass
```

---

## Repository Structure

```
gl43/
├── src/                    # Compiler source code
│   ├── main/
│   │   ├── antlr4/         # ANTLR grammars (Lexer/Parser)
│   │   ├── bin/            # decac executable
│   │   └── java/           # Compiler Java code
│   └── test/
│       ├── deca/           # Test files (.deca)
│       └── script/         # Automated test scripts
├── docs/                   # Documentation
├── planning/               # Planning and progress tracking
├── examples/               # Example Deca programs
└── tools/                  # Additional tools
```

---

## Documentation

All detailed documentation is available in the [`docs/`](docs/) directory:

| Document | Description |
|----------|-------------|
| [User Manual](docs/Manuel-Utilisateur.pdf) | Complete guide to using the compiler, options, commands, and test scripts |
| [Design Documentation](docs/Documentation-Conception.pdf) | Compiler architecture and implementation |
| [Validation Documentation](docs/Documentation-Validation.pdf) | Testing strategy and results |
| [Extensions Documentation](docs/Documentation-Extensions.pdf) | Implemented extensions and their usage |
| [Energy Analysis](docs/Analyse-Energetique.pdf) | Energy impact of the project |
| [Project Report](docs/Bilan_Projet.pdf) | Team and project management review |

---

## Planning

Planning files are available in [`planning/`](planning/):

| File | Description |
|------|-------------|
| [Planning.pdf](planning/Planning.pdf) | Planned schedule (Gantt chart) |
| [Realisation.pdf](planning/Realisation.pdf) | Progress tracking |

---

## Tests

Test scripts are located in [`src/test/script/`](src/test/script/):

```bash
# Run all tests (energy-efficient)
./src/test/script/test_all_energy.sh

# Tests by phase
./src/test/script/test_lex_energy.sh        # Lexical analysis
./src/test/script/test_synt_energy.sh       # Syntax analysis
./src/test/script/test_context_energy.sh    # Contextual analysis
./src/test/script/test_gencode_energy.sh    # Code generation

# Tests with optimizations
./src/test/script/not_basic-gencode.sh -o
```

> Detailed test behavior is explained in the [User Manual](docs/Manuel-Utilisateur.pdf), section 4.

---

## Optimizations

The implemented optimizations can be enabled using the `-o` option:

```bash
./src/main/bin/decac -o fichier.deca
```

> Optimizations are detailed in the [Extensions Documentation](docs/Documentation-Extensions.pdf), and the corresponding code is located in [`src/main/java/fr/ensimag/deca/`](src/main/java/fr/ensimag/deca/).

---

## Continuous Integration

The project uses **GitLab CI/CD** to automatically run tests on each push.

> The CI configuration is described in the [User Manual](docs/Manuel-Utilisateur.pdf), section 6.

---

## Code Coverage (Jacoco)

```bash
# Run tests and generate the report
./src/test/script/test_all.sh

# View the report
firefox target/site/jacoco/index.html
```

---

## Git Repository

```
git@gitlab.ensimag.fr:gl2026/gr8/gl43.git
```

---

<p align="center">
  <i>Team 43 — GL Project 2025-2026</i>
</p>
