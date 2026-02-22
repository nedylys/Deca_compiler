#! /bin/sh
set -eu
# Auteur : gl43
# Version initiale : 08/01/2026

# Ici je parle français car je m'inspire de basic-lex.sh qui lui est en français
# Script de test syntaxique "énergétiquement responsable".
#
# Ce script a pour but de lancer les tests syntaxiques du compilateur
# uniquement lorsque cela est pertinent, afin d'éviter des calculs
# inutiles dans le cadre de l'intégration continue (CI).
#


# Important !!!!!!!!! : # Les tests unitaires Java sont gérés par Maven (mvn -q test).



# Le principe est le suivant :
#   - analyser les fichiers modifiés depuis le dernier commit
#   - si aucun fichier lié à la syntaxe n'a changé, ne pas lancer les tests
#   - sinon, lancer les tests syntaxiques classiques fournis

PASSED=0
FAILED=0
# On se place dans le répertoire racine du projet,
cd "$(dirname "$0")"/../../.. || exit 1
PATH=./src/test/script/launchers:"$PATH"
mkdir -p src/test/script/report

cat > src/test/script/report/syntax.json <<EOF
{
  "passed": 0,
  "failed": 0
}
EOF



# On récupère ce qui a été changé depuis le dernier commit, pour décider si il faut relancer des test
# Rmq: On récupère ceci depuis le HEAD, donc ça ne va marché que si le dernier commit a été un push 
# si un merge request est demandé on doit cherché au SHA du dernier commit dans la branche main
# mais à ce moment j'assume que personne n'a sa branche à lui donc je fais directement avec HEAD~1
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

# Ignorer fichiers générés ANTLR
CHANGED_FILES=$(echo "$CHANGED_FILES" | grep -v '/\.antlr/' || true)

echo "Fichiers modifies :"
echo "$CHANGED_FILES"
echo

run_all=false

is_only_comment_or_blank () {
    f="$1"
    # fichier supprimé 
    [ ! -f "$f" ] && return 1

    DIFF_CONTENT=$(git diff --unified=0 $DIFF_RANGE -- "$f" \
        | grep '^+' \
        | grep -v '^+++' \
        | sed 's/^+//' \
        | grep -v '^[[:space:]]*//' \
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

# Fichiers ANTLR (DecaParser.g4, DecaLexer.g4 ...) = tout ce qui termine par .g4
for f in $CHANGED_FILES; do
    case "$f" in
        src/main/antlr4/fr/ensimag/deca/syntax/*.g4|\
        src/main/java/fr/ensimag/deca/syntax/*)

            if ! is_only_comment_or_blank "$f"; then
                echo "Changement syntaxique fonctionnel détecté dans $f"
                run_all=true
                break
            else
                echo "Changement syntaxique non fonctionnel (commentaires/blancs) dans $f"
            fi
            ;;
    esac
done



# Liste des tests .deca modifiés
MODIFIED_TESTS=$(echo "$CHANGED_FILES" \
    | grep '^src/test/deca/syntax/.*\.deca$' || true)




#################### Cas 1 : Pas de changements pertinents nul part ##############################
if [ "$run_all" = false ]  && [ -z "$MODIFIED_TESTS" ]; then
    echo "Aucun changement syntaxique détecté : tests syntaxiques ignorés."
    cat > src/test/script/report/syntax.json <<EOF
{
"passed": 0,
"failed": 0
}
EOF
./src/test/script/make_badge.sh syntax 0 0

exit 0
fi


#################### Cas 2 : Changement dans l'analysuer syntaxiques (fichiers java ou .g4) ##############################
if [ "$run_all" = true ];
then
    #Important!!!!! SI un fichier est supprimé , il est "changed" donc on relance tout
    echo "Relance de TOUS les tests syntaxiques."
    echo
    ./src/test/script/not_basic-synt.sh

    PASSED=$((PASSED + $(cat src/test/script/report/syntax.json | jq '.passed')))
    FAILED=$((FAILED + $(cat src/test/script/report/syntax.json | jq '.failed')))

    echo "Passed: $PASSED"
    echo "Failed: $FAILED"
    echo
    cat > src/test/script/report/syntax.json <<EOF
{
  "passed": 0,
  "failed": 0
}
EOF
    ./src/test/script/make_badge.sh syntax "$PASSED" "$FAILED"
    exit 0

    
fi


#################### Cas 3 : On a cangé que des fichier .deca et pas des fichier de l'analyseur syntaxique, dans ce cas on ne test que les fichiers deca changés ##############################
echo "Relance des tests syntaxiques modifiés."
#basic-synt appelle test-synt qui test tout ce qu'il y 'a dans src/test/deca/syntax/ (valid ou invalid)
#./src/test/script/basic-synt.sh
echo "$MODIFIED_TESTS"
echo



################### Test Invalides #####################
test_synt_invalide () {
    # $1 = premier argument.
    echo "on teste le fichier : $1"
    output=$(test_synt "$1" 2>&1 || true)
    if echo "$output" | grep -q -e "$1:[0-9][0-9]*:"
    then
        echo "Echec attendu pour test_synt sur $1."
        echo "Erreur syntaxique produite :"
        echo "$output" | grep "$1:[0-9][0-9]*:"
        echo
        PASSED=$((PASSED+1))
        echo
    else
        echo "Succes inattendu de test_synt sur $1."
        echo
        FAILED=$((FAILED+1))
        #exit 1
    fi
}    


############ Test Valides ################
test_synt_valide () {
    if echo "$output" | grep -q -e "$1:[0-9][0-9]*:"; then
        echo "Echec inattendu pour test_synt sur $1."
        echo "Sortie test_synt :"
        echo "$output" | grep "$1:[0-9][0-9]*:"
        echo
        FAILED=$((FAILED+1))
        echo
        #exit 1
    else
        echo "Succes attendu pour test_synt sur $1."
        PASSED=$((PASSED+1))
        echo
    fi
}

for f in $MODIFIED_TESTS
do
    if is_deleted_file "$f"; then
        echo "Fichier supprimé, test ignoré : $f"
        echo
        continue
    fi

    if is_new_file "$f"; then
        echo "Nouveau fichier, test lancé : $f"
        continue

    elif is_only_comment_or_blank "$f"; then
        echo "Seulement des commentaires modifiés, test ignoré : $f"
        continue
    fi 
    
    case "$f" in */invalid/*)
            test_synt_invalide "$f"
            ;;*)
            test_synt_valide "$f"
            ;;
    esac
done

echo "Tests syntaxiques modifiés : OK"

# test


cat > src/test/script/report/syntax.json <<EOF
{
  "passed": $PASSED,
  "failed": $FAILED
}
EOF

./src/test/script/make_badge.sh syntax $PASSED $FAILED

if [ "$FAILED" -gt 0 ]; then
    echo "Des tests syntaxiques ont échoué ($FAILED échec(s))"
    exit 1
else
    echo "Tous les tests syntaxiques ont réussi"
    exit 0
fi
