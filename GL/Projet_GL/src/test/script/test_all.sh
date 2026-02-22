#! /bin/sh
set -eu
# Auteur : gl43
#

# se placer à la racine du projet
GL=$(cd "$(dirname "$0")"/../../.. && pwd)
export GL

export PATH="$GL/src/main/bin:$PATH"
export PATH="$GL/src/test/script:$PATH"
export PATH="$GL/src/test/script/launchers:$PATH"
export PATH="$GL/global/bin:$PATH" 2>/dev/null || true

echo "mvn clean + mvn test en cours ..."

# Build + tests unitaires
mvn -q clean test -Djacoco.skip=false


echo "Activation de Jacoco pour les scripts de tests en cours ..."
JACOCO_AGENT_DEFAULT="$HOME/.m2/repository/org/jacoco/org.jacoco.agent/0.8.13/org.jacoco.agent-0.8.13-runtime.jar"

if [ -f "$JACOCO_AGENT_DEFAULT" ]; then
  export JACOCO_AGENT_JAR="$JACOCO_AGENT_DEFAULT"
  export JACOCO_DESTFILE="$GL/target/jacoco.exec"
  echo "Jacoco agent trouvé: $JACOCO_AGENT_JAR"
  echo "Destfile: $JACOCO_DESTFILE"
else
  echo "tests scripts non couverts par Jacoco."
  echo "      Attendu: $JACOCO_AGENT_DEFAULT"
fi

echo "Lancement des scripts de tests ..."

./src/test/script/not_basic-decompile.sh
./src/test/script/run_metamorphic_tests.sh -o
./src/test/script/not_basic-context.sh 
./src/test/script/not_basic-lex.sh 
./src/test/script/not_basic-synt.sh 
./src/test/script/not_basic-gencode.sh -o
./src/test/script/not_basic-differential.sh  -o



echo "Génération en cours du rapport ..."

mvn -q jacoco:report -Djacoco.skip=false

echo "Rapport généré dans target/site/jacoco/index.html"
