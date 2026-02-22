#! /bin/sh
set -eu
# Auteur : gl43
# Version initiale : 08/01/2026
#
# Script de test codegen "énergétiquement responsable".
#
# Objectif :
#   - Relancer les tests de génération de code uniquement si nécessaire
#   - Éviter les calculs inutiles
#

PASSED=0
FAILED=0

MODE="Default"
GENCODE_OPTS=""

if [ "$#" -eq 0 ]; then
    : # mode par défaut
elif [ "$#" -eq 1 ] && [ "$1" = "-o" ]; then
    MODE="Opt"
    GENCODE_OPTS="-o"
else
    echo "Usage: $0 [-o]" >&2
    exit 1
fi

echo "Mode de test : $MODE"
echo


# On se place à la racine du projet
cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"
mkdir -p src/test/script/report

# Initialisation du fichier .json
cat > src/test/script/report/gencode.json <<EOF
{
  "passed": 0,
  "failed": 0
}
EOF


########### Détection des fichiers modifiés #############
if [ -n "${CI_COMMIT_BEFORE_SHA:-}" ] \
   && [ "$CI_COMMIT_BEFORE_SHA" != "0000000000000000000000000000000000000000" ]; then
    DIFF_RANGE="$CI_COMMIT_BEFORE_SHA $CI_COMMIT_SHA"
else
    DIFF_RANGE="HEAD"
fi

# Fichiers modifiés
CHANGED_FILES=$(git diff --name-only $DIFF_RANGE)

# Nouveaux fichiers local
NEW_FILES=$(git ls-files --others --exclude-standard)

# Union
CHANGED_FILES=$(printf "%s\n%s\n" "$CHANGED_FILES" "$NEW_FILES" | sort -u)

# Ignorer les fichiers ANTLR générés
CHANGED_FILES=$(echo "$CHANGED_FILES" | grep -v '/\.antlr/' || true)
# Les fichiers générés par ANTLR changent trop souvent et n'affectent pas le codegen

echo "Fichiers modifies :"
echo "$CHANGED_FILES"
echo

run_all=false



# Vérifie si les modifications portent uniquement sur des commentaires ou des lignes vides
is_only_comment_or_blank () {
    f="$1"
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

# Test d’appartenance EXACTE dans une liste (une entrée par ligne), compatible set -e
list_contains () {
    list="$1"
    item="$2"
    set +e
    printf "%s\n" "$list" | grep -Fxq "$item"
    rc=$?
    set -e
    return $rc
}

# "New file" uniquement pour les .deca (si on ajoute un expected on ne le considère pas  sans son d)
is_new_deca_file () {
    f="$1"
    case "$f" in
        *.deca) is_new_file "$f" ;;
        *) return 1 ;;
    esac
}


