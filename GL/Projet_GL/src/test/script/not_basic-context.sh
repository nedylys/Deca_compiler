#!/bin/sh
set -eu

PASSED=0
FAILED=0

cd "$(dirname "$0")"/../../.. || exit 1
PATH=./src/test/script/launchers:"$PATH"

echo "Tests contextuels INVALIDES"
tmp_invalid=$(mktemp)
find src/test/deca/context/invalid -name "*.deca" > "$tmp_invalid"

while IFS= read -r f; do
    [ -z "$f" ] && continue
    echo "on teste le fichier : $f"
    output=$(test_context "$f" 2>&1 || true) #on met le OR true pour que le script ne s'arrete pas si test_context echoue

    if echo "$output" | grep -q "$f:[0-9][0-9]*:"; then
        echo "Echec attendu : $f"
        echo "Erreur contextuelle produite :"
        echo "$output" | grep "$f:[0-9][0-9]*:"
        echo
        PASSED=$((PASSED+1))
    else
        echo "Succes inattendu : $f"
        FAILED=$((FAILED+1))
        rm -f "$tmp_invalid"
        #exit 1
    fi
    echo PASSED=$PASSED
done < "$tmp_invalid"
rm -f "$tmp_invalid"

echo
echo "Tests contextuels VALIDES"
echo

tmp_valid=$(mktemp)
find src/test/deca/context/valid -name "*.deca" > "$tmp_valid"

while IFS= read -r f; do
    [ -z "$f" ] && continue
    echo "on teste le fichier : $f"

    output=$(test_context "$f" 2>&1 || true) #on met le OR true pour que le script ne s'arrete pas si test_context echoue

    if echo "$output" | grep -q "$f:[0-9][0-9]*:"; then
        echo "Echec inattendu : $f"
        echo "Erreur contextuelle produite :"
        echo "$output" | grep "$f:[0-9][0-9]*:"
        echo
        FAILED=$((FAILED+1))
        rm -f "$tmp_valid"
        #exit 1
    else
        echo "Succes attendu : $f"
        PASSED=$((PASSED+1))
        
    fi
    echo PASSED=$PASSED
done < "$tmp_valid"
rm -f "$tmp_valid"

cat > src/test/script/report/context.json <<EOF
{
  "passed": $PASSED,
  "failed": $FAILED
}
EOF
./src/test/script/make_badge.sh context "$PASSED" "$FAILED"

echo "Tous les tests terminés."

if [ "$FAILED" -gt 0 ]; then
    echo "Des tests contextuels ont échoué ($FAILED échec(s))"
    exit 1
else
    echo "Tous les tests contextuels ont réussi"
    exit 0
fi

