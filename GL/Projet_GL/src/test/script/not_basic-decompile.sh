#!/bin/bash
set -eu

PASSED=0
FAILED=0

# Répertoires à tester
DECA_DIRECTORIES=("src/test/deca/codegen/valid/created" "src/test/deca/codegen/valid/oracle/AvecObjet" "src/test/deca/codegen/invalid" "src/test/deca/codegen/perf")

cd "$(dirname "$0")"/../../.. || exit 1
PATH=./src/test/script/launchers:"$PATH"


decompile_file() {
    local DECA="$1"
    
    echo "Test de décompilation pour : $DECA"

    output=$(./src/main/bin/decac -p "$DECA" 2>&1 < /dev/null) 
    # Vérifier si la décompilation dans la console fonctionne
    decompilation_output=$(./src/main/bin/decac -p "$DECA" 2>&1 < /dev/null)  
    # Vérifier si une erreur a été générée
    if echo "$decompilation_output" | grep -q "erreur"; then
        echo "Echec de la décompilation pour : $DECA"
        echo "Erreur de décompilation :"
        echo "$decompilation_output"
        FAILED=$((FAILED+1))
    else
        echo "Décompilation réussie pour : $DECA"
        echo "$decompilation_output"
        PASSED=$((PASSED+1))
    fi
}

for DIR in "${DECA_DIRECTORIES[@]}"; do
    echo "Vérification des fichiers dans le répertoire : $DIR"  
    
    # Vérification de l'existence
    files=$(find "$DIR" -name "*.deca")
    if [ -z "$files" ]; then
        echo "Aucun fichier .deca trouvé dans $DIR"
    fi
    

    for DECA in $files; do
        if [ -f "$DECA" ]; then
            echo "Fichier trouvé : $DECA"  # Affiche chaque fichier trouvé
            decompile_file "$DECA"
        fi
    done
done

# Générer le rapport
cat > src/test/script/report/decompile.json <<EOF
{
  "passed": $PASSED,
  "failed": $FAILED
}
EOF

./src/test/script/make_badge.sh decompile "$PASSED" "$FAILED"


echo "Tous les tests terminés."

if [ "$FAILED" -gt 0 ]; then
    echo "Des tests de décompilation ont échoué ($FAILED échec(s))"
    exit 1
else
    echo "Tous les tests de décompilation ont réussi"
    exit 0
fi