#################### Détection des changements critiques ##############################
for f in $CHANGED_FILES; do
    case "$f" in
        src/main/java/fr/ensimag/deca/codegen/*|\
        src/main/java/fr/ensimag/deca/context/*|\
        src/main/java/fr/ensimag/deca/syntax/*|\
        src/main/antlr4/fr/ensimag/deca/syntax/*.g4|\
        src/main/java/fr/ensimag/deca/tree/*)


            if ! is_only_comment_or_blank "$f"; then
                echo "Changement codegen fonctionnel detecte dans $f"
                run_all=true
                break
            else
                echo "Changement codegen non fonctionnel (commentaires/blancs) dans $f"
            fi
            ;;
    esac
done


################ Tests codegen modifiés #####################
MODIFIED_DECA_TESTS=$(echo "$CHANGED_FILES" \
    | grep '^src/test/deca/codegen/.*\.deca$' || true)

MODIFIED_EXPECTED_TESTS=$(echo "$CHANGED_FILES" \
    | grep '^src/test/deca/codegen/.*\.expected$' || true)

MODIFIED_FROM_DECA="$MODIFIED_DECA_TESTS"

MODIFIED_FROM_EXPECTED=$(echo "$MODIFIED_EXPECTED_TESTS" | sed 's/\.expected$/.deca/')

MODIFIED_TESTS=$(printf "%s\n%s\n" \
    "$MODIFIED_FROM_DECA" \
    "$MODIFIED_FROM_EXPECTED" \
    | grep -v '^[[:space:]]*$' \
    | sort -u)


#################### Cas 1 : Pas de changements pertinents ##############################
if [ "$run_all" = false ] && [ -z "$MODIFIED_TESTS" ]; then
    echo "Aucun changement codegen detecte : tests ignores."
cat > src/test/script/report/gencode.json <<EOF
{
    "passed": $PASSED,
    "failed": $FAILED
}
EOF
./src/test/script/make_badge.sh gencode $PASSED $FAILED

exit 0

fi

#################### Cas 2 : Changement dans le codegen ##############################
if [ "$run_all" = true ]; then
    echo "Relance de TOUS les tests codegen."
    ./src/test/script/not_basic-gencode.sh $GENCODE_OPTS
    PASSED=$((PASSED + $(cat src/test/script/report/gencode.json | jq '.passed')))
    FAILED=$((FAILED + $(cat src/test/script/report/gencode.json | jq '.failed')))

echo "Passed: $PASSED"
echo "Failed: $FAILED"
echo
echo
cat > src/test/script/report/gencode.json <<EOF
{
    "passed": $PASSED,
    "failed": $FAILED
}
EOF
./src/test/script/make_badge.sh gencode "$PASSED" "$FAILED"
exit 0

fi

#################### Cas 3 : Seulement des tests .deca modifies ##############################
echo "Relance des tests codegen modifies :"
echo "$MODIFIED_TESTS"
echo

########## Tests invalides ##########


test_codegen_invalide () {
    if test_gencode $GENCODE_OPTS "$f"; then
        echo "Succes inattendu (invalid) pour test_gencode sur: $1"
        FAILED=$((FAILED+1))
        #exit 1
    else
        echo "Echec attendu (invalid) pour test_gencode sur: $1"
        PASSED=$((PASSED+1))
        echo
    fi
}


########## Tests valides ##########
test_codegen_valide () {

    if test_gencode $GENCODE_OPTS "$f"; then
        echo "Succes attendu (valid) pour test_gencode sur: $1"
        PASSED=$((PASSED+1))
    else
        echo "Echec inattendu (valid) pour test_gencode sur: $1"
        echo "Sortie test_gencode :"
        FAILED=$((FAILED+1))
    fi
}

test_codegen_perf () {

    if test_gencode $GENCODE_OPTS "$f"; then
        echo "Succes attendu (perf) pour test_gencode sur: $1"
        PASSED=$((PASSED+1))
        echo
    else
        echo "Echec inattendu (perf) pour test_gencode sur: $1"
        echo "Sortie test_gencode :"
        FAILED=$((FAILED+1))
        #exit 1
    fi
}



for f in $MODIFIED_TESTS; do
    # Convention : jamais mettre des espaces dans les noms des fichiers de test
    # On ne traite que des .deca (sécurité)
    case "$f" in
        *.deca) : ;;
        *) continue ;;
    esac

    # 1) fichier supprimé
    if is_deleted_file "$f"; then
        echo "Fichier supprime, test ignore : $f"
        continue
    fi

    # 2) new file : uniquement si .deca
    if is_new_deca_file "$f"; then
        echo "Nouveau fichier .deca, test lance : $f"
    else
        # On ne traite que les .deca
        case "$f" in
            *.deca) ;;
            *) continue ;;
        esac

        # Ignorer les commentaires ssi le .deca est  modifié directement et son .expected n'est
        if list_contains "$MODIFIED_FROM_DECA" "$f" \
        && ! list_contains "$MODIFIED_FROM_EXPECTED" "$f"; then
            if is_only_comment_or_blank "$f"; then
                echo "Seulement des commentaires modifies, test ignore : $f"
                continue
            fi
        fi
    fi

    if [ ! -f "${f%.deca}.expected" ]; then
        echo  "⚠️⚠️⚠️⚠️⚠️ Fichier non trouvé ⚠️⚠️⚠️⚠️⚠️"
        echo "Fichier attendu manquant: ${f%.deca}.expected"
        echo
        continue
    fi
    echo ">> Lancement du test : $f"

    case "$f" in
        */invalid/*)
            test_codegen_invalide "$f"
            ;;
        */valid/created/*)
            test_codegen_valide "$f"
            ;;
        */valid/optim/*)
            test_codegen_valide "$f"
            ;;
        */valid/oracle/*)
            test_codegen_valide "$f"
            ;;

        */perf/*)
            test_codegen_perf "$f"
            ;;
        *)
            echo "Type de test inconnu, ignore : $f"
            ;;
    esac
done


echo "Tests codegen modifies : OK"


cat > src/test/script/report/gencode.json <<EOF
{
  "passed": $PASSED,
  "failed": $FAILED
}
EOF
./src/test/script/make_badge.sh gencode $PASSED $FAILED

if [ "$FAILED" -gt 0 ]; then
    echo " Des tests codegen ont échoué ($FAILED échec(s))"
    exit 1
else
    echo " Tous les tests codegen ont réussi"
    exit 0
fi
