#!/usr/bin/env bash
set -euo pipefail

MODE="Default"
GENCODE_OPTS=""
if [ "$#" -ge 1 ] && [ "$1" = "-o" ]; then
    MODE="Opt"
    GENCODE_OPTS="-o"
    shift
fi

echo "Mode de test : $MODE"
echo

# Absolute path to project root (directory containing this script)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"

TEST_DIR="$ROOT/src/test/deca/codegen/valid/metamorphic"
DECAC="$ROOT/src/main/bin/decac"

if [ ! -d "$TEST_DIR" ]; then
  echo "[ERROR] Test directory not found:"
  echo "        $TEST_DIR"
  exit 1
fi


PASSED=0
FAILED=0

cd "$TEST_DIR"

# For each *_1.deca, find its *_2.deca pair, compile both, run ima, compare outputs
for f1 in *_1.deca; do
  [ -e "$f1" ] || continue

  prefix="${f1%_1.deca}"
  f2="${prefix}_2.deca"

  if [ ! -f "$f2" ]; then
    echo "[SKIP] Missing pair: $f2"
    continue
  fi

  echo "== $prefix =="

  # Compile
  "$DECAC" $GENCODE_OPTS "$f1" >/dev/null
  "$DECAC" $GENCODE_OPTS "$f2" >/dev/null


  ass1="${prefix}_1.ass"
  ass2="${prefix}_2.ass"

  if [ ! -f "$ass1" ] || [ ! -f "$ass2" ]; then
    echo "[FAIL] .ass not generated for $prefix"
    FAILED=$((FAILED + 1))
    continue
  fi

  # Run ima
  out1="$(ima "$ass1")"
  out2="$(ima "$ass2")"

  # Compare outputs
  if [ "$out1" = "$out2" ]; then
    echo "[OK] outputs identical"
    PASSED=$((PASSED + 1))
  else
    echo "[KO] outputs differ"
    echo "----- ${prefix}_1 output -----"
    printf '%s\n' "$out1"
    echo "----- ${prefix}_2 output -----"
    printf '%s\n' "$out2"
    echo "------------------------------"
    FAILED=$((FAILED + 1))
  fi

  echo
done

echo "Summary: PASSED=$PASSED FAILED=$FAILED"
if [ "$FAILED" -ne 0 ]; then
  exit 1
fi
