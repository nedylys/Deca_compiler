#!/bin/sh
set -eu
################# Important ##################"##
# Voir le fait que je suis peu informé en SVG je me suis juste inspiré du projet CEP
# et je me suis servis de l'IA pour écrire ce fichier et qlq autres détails pour la CI (dans le yaml et le DOCKERFILE)
NAME=$1
PASSED=$2
FAILED=$3

COLOR="#4c1"   # vert
[ "$FAILED" -ne 0 ] && COLOR="#e05d44"  # rouge

mkdir -p src/test/script/report

cat > src/test/script/report/$NAME.svg <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="180" height="20">
  <linearGradient id="a" x2="0" y2="100%">
    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
    <stop offset="1" stop-opacity=".1"/>
  </linearGradient>
  <rect rx="3" width="180" height="20" fill="#555"/>
  <rect rx="3" x="70" width="110" height="20" fill="$COLOR"/>
  <path fill="$COLOR" d="M70 0h4v20h-4z"/>
  <rect rx="3" width="180" height="20" fill="url(#a)"/>
  <g fill="#fff" text-anchor="middle"
     font-family="Verdana,Geneva,DejaVu Sans,sans-serif"
     font-size="11">
    <text x="35" y="15">$NAME</text>
    <text x="125" y="15">$PASSED passed / $FAILED failed</text>
  </g>
</svg>
EOF
