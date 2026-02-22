#! /bin/sh
set -eu

# Script principal pour lancer tous les tests

MODE="Default"
GENCODE_OPTS=""

if [ "$#" -gt 1 ]; then
    echo "Usage: $0 [-o]" >&2
    exit 1
elif [ "$#" -eq 1 ]; then
    if [ "$1" = "-o" ]; then
        MODE="Opt"
        GENCODE_OPTS="-o"
    else
        echo "Usage: $0 [-o]" >&2
        exit 1
    fi
fi

echo "Lancement des scripts de tests (Mode : $MODE)..."
echo
./src/test/script/test_context_energy.sh 
./src/test/script/test_lex_energy.sh 
./src/test/script/test_syntax_energy.sh 
./src/test/script/test_gencode_energy.sh 
./src/test/script/test_differential_energy.sh 
./src/test/script/run_metamorphic_tests.sh

