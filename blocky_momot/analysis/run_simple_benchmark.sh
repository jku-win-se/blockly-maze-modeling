#!/usr/bin/env bash
# Minimum solutionLength benchmark (all 10 Blockly maze levels).
# Defaults: pop=150, 100 iterations, 10 runs (override via BLOCKY_* env vars).
# Run from repository root.

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

SESSION="${1:-paper_simple_$(date +%Y%m%d_%H%M%S)}"
FROM_LEVEL="${BLOCKY_FROM_LEVEL:-1}"
TO_LEVEL="${BLOCKY_TO_LEVEL:-10}"
LOG="$ROOT/blocky_momot/analysis/simple_benchmark_${SESSION}.log"
CP_FILE="$ROOT/blocky_game/target/simple-benchmark.cp"

echo "Session: $SESSION"
echo "Levels:  $FROM_LEVEL..$TO_LEVEL"
echo "Log:     $LOG"

mvn -pl blocky_game,blocky_momot -am compile -q
mvn -pl blocky_game -q dependency:build-classpath -Dmdep.outputFile=target/simple-benchmark.cp -DincludeScope=runtime

cd "$ROOT/blocky_game"

java \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  -cp "target/classes;../blocky_momot/target/classes;../blocky_model/src-gen;$(cat "$CP_FILE")" \
  -Dblocky.benchmarkSession="$SESSION" \
  -Dblocky.fromLevel="$FROM_LEVEL" \
  -Dblocky.toLevel="$TO_LEVEL" \
  blocky_game.MomotSimpleMinSolutionLengthRunner \
  2>&1 | tee "$LOG"

echo "Done. Summary CSV: $ROOT/blocky_momot/analysis/min_solution_length_simple_${SESSION}.csv"

echo "Enriching CSV with MOMoT block counts..."
cd "$ROOT/blocky_game"
mvn -q dependency:build-classpath -Dmdep.outputFile=target/simple-benchmark.cp -DincludeScope=runtime
java \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  -cp "target/classes;../blocky_momot/target/classes;../blocky_model/src-gen;$(cat target/simple-benchmark.cp)" \
  blocky_game.MomotSimpleBenchmarkEnricher "$SESSION"

echo "Regenerating charts..."
cd "$ROOT/blocky_momot/analysis"
python plot_results.py
