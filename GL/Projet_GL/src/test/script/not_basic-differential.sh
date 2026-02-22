#!/bin/sh
set -eu

PASSED=0
FAILED=0

# Parsing strict de l'option -o
MODE="Default"
GENCODE_OPTS=""
if [ "$#" -ge 1 ] && [ "$1" = "-o" ]; then
    MODE="Opt"
    GENCODE_OPTS="-o"
    shift # on retire -o des arguments
fi

echo "Mode de test : $MODE"
echo


# Arguments restants : registres
if [ "$#" -gt 0 ]; then
    REGS="$@"
else
    REGS=$(seq 4 16)
fi

#################### Validation des registres ####################
for r in $REGS; do
    case "$r" in
        ''|*[!0-9]*)
            echo "ERREUR : registre invalide '$r' (doit être un entier)"
            exit 1
            ;;
    esac

    if [ "$r" -lt 4 ] || [ "$r" -gt 16 ]; then
        echo "ERREUR : registre $r invalide (autorisé : 4 à 16 uniquement)"
        exit 1
    fi
done



cd "$(dirname "$0")"/../../.. || exit 1
PATH=./src/test/script/launchers:"$PATH"

# Registres passés en argument, sinon 4..16


echo "Tests regpressure sur tous les fichiers .deca VALID"
echo "Registres testés : $REGS"
echo

# Fonction de test d'un fichier .deca
run_test_for_file() {
    f="$1"

    if [ ! -f "${f%.deca}.expected" ]; then
        echo "⚠️⚠️⚠️⚠️⚠️Fichier attendu manquant ⚠️⚠️⚠️⚠️⚠️: ${f%.deca}.expected"
        return 0
    fi

    echo ">> Test du fichier $f"

    file_passed=true
    reg_passed=0
    reg_failed=0

    for r in $REGS; do
        echo "  -r $r"

        set +e
        output=$(DECA_REG="$r" test_gencode $GENCODE_OPTS "$f" 2>&1)
        status=$?
        set -e

        rm -f "${f%.deca}.out" 2>/dev/null || true

        if [ "$status" -eq 0 ]; then
            echo "    Succès (-r $r)"
            reg_passed=$((reg_passed+1))
        else
            echo "    Echec (-r $r)"
            echo Erreur gencode produite :
            echo "$output"
            reg_failed=$((reg_failed+1))
 
            file_passed=false
        fi
    done

    echo ">> Résultat $f : $reg_passed/$(echo "$REGS" | wc -w) registres OK"
    echo

    if [ "$file_passed" = true ]; then
        PASSED=$((PASSED+1))
    else
        FAILED=$((FAILED+1))
    fi
}


tmp=$(mktemp)
find src/test/deca/codegen/valid/regpressure -type f -name "*.deca" > "$tmp"

while IFS= read -r f; do
    [ -z "$f" ] && continue
    run_test_for_file "$f"
done < "$tmp"

rm -f "$tmp"

echo "Passed: $PASSED"
echo "Failed: $FAILED"
echo
echo


cat > src/test/script/report/differential.json <<EOF
{
  "files_passed": $PASSED,
  "files_failed": $FAILED,
}
EOF

./src/test/script/make_badge.sh differential "$PASSED" "$FAILED"

[ "$FAILED" -eq 0 ] || exit 1
exit 0
