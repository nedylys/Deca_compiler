#! /bin/sh
set -eu
# Auteur : gl43
# Version initiale : 08/01/2026
#
# Script de test contextuel "énergétiquement responsable".
#
# Objectif :
#   - Relancer les tests contextuels uniquement si nécessaire
#   - Éviter les calculs inutiles
#
PASSED=0
FAILED=0
cd "$(dirname "$0")"/../../.. || exit 1
PATH=./src/test/script/launchers:"$PATH"
mkdir -p src/test/script/report

cat > src/test/script/report/context.json <<EOF
{
  "passed": 0,
  "failed": 0
}
EOF


########### Détection des fichiers modifiés #############

# Détermination du diff selon local ou CI
if [ -n "${CI_COMMIT_BEFORE_SHA:-}" ] \
   && [ "$CI_COMMIT_BEFORE_SHA" != "0000000000000000000000000000000000000000" ]; then
    DIFF_RANGE="$CI_COMMIT_BEFORE_SHA $CI_COMMIT_SHA"
else
    DIFF_RANGE="HEAD"
fi

# Fichiers modifiés entre commits
CHANGED_FILES=$(git diff --name-only $DIFF_RANGE)

# Nouveaux fichiers (local uniquement)
NEW_FILES=$(git ls-files --others --exclude-standard)

# Union
CHANGED_FILES=$(printf "%s\n%s\n" "$CHANGED_FILES" "$NEW_FILES" | sort -u)

# Ignorer les fichiers générés par ANTLR
CHANGED_FILES=$(echo "$CHANGED_FILES" | grep -v '/\.antlr/' || true)
echo "Fichiers modifies :"
echo "$CHANGED_FILES"
echo

run_all=false

is_only_comment_or_blank () {
    f="$1"

    # Si le fichier est supprimé (ou n'existe pas), on considère que c'est fonctionnel
    [ ! -f "$f" ] && return 1

    DIFF_CONTENT=$(git diff --unified=0 $DIFF_RANGE -- "$f" \
        | grep '^+' \
        | grep -v '^+++' \
        | sed 's/^+//' \
        | sed 's,//.*,,' \
        | grep -v '^[[:space:]]*$')
    [ -z "$DIFF_CONTENT" ]
}



is_deleted_file () {
    [ ! -f "$1" ]
}

is_new_file () {
    git ls-files --others --exclude-standard -- "$1" | grep -q .
}

#################### Détection des changements critiques ##############################
for f in $CHANGED_FILES; do
    case "$f" in
        src/main/java/fr/ensimag/deca/context/*|\
        src/main/java/fr/ensimag/deca/syntax/*|\
        src/main/antlr4/fr/ensimag/deca/syntax/*.g4|\
        src/main/java/fr/ensimag/deca/tree/*)

            if ! is_only_comment_or_blank "$f"; then
                echo "Changement contextuel fonctionnel detecte dans $f"
                run_all=true
                break
            else
                echo "Changement contextuel non fonctionnel (commentaires/blancs) dans $f"
            fi
            ;;
    esac
done

MODIFIED_TESTS=$(echo "$CHANGED_FILES" \
    | grep '^src/test/deca/context/.*\.deca$' || true)

#################### Cas 1 : Pas de changements pertinents nul part ##############################
if [ "$run_all" = false ] && [ -z "$MODIFIED_TESTS" ]; then
    echo "Aucun changement contextuel detecte : tests ignores."
    cat > src/test/script/report/context.json <<EOF
{
"passed": 0,
"failed": 0
}
EOF
    ./src/test/script/make_badge.sh context 0 0

exit 0
fi

#################### Cas 2 :Changement dans l'analysuer contextuel (fichiers java) ##############################
if [ "$run_all" = true ]; then
    echo "Relance de TOUS les tests contextuels."
    ./src/test/script/not_basic-context.sh
    PASSED=$((PASSED + $(cat src/test/script/report/context.json | jq '.passed')))
    FAILED=$((FAILED + $(cat src/test/script/report/context.json | jq '.failed')))
    echo "Passed: $PASSED"
    echo "Failed: $FAILED"
    echo
    cat > src/test/script/report/context.json <<EOF
{
  "passed": 0,
  "failed": 0
}
EOF
./src/test/script/make_badge.sh context $PASSED $FAILED
exit 0
fi

#################### Cas 3 : On a cangé que des fichier .deca et pas des fichier de l'analyseur contextuel, dans ce cas on ne test que les fichiers deca changés ##############################
echo "Relance des tests contextuels modifiés :"
echo "$MODIFIED_TESTS"
echo


########## Test Invalides ################
test_ctx_invalide () {
    output=$(test_context "$1" 2>&1 || true)
    if echo "$output" | grep -q "$1:[0-9][0-9]*:"; then

        echo "Echec attendu pour test_context sur: $1"
        echo "Erreur contextuelle produite :"
        echo "$output" | grep "$1:[0-9][0-9]*:"
        echo
        PASSED=$((PASSED+1))
        echo
    else
        echo "Succés inattendu pour test_context sur: $1"
        FAILED=$((FAILED+1))
        #exit 1
    fi
}



############ Test Valides ################
test_ctx_valide () {
    output=$(test_context "$1" 2>&1 || true)
    if echo "$output" | grep -q "$1:[0-9][0-9]*:"; then
        echo "Echec inattendu pour test_context sur: $1"
        echo "Erreur contextuelle produite :"
        echo "$output" | grep "$1:[0-9][0-9]*:"
        echo

        FAILED=$((FAILED+1))
        #exit 1
    else
        echo "Succés attendu pour test_context sur: $1"
        PASSED=$((PASSED+1))
        echo
    fi
}



for f in $MODIFIED_TESTS; do
    if is_deleted_file "$f"; then
        echo "Fichier supprimé, test ignoré : $f"
        continue
    fi

    if is_new_file "$f"; then
        echo "Nouveau fichier, test lancé : $f"
    elif is_only_comment_or_blank "$f"; then
        echo "Seulement des commentaires modifiés, test ignoré : $f"
        continue
    fi

    case "$f" in */invalid/*)
        test_ctx_invalide "$f"
        ;;
    *)
        test_ctx_valide "$f"
        ;;
    esac
done

echo "Tests contextuels modifiés : OK"



cat > src/test/script/report/context.json <<EOF
{
  "passed": $PASSED,
  "failed": $FAILED
}
EOF
./src/test/script/make_badge.sh context $PASSED $FAILED

if [ "$FAILED" -gt 0 ]; then
    echo "Des tests contextuels ont échoué ($FAILED échec(s))"
    exit 1
else
    echo "Tous les tests contextuels ont réussi"
    exit 0
fi


