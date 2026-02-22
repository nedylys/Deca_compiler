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
