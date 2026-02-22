#! /bin/sh
set -eu
# Auteur : gl43
# Version initiale : 08/01/2026
#
# Script de test lexical "énergétiquement responsable".
#
# Objectif :
#   - Relancer les tests lexicaux uniquement si nécessaire
#   - Éviter les calculs inutiles en CI
#
PASSED=0
FAILED=0
cd "$(dirname "$0")"/../../.. || exit 1
PATH=./src/test/script/launchers:"$PATH"
mkdir -p src/test/script/report

cat > src/test/script/report/lex.json <<EOF
{
  "passed": 0,
  "failed": 0
}
EOF


# On récupère ce qui a été changé depuis le dernier commit, pour décider si il faut relancer des test
# Rmq: On récupère ceci depuis le HEAD, donc ça ne va marché que si le dernier commit a été un push 
# si un merge request est demandé on doit cherché au SHA du dernier commit dans la branche main
# mais à ce moment j'assume que personne n'a sa branche à lui donc je fais directement avec HEAD~1
# Fichiers nouveaux (non trackés)
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
CHANGED_FILES=$(echo "$CHANGED_FILES" | grep -v '/\.antlr/')


echo "Fichiers modifies :"
echo "$CHANGED_FILES"
echo

run_all=false


is_only_comment_or_blank () {
    f="$1"

    # fichier supprimé → changement fonctionnel
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
        src/main/antlr4/fr/ensimag/deca/syntax/DecaLexer.g4|src/main/java/fr/ensimag/deca/syntax/*)
            if ! is_only_comment_or_blank "$f"; then
                echo "Changement fonctionnel detecte dans $f"
                run_all=true
                break
            else
                echo "Changement non fonctionnel (commentaires) dans $f"
            fi
            ;;
    esac
done


# Liste des tests .deca modifiés
MODIFIED_TESTS=$(echo "$CHANGED_FILES" \
    | grep '^src/test/deca/lex/.*\.deca$' || true)

#################### Cas 1 : Pas de changements pertinentsnul part ##############################
if [ "$run_all" = false ] && [ -z "$MODIFIED_TESTS" ]; then
    echo "Aucun changement lexical détecté : tests lexicaux ignorés."
cat > src/test/script/report/lex.json <<EOF
{
"passed": 0,
"failed": 0
}
EOF
./src/test/script/make_badge.sh lex 0 0

exit 0

fi

#################### Cas 2 : Changement dans l'analysuer syntaxiques (fichiers java ou DecaLexer.g4) ##############################

if [ "$run_all" = true ]; then
    #Important!!!!! SI un fichier est supprimé , il est "changed" donc on relance tout
    echo "Relance de TOUS les tests lexicaux."
    ./src/test/script/not_basic-lex.sh
    PASSED=$((PASSED + $(cat src/test/script/report/lex.json | jq '.passed')))
    FAILED=$((FAILED + $(cat src/test/script/report/lex.json | jq '.failed')))

    echo "Passed: $PASSED"
    echo "Failed: $FAILED"

    cat > src/test/script/report/lex.json <<EOF
{
  "passed": $PASSED,
  "failed": $FAILED
}
EOF
    ./src/test/script/make_badge.sh lex "$PASSED" "$FAILED"
    exit 0

    
fi

#################### Cas 3 : On a cangé que des fichier .deca et pas des fichier de l'analyseur lexical, dans ce cas on ne test que les fichiers deca changés ##############################
echo "Relance des tests lexicaux modifies :"
echo "$MODIFIED_TESTS"
echo



########## Test Invalides ################
test_lex_invalide () {
    output=$(test_lex "$1" 2>&1 || true)
    if echo "$output" | grep -q -e "$1:[0-9][0-9]*:"; then
        echo "Echec attendu pour test_lex sur $1."
        echo "Erreur lexicale produite :"
        echo "$output" | grep "$1:[0-9][0-9]*:"
        echo
        PASSED=$((PASSED+1))
        echo
    else
        echo "Succes inattendu pour test_lex sur $1."
        echo
        FAILED=$((FAILED+1))
        #exit 1
    fi
}

############ Test Valides ################
test_lex_valide () {
    output=$(test_lex "$1" 2>&1 || true)
    if echo "$output" | grep -q -e "$1:[0-9][0-9]*:"; then
        echo "Echec inattendu pour test_lex sur $1."
        echo "Sortie test_lex :"
        echo "$output" | grep "$1:[0-9][0-9]*:"
        echo
        FAILED=$((FAILED+1))
        #exit 1
    else
        echo "Succes attendu pour test_lex sur $1."
        PASSED=$((PASSED+1))
        echo
    fi

}


for f in $MODIFIED_TESTS
do
    if is_deleted_file "$f"; then
        echo "Fichier supprime, test ignore : $f"
        echo
        continue
    fi

    if is_new_file "$f"; then
        echo "Nouveau fichier, test lance : $f"
        # on teste quand même
    elif is_only_comment_or_blank "$f"; then
        echo "Seulement des commentaires modifies, test ignore : $f"
        continue
    fi

    case "$f" in */invalid/*)
        test_lex_invalide "$f"
        ;;
    *)
        test_lex_valide "$f"
        ;;
    esac
done


echo "Tests lexicaux modifies : OK"



cat > src/test/script/report/lex.json <<EOF
{
  "passed": $PASSED,
  "failed": $FAILED
}
EOF
./src/test/script/make_badge.sh lex $PASSED $FAILED

if [ "$FAILED" -gt 0 ]; then
    echo "Des tests lexicaux ont échoué ($FAILED échec(s))"
    exit 1
else
    echo "Tous les tests lexicaux ont réussi"
    exit 0
fi
