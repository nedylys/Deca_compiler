#! /bin/sh
set -eu
# Auteur : gl43
# Version : adaptée 18/01/2026
#
# Script de test codegen multi-registres avec option -o (optimisation)
#

# Détection du mode
MODE="Default"
GENCODE_OPTS=""
if [ "$#" -ge 1 ] && [ "$1" = "-o" ]; then
    MODE="Opt"
    GENCODE_OPTS="-o"
    shift
fi

# Registres passés en argument, sinon 4..16
REGS="$@"
[ -z "$REGS" ] && REGS=$(seq 4 16)

for r in $REGS; do
    case "$r" in ''|*[!0-9]*)
        echo "ERREUR : registre invalide '$r'"
        exit 1 ;;
    esac
    if [ "$r" -lt 4 ] || [ "$r" -gt 16 ]; then
        echo "ERREUR : registre $r invalide (autorisé : 4 à 16)"
        exit 1
    fi
done

REG_COUNT=$(echo "$REGS" | wc -w)
echo "Mode : $MODE"
echo "$REG_COUNT registres seront testés : $REGS"

PASSED=0
FAILED=0

# On se place à la racine du projet
cd "$(dirname "$0")"/../../.. || exit 1
PATH=./src/test/script/launchers:"$PATH"
mkdir -p src/test/script/report

cat > src/test/script/report/differential.json <<EOF
{
  "passed": 0,
  "failed": 0
}
EOF

#################### Détection des fichiers modifiés ####################
if [ -n "${CI_COMMIT_BEFORE_SHA:-}" ] && [ "$CI_COMMIT_BEFORE_SHA" != "0000000000000000000000000000000000000000" ]; then
    DIFF_RANGE="$CI_COMMIT_BEFORE_SHA $CI_COMMIT_SHA"
else
    DIFF_RANGE="HEAD"
fi

CHANGED_FILES=$(git diff --name-only $DIFF_RANGE)
NEW_FILES=$(git ls-files --others --exclude-standard)
CHANGED_FILES=$(printf "%s\n%s\n" "$CHANGED_FILES" "$NEW_FILES" | sort -u)
CHANGED_FILES=$(echo "$CHANGED_FILES" | grep -v '/\.antlr/' || true)

run_all=false

is_only_comment_or_blank() {
    f="$1"
    DIFF_CONTENT=$(git diff --unified=0 $DIFF_RANGE -- "$f" \
        | grep '^+' \
        | grep -v '^+++' \
        | sed 's/^+//' \
        | sed 's,//.*,,' \
        | grep -v '^[[:space:]]*$')
    [ -z "$DIFF_CONTENT" ]
}

is_deleted_file() { [ ! -f "$1" ]; }
is_new_file() { git ls-files --others --exclude-standard -- "$1" | grep -q .; }

is_new_deca_file() {
    f="$1"
    case "$f" in *.deca) is_new_file "$f" ;; *) return 1 ;; esac
}

list_contains() {
    list="$1"; item="$2"
    set +e
    printf "%s\n" "$list" | grep -Fxq "$item"
    rc=$?; set -e
    return $rc
}

# Détection des changements critiques
for f in $CHANGED_FILES; do
    case "$f" in
        src/main/java/fr/ensimag/deca/codegen/*|\
        src/main/java/fr/ensimag/deca/context/*|\
        src/main/java/fr/ensimag/deca/syntax/*|\
        src/main/antlr4/fr/ensimag/deca/syntax/*.g4|\
        src/main/java/fr/ensimag/deca/tree/*)
            if ! is_only_comment_or_blank "$f"; then
                echo "Changement codegen fonctionnel détecté dans $f"
                run_all=true
                break
            else
                echo "Changement non fonctionnel (commentaires/blancs) dans $f"
            fi
            ;;
    esac
done

# Tests modifiés
# Ne garder que les fichiers modifiés dans regpressure
MODIFIED_DECA_TESTS=$(echo "$CHANGED_FILES" | grep '^src/test/deca/codegen/valid/regpressure/.*\.deca$' || true)
MODIFIED_EXPECTED_TESTS=$(echo "$CHANGED_FILES" | grep '^src/test/deca/codegen/valid/regpressure/.*\.expected$' || true)
MODIFIED_FROM_EXPECTED=$(echo "$MODIFIED_EXPECTED_TESTS" | sed 's/\.expected$/.deca/')
MODIFIED_TESTS=$(printf "%s\n%s\n" "$MODIFIED_DECA_TESTS" "$MODIFIED_FROM_EXPECTED" | grep -v '^[[:space:]]*$' | sort -u)


#################### Cas 1 : pas de changements pertinents ####################
if [ "$run_all" = false ] && [ -z "$MODIFIED_TESTS" ]; then
    echo "Aucun changement codegen détecté : tests multi-registres ignorés."
    cat > src/test/script/report/differential.json <<EOF
{
  "passed": $PASSED,
  "failed": $FAILED
}
EOF
    ./src/test/script/make_badge.sh differential "$PASSED" "$FAILED"
    exit 0
fi

#################### Fonction de test ####################
run_test_for_file() {
    f="$1"

    if [ ! -f "${f%.deca}.expected" ]; then
        echo "⚠️ Fichier attendu manquant: ${f%.deca}.expected"
        return 0
    fi


    echo ">> Test du fichier $f avec les registres : $REGS"
    file_passed=true
    reg_passed=0
    reg_failed=0

    for r in $REGS; do
        set +e
        output=$(test_gencode -r "$r" $GENCODE_OPTS "$f" 2>&1)
        status=$?
        set -e

        if [ "$status" -eq 0 ]; then
            echo "    OK (-r $r)"
            reg_passed=$((reg_passed+1))
        else
            echo "    Not OK (-r $r)"
            echo "$output"
            reg_failed=$((reg_failed+1))
            file_passed=false
        fi
        rm -f "${f%.deca}.out"
    done


    echo ">> Résultat $f : $reg_passed/$REG_COUNT registres OK"
    echo

    if [ "$file_passed" = true ]; then
        PASSED=$((PASSED+1))
    else
        FAILED=$((FAILED+1))
    fi
}

#################### Cas 2 : changement complet ####################
if [ "$run_all" = true ]; then
    echo "Relance de tous les tests multi-registres en mode $MODE"
    ./src/test/script/not_basic-differential.sh $GENCODE_OPTS $REGS
    exit $?
fi

#################### Cas 3 : seulement tests modifiés ####################
if [ "$run_all" = false ]; then
    echo "Relance des tests multi-registres pour les fichiers modifiés uniquement :"
    for f in $MODIFIED_TESTS; do
        case "$f" in *.deca) ;; *) continue ;; esac
        if is_deleted_file "$f"; then echo "Fichier supprimé, ignoré : $f"; continue; fi
        if is_new_deca_file "$f"; then echo "Nouveau fichier .deca, test lancé : $f"; fi
        run_test_for_file "$f"
    done
fi

#################### Résumé ####################
echo "Passed: $PASSED"
echo "Failed: $FAILED"
cat > src/test/script/report/differential.json <<EOF
{
  "passed": $PASSED,
  "failed": $FAILED
}
EOF
./src/test/script/make_badge.sh differential "$PASSED" "$FAILED"

if [ "$FAILED" -gt 0 ]; then
    echo "Des tests multi-registres ont échoué ($FAILED échec(s))"
    exit 1
else
    echo "Tous les tests multi-registres ont réussi"
    exit 0
fi
