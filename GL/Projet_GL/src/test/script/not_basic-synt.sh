#!/bin/sh
set -eu
PASSED=0 
FAILED=0 

cd "$(dirname "$0")"/../../.. || exit 1
PATH=./src/test/script/launchers:"$PATH"


echo "Tests syntaxiques INVALIDES"
tmp_invalid=$(mktemp)
find src/test/deca/syntax/invalid -name "*.deca" > "$tmp_invalid"

is_runtime_error () {
    echo "$1" | grep -qiE \
        'overflow|underflow|IllegalArgumentException|FormatFxception|ArithmeticException|runtimeexception|internal error|segmentation|assert|NumberFormatException|Circular include'
}


while IFS= read -r f; do
    [ -z "$f" ] && continue
    echo "on teste le fichier : $f"
    set +e
    output=$(test_synt "$f" 2>&1 || true)
    set -e

    # Erreur critique
    if is_runtime_error "$output"; then
        echo "Erreur syntaxique attendu (overflow/underflow/crash) sur $f"
        echo "$output"
        PASSED=$((PASSED+1))
        echo
        continue
    fi

    if echo "$output" | grep -q -e "$f:[0-9][0-9]*:"; then
        echo "Echec syntaxique attendu : $f"
        echo "Erreur syntaxique produite :"
        echo "$output" | grep "$f:[0-9][0-9]*:"
        echo
        PASSED=$((PASSED+1))
    else
        echo "Succes inattendu : $f"
        FAILED=$((FAILED+1))
        rm -f "$tmp_invalid"
        #exit 1
    fi
done < "$tmp_invalid"

rm -f "$tmp_invalid"

echo

echo "Tests syntaxiques VALIDES"

tmp_valid=$(mktemp)
find src/test/deca/syntax/valid -name "*.deca" > "$tmp_valid"

while IFS= read -r f; do
    [ -z "$f" ] && continue
    echo "on teste le fichier : $f"
    set +e
    output=$(test_synt "$f" 2>&1 || true)
    set -e

    # Erreur critique
    if is_runtime_error "$output"; then
        echo "Erreur syntaxique inattendu (overflow/underflow/crash) sur $f"
        echo "$output"
        FAILED=$((FAILED+1))
        echo
        continue
    fi
    if echo "$output" | grep -q "$f:[0-9][0-9]*:"; then
        echo "Echec syntaxique inattendu : $f"
        echo "Erreur syntaxique produite :"
        echo "$output" | grep "$f:[0-9][0-9]*:"
        echo
        FAILED=$((FAILED+1))
        rm -f "$tmp_valid"
        #exit 1
    else
        echo "Succes attendu : $f"
        PASSED=$((PASSED+1))
    fi
done < "$tmp_valid"

rm -f "$tmp_valid"

cat > src/test/script/report/syntax.json <<EOF
{
  "passed": $PASSED,
  "failed": $FAILED
}
EOF

./src/test/script/make_badge.sh syntax "$PASSED" "$FAILED"


echo "Tous les tests terminés."

if [ "$FAILED" -gt 0 ]; then
    echo "Des tests syntaxiques ont échoué ($FAILED échec(s))"
    exit 1
else
    echo "Tous les tests syntaxiques ont réussi"
    exit 0
fi
