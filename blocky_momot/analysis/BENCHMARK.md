# MOMoT Minimum Solution-Length Benchmark

This document describes the **reproducible empirical benchmark** that measures, for each of the ten Blockly maze levels, how many graph transformation steps (`solutionLength`) MOMoT needs to synthesize a goal-reaching program, and how that compares to the known optimal Blockly block count and the actual block count in the synthesized solution.

The canonical result set is session **`paper_simple_boosted_20260627`**.

---

## What is measured

For maze levels **1–10**:

| Metric | Meaning |
|--------|---------|
| **Optimal block count** | Hand-verified minimum Blockly blocks for a correct solution (reference baseline). |
| **Min. MOMoT `solutionLength`** | Smallest number of allowed Henshin transformation steps under which at least one of the independent MOMoT runs reaches the goal. |
| **MOMoT program block count** | Minimum number of Blockly statements in a **goal-reaching** synthesized model at that `solutionLength` (counts real blocks, not placeholders or deleted steps). |

The gap between **min. `solutionLength`** and **MOMoT program block count** reflects search overhead: placeholder transformations, deletions, and reordering during synthesis.

---

## Search configuration (canonical run)

| Parameter | Value |
|-----------|------:|
| Population size | 150 |
| Iterations per run | 100 |
| Max evaluations per run | 15,000 (= pop × iterations) |
| Independent runs | 10 |
| Random seed for run *i* | *i* (1 … 10), set via `PRNG.setSeed(i)` in `blocky_custom` |
| Success criterion | ≥ 1 of 10 runs reaches the goal (`GoalReached ≤ −1`) |
| Algorithm | NSGA-II (default MOMoT orchestration) |

These defaults live in `MomotSimpleMinSolutionLengthRunner` and can be overridden with JVM system properties (see below).

---

## Prerequisites

1. **JDK 17** (tested with Eclipse Temurin 17).
2. **Maven 3.9+** at the repository root.
3. **Python 3.10+** with `matplotlib` (only for regenerating charts).
4. **Git Bash** on Windows (or any POSIX shell).

Build once:

```bash
cd /path/to/blockly-maze-modeling
mvn -pl blocky_game,blocky_momot -am compile
```

---

## Reproduce the exact benchmark

### 1. Run the benchmark

From the **repository root**:

```bash
bash blocky_momot/analysis/run_simple_benchmark.sh paper_simple_boosted_20260627
```

This script:

1. Compiles `blocky_game` and `blocky_momot`.
2. Builds the runtime classpath via Maven.
3. Runs `MomotSimpleMinSolutionLengthRunner` with **`cwd = blocky_game`** (required for relative Henshin paths).
4. Passes JVM `--add-opens` flags required by the MOMoT instrumentation layer on JDK 17.
5. Writes a log and CSV under `blocky_momot/analysis/`.

**Expected runtime:** roughly 1–3 hours on a modern desktop (level 10 dominates).

### 2. Enrich CSV with MOMoT block counts (if re-running on old output)

New runs record `momotBlockCount` automatically. To derive block counts from saved output directories:

```bash
cd blocky_game
mvn -q dependency:build-classpath -Dmdep.outputFile=target/simple-benchmark.cp -DincludeScope=runtime

java \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  -cp "target/classes;../blocky_momot/target/classes;../blocky_model/src-gen;$(cat target/simple-benchmark.cp)" \
  blocky_game.MomotSimpleBenchmarkEnricher paper_simple_boosted_20260627
```

### 3. Regenerate charts

```bash
cd blocky_momot/analysis
python plot_results.py
pdflatex min_solution_length_chart.tex   # optional TikZ/PGF export
```

Outputs:

- `min_solution_length_chart.png` / `.pdf` / `.svg`
- `min_solution_length_chart.tex` (standalone PGF/TikZ)

---

## Expected results (`paper_simple_boosted_20260627`)

| Level | Optimal blocks | Min. solutionLength | MOMoT block count | Successes at min |
|------:|---------------:|--------------------:|------------------:|-----------------:|
| 1 | 2 | 2 | 2 | 10/10 |
| 2 | 5 | 8 | 8 | 2/10 |
| 3 | 2 | 2 | 2 | 9/10 |
| 4 | 5 | 11 | 8 | 1/10 |
| 5 | 5 | 8 | 8 | 1/10 |
| 6 | 4 | 10 | 9 | 1/10 |
| 7 | 4 | 8 | 6 | 1/10 |
| 8 | 5 | 12 | 11 | 1/10 |
| 9 | 4 | 8 | 5 | 2/10 |
| 10 | 7 | 38 | 10 | 1/10 |

Full machine-readable data:  
`blocky_momot/analysis/min_solution_length_simple_paper_simple_boosted_20260627.csv`

---

## Determinism notes

To obtain **bit-for-bit identical** results:

- Use the **same session id**: `paper_simple_boosted_20260627` (output directories are keyed by session).
- Keep **JDK 17**, dependency versions, and this **git commit** unchanged.
- Do not modify `blocky_momot/model/input/{1..10}.xmi` or Henshin rules under `blocky_model/transformations/`.
- Run with **`cwd = blocky_game`** (handled by `run_simple_benchmark.sh`).

MOMoT sets `PRNG.setSeed(i)` at the start of each run *i*, so the ten runs are deterministic for a fixed codebase. Floating-point order in NSGA-II may still vary across JVM builds; the table above was produced on **Windows 10, JDK 17.0.19, June 2026**.

---

## Advanced overrides

All via JVM `-D` properties passed to `MomotSimpleMinSolutionLengthRunner`:

| Property | Default | Description |
|----------|---------|-------------|
| `blocky.benchmarkSession` | timestamp `_simple` | Output session id (use fixed id for reproduction) |
| `blocky.fromLevel` | 1 | First maze level |
| `blocky.toLevel` | 10 | Last maze level |
| `blocky.populationSize` | 150 | NSGA-II population |
| `blocky.iterations` | 100 | Generations (= maxEval / pop) |
| `blocky.maxEvaluations` | pop × iterations | Override total evaluations |
| `blocky.nrRuns` | 10 | Independent seeded runs per length |

Example — partial re-run of levels 4–10:

```bash
BLOCKY_FROM_LEVEL=4 BLOCKY_TO_LEVEL=10 \
  bash blocky_momot/analysis/run_simple_benchmark.sh my_partial_session
```

---

## Key source files

| File | Role |
|------|------|
| `blocky_game/src/blocky_game/MomotSimpleMinSolutionLengthRunner.java` | Benchmark driver |
| `blocky_game/src/blocky_game/MomotSimpleBenchmarkMetrics.java` | Block counting from MOMoT outputs |
| `blocky_game/src/blocky_game/MomotSimpleBenchmarkEnricher.java` | Post-hoc CSV enrichment |
| `blocky_momot/src/blocky_momot/BlockyProgramMetrics.java` | Statement/block counting in EMF models |
| `blocky_momot/src/blocky_momot_runner/blocky_custom.java` | Seeded runs, population/eval overrides |
| `blocky_momot/analysis/run_simple_benchmark.sh` | One-command reproduction script |
| `blocky_momot/analysis/plot_results.py` | Bar chart generator |

---

## Output layout

Per level and attempted `solutionLength`, MOMoT writes:

```
blocky_game/blocky_momot/output_simple_<session>_lvl<L>_len<LEN>_try<ATTEMPT>/
  objectives_seed_<i>.pf    # per-run Pareto objectives
  models/*.xmi              # synthesized Blockly game models
```

The winning directory for level *L* is recorded in the CSV column `winningOutputDir`.
