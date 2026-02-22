#!/bin/sh
set -eu

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


cd "$(dirname "$0")"/../../.. || exit 1
PATH=./src/test/script/launchers:"$PATH"

echo "Tests sur tous les fichiers .deca VALID"
echo


tmp_valid=$(mktemp)
find \src/test/deca/codegen/valid/created \
    \src/test/deca/codegen/valid/oracle \
    \src/test/deca/codegen/valid/optim \
    -name "*.deca" > "$tmp_valid"

while IFS= read -r f; do #IFS il s'agit de IInternal Field Separator (pour ne pas coupé une ligne en plusoeurs morceaux)
    [ -z "$f" ] && continue
    #echo "on teste le fichier : $f"
    echo ">> Lancement du test : $f"


    if [ ! -f "${f%.deca}.expected" ]; then
        echo "⚠️⚠️⚠️⚠️⚠️ Fichier non trouvé ⚠️⚠️⚠️⚠️⚠️"
        echo "Fichier attendu manquant: ${f%.deca}.expected"
        echo

        #FAILED=$((FAILED+1))
        #echo "FAILED=$FAILED"
        continue
    fi

    if test_gencode $GENCODE_OPTS "$f"; then
        echo "Succès attendu pour le fichier valid : $f"
        PASSED=$((PASSED+1))
        echo "PASSED=$PASSED"
    else
        echo "Échec inattendu pour le fichier valid : $f"
        FAILED=$((FAILED+1))
        echo "FAILED=$FAILED"
    fi
done < "$tmp_valid"

rm -f "$tmp_valid"

echo
echo "passed en fin $PASSED"
echo "failed en fin $FAILED"
echo


echo "Tests sur tous les fichiers .INVALID"
echo
echo "Fichiers dans src/test/deca/codegen/invalid :"
ls -l src/test/deca/codegen/invalid
echo

tmp_invalid=$(mktemp)
find src/test/deca/codegen/invalid -name "*.deca" > "$tmp_invalid"

while IFS= read -r f; do
    [ -z "$f" ] && continue
    #echo "on teste le fichier : $f"
    echo ">> Lancement du test : $f"

    if [ ! -f "${f%.deca}.expected" ]; then
        echo "⚠️⚠️⚠️⚠️⚠️ Fichier non trouvé ⚠️⚠️⚠️⚠️⚠️"
        echo "Fichier attendu manquant: ${f%.deca}.expected"
        echo

        #FAILED=$((FAILED+1))
        #echo "FAILED=$FAILED"
        continue
    fi

    if test_gencode $GENCODE_OPTS "$f"; then
        echo "Succès inattendu pour le fichier invalid : $f"
        FAILED=$((FAILED+1))
        echo "FAILED=$FAILED"
    else
        echo "Échec attendu pour le fichier invalid : $f"
        PASSED=$((PASSED+1))
        echo "PASSED=$PASSED"
    fi
done < "$tmp_invalid"

rm -f "$tmp_invalid"
echo

# PERF 

echo "Tests sur tous les fichiers .PERF"
echo
tmp_perf=$(mktemp)
find src/test/deca/codegen/perf -name "*.deca" > "$tmp_perf"
while IFS= read -r f; do
     [ -z "$f" ] && continue
         if [ ! -f "${f%.deca}.expected" ]; then
        echo "⚠️⚠️⚠️⚠️⚠️ Fichier non trouvé ⚠️⚠️⚠️⚠️⚠️"
        echo "Fichier attendu manquant: ${f%.deca}.expected"
        echo

        #FAILED=$((FAILED+1))
        #echo "FAILED=$FAILED"
        continue
   fi
     echo "on teste le fichier : $f"
     if ./src/test/script/launchers/test_gencode "$f"; then
        echo "Succès attendu pour le fichier perf : $f"
        PASSED=$((PASSED+1))
        echo "PASSED=$PASSED"
     else
        echo "Échec inattendu pour le fichier perf : $f"
        FAILED=$((FAILED+1))
        echo "FAILED=$FAILED"
     fi
 done < "$tmp_perf"
 rm -f "$tmp_perf"
 echo


cat > src/test/script/report/gencode.json <<EOF
{
    "passed": $PASSED,
    "failed": $FAILED
}
EOF

./src/test/script/make_badge.sh gencode "$PASSED" "$FAILED"

echo "Tous les tests terminés."

if [ "$FAILED" -gt 0 ]; then
    echo " Des tests codegen ont échoué ($FAILED échec(s))"
    exit 1
else
    echo "Tous les tests codegen ont réussi"
    exit 0
fi
