#!/bin/sh
set -eu

PASSED=0 
FAILED=0 

cd "$(dirname "$0")"/../../.. || exit 1
PATH=./src/test/script/launchers:"$PATH"

echo "Tests lexicaux INVALIDES"
tmp_invalid=$(mktemp)
find src/test/deca/lex/invalid -name "*.deca" > "$tmp_invalid"

while IFS= read -r f; do
    [ -z "$f" ] && continue
    echo "on teste le fichier : $f"

    output=$(test_lex "$f" 2>&1 || true) #on met le OR true pour que le script ne s'arrete pas si test_lex echoue
    if echo "$output" | grep -q "$f:[0-9][0-9]*:"; then
        echo "Echec lexical attendu : $f"
        echo "Erreur lexical produite :"
        echo "$output" | grep "$f:[0-9][0-9]*:"
        echo
        PASSED=$((PASSED+1))
    else
        echo "Succes lexical inattendu : $f"
        FAILED=$((FAILED+1))
        rm -f "$tmp_invalid" # Nettoyage du fichier temporaire
        #exit 1
    fi
done < "$tmp_invalid"
rm -f "$tmp_invalid"

echo
echo "Tests lexicaux VALIDES"

tmp_valid=$(mktemp)
find src/test/deca/lex/valid -name "*.deca" > "$tmp_valid"

while IFS= read -r f; do
    [ -z "$f" ] && continue
    echo "on teste le fichier : $f"
    output=$(test_lex "$f" 2>&1 || true)
    if echo "$output" | grep -q "$f:[0-9][0-9]*:"; then
        echo "Echec lexical inattendu : $f"
        echo "Sortie test_lex :"
        echo "$output" | grep "$f:[0-9][0-9]*:"
        echo
        FAILED=$((FAILED+1))
        rm -f "$tmp_valid" # Nettoyage du fichier temporaire
        #exit 1
    else
        echo "Succes lexical attendu : $f"
        PASSED=$((PASSED+1))

    fi
done < "$tmp_valid"
rm -f "$tmp_valid"

cat > src/test/script/report/lex.json <<EOF
{
  "passed": $PASSED,
  "failed": $FAILED
}
EOF
./src/test/script/make_badge.sh lex $PASSED $FAILED

echo "Tous les tests terminés."


if [ "$FAILED" -gt 0 ]; then
    echo "Des tests lexicaux ont échoué ($FAILED échec(s))"
    exit 1
else
    echo "Tous les tests lexicaux ont réussi"
    exit 0
fi
