#!/bin/sh
set -eu

# Usage: decompile <file.deca>
# Ce script effectue uniquement la décompilation due l'arbre abstrait

DECA=""

# Vérifie qu'un fichier a été passé en argument
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <file.deca>" >&2
    exit 1
fi

DECA="$1"

ASS="${DECA%.deca}.ass"

########## Décompilation ##########
# Lancer la décompilation et afficher le résultat directement dans le terminal
./src/main/bin/decac -p "$DECA" 



