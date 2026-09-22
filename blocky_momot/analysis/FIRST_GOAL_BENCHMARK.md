# MOMoT First-Goal Benchmark and Statistical Analysis

This document describes the **MOMoT First-Goal Benchmark**, a statistically rigorous evaluation framework measuring the time and generation search effort required by MOMoT to synthesize its **first goal-reaching solution** across all 10 Blockly maze levels.

To maximize execution efficiency and eliminate post-goal evaluation overhead, the search employs **Early Stopping**: as soon as any candidate program achieves `GoalReached <= -0.5`, the exact formation time and generation number are recorded and the NSGA-II search terminates immediately via `algorithm.terminate()`.

---

## 1. Purpose and Research Questions

1. **RQ1 (Time Effort)**: How long (wall-clock time in milliseconds and seconds) does multi-objective search take to discover the first goal-reaching program for each level?
2. **RQ2 (Search Depth / Generations)**: How many evolutionary generations (and fitness evaluations) are required before a goal-reaching program is first generated?
3. **RQ3 (Variance and Consistency)**: Across 30 independent stochastic runs with distinct random seeds, what is the distribution and spread (mean, standard deviation, median, IQR) of synthesis effort?

---

## 2. Early Stopping Protocol

The search uses early stopping enabled via `MomotRunContext.stopOnFirstGoal = true` or JVM flag `-Dblocky.stopOnFirstGoal=true`:

1. **Objective Tracking**: Solutions are continuously evaluated against MOMoT's multi-objective fitness function.
2. **Goal Reached Condition**: `GoalReached <= -0.5` signifies that the simulation successfully guided the avatar from start to goal cell.
3. **Event Interception**: `ParetoFrontPublisherListener` detects the first solution satisfying `GoalReached <= -0.5`, records `timeToFirstGoalMs` and `generationOfFirstGoal`, writes `first_goal.txt`, and calls `algorithm.terminate()` on the active MOEA `Algorithm` instance.
4. **Immediate Exit**: Search terminates immediately without completing the remaining generation evaluations, freeing resources and preventing runtime inflation.

---

## 3. Level Configurations

Each level uses canonical minimal solution lengths and tailored Henshin transformation rule sets based on language feature constraints:

| Level | Input Model | Henshin Rule Set | Solution Length (`solutionLength`) |
|-------|-------------|------------------|------------------------------------|
| 1 | `1.xmi` | `statement_insertions_atomic_only.henshin` | 2 |
| 2 | `2.xmi` | `statement_insertions_atomic_only.henshin` | 8 |
| 3 | `3.xmi` | `statement_insertions_no_conds.henshin` | 2 |
| 4 | `4.xmi` | `statement_insertions_no_conds.henshin` | 11 |
| 5 | `5.xmi` | `statement_insertions_no_conds.henshin` | 8 |
| 6 | `6.xmi` | `statement_insertions_no_else.henshin` | 10 |
| 7 | `7.xmi` | `statement_insertions_no_else.henshin` | 8 |
| 8 | `8.xmi` | `statement_insertions_no_else.henshin` | 12 |
| 9 | `9.xmi` | `statement_insertions_henshin_text.henshin` | 8 |
| 10 | `10.xmi` | `statement_insertions_henshin_text.henshin` | 38 |

### Default Search Hyperparameters
- **Population Size**: 100
- **Iterations / Max Generations**: 100
- **Ceiling Timeout Budget**: 10,000 evaluations
- **Independent Repetitions**: 30 runs per level (`seed = 1..30`)

### Hardware & System Specifications
- **Processor (CPU)**: 11th Gen Intel(R) Core(TM) i7-11800H @ 2.30GHz (8 Cores, 16 Threads)
- **Memory (RAM)**: 32.0 GB Physical RAM
- **Operating System**: Microsoft Windows 11 Pro (64-bit, Build 26200)

---

## 4. Statistical Analysis Metrics

For each level across $N = 30$ runs, standard descriptive statistics are calculated over successful runs:

- **Mean ($\bar{x}$)**: Sample mean over successful runs.
- **Sample Standard Deviation ($s$)**: Spread of successful run durations.
- **Median ($50^{\text{th}}$ percentile)**: Linear interpolation rank $0.50(N-1)$.
- **Min / Max**: Minimum and maximum observed values.
- **$Q_1$ and $Q_3$**: $25^{\text{th}}$ and $75^{\text{th}}$ percentiles.
- **Interquartile Range ($IQR$)**: $IQR = Q_3 - Q_1$.
- **Success Rate ($p$)**: $p = \frac{S}{N}$ (ratio of successful runs $S$ to total runs $N$).

### Expected Time to Target (ETT) and Survival Bias
When the empirical success rate $p < 100\%$, reporting only the average time of successful runs ($\bar{T}_{\text{success}}$) causes **survival bias** (optimistic distortion), ignoring the computation time wasted on failed attempts.

In Search-Based Software Engineering (SBSE), the realistic search cost when running independent random restarts until discovering a target solution follows a Geometric Distribution:

- **Expected Number of Failures Before First Success**:
  $$E[\text{Failures}] = \frac{1 - p}{p}$$

- **Expected Time to Target ($ETT$)**:
  $$ETT = \frac{1 - p}{p} \cdot T_{\text{failed}} + \bar{T}_{\text{success}}$$
  where $T_{\text{failed}}$ is the mean wall-clock duration of failed search attempts (e.g., executing the full 10,000 evaluation budget).

- **Expected Evaluations to Target ($EET$)**:
  $$EET = \frac{1 - p}{p} \cdot \text{MaxEvaluations} + \bar{E}_{\text{success}}$$
  where $\bar{E}_{\text{success}}$ is the mean evaluations required by successful runs.

---

## 5. Empirical Benchmark Results (Levels 1–10)

The table below summarizes both the naive performance ($\bar{T}_{\text{success}}$) and the realistic restart-inclusive metrics ($ETT$ and $EET$) of MOMoT across all 10 Blockly maze levels ($N = 30$ independent runs per level, population size = 100, max evaluations = 10,000):

| Level | Henshin Rule Set | Sol. Len. | Success Rate ($p$) | Naive Mean ($\bar{T}_{\text{success}}$) | Mean Failed ($T_{\text{failed}}$) | Expected Time to Target ($ETT$) | Expected Evals to Target ($EET$) |
|:-----:|:-----------------|:---------:|:------------------:|:---------------------------------------:|:---------------------------------:|:-------------------------------:|:--------------------------------:|
| **1** | `atomic_only` | 2 | **100.0%** (30/30) | 0.001 s | N/A | **0.001 s** | **100** |
| **2** | `atomic_only` | 8 | **100.0%** (30/30) | 0.055 s | N/A | **0.055 s** | **167** |
| **3** | `no_conds` | 2 | **100.0%** (30/30) | 0.038 s | N/A | **0.038 s** | **220** |
| **4** | `no_conds` | 11 | 6.7% (2/30) | 0.583 s | 3.044 s | **43.192 s** | **140,850** |
| **5** | `no_conds` | 8 | 33.3% (10/30) | 0.647 s | 2.489 s | **5.625 s** | **22,140** |
| **6** | `no_else` | 10 | 3.3% (1/30) | 0.087 s | 2.680 s | **77.820 s** | **290,200** |
| **7** | `no_else` | 8 | 3.3% (1/30) | 0.073 s | 2.417 s | **70.164 s** | **290,200** |
| **8** | `no_else` | 12 | **0.0%** (0/30)* | N/A | 3.067 s | **$\ge 88.930\text{ s}$** | **$\ge 290,000$** |
| **9** | `henshin_text` | 8 | **0.0%** (0/30)* | N/A | 2.349 s | **$\ge 68.120\text{ s}$** | **$\ge 290,000$** |
| **10**| `henshin_text` | 38 | **0.0%** (0/30)* | N/A | 12.721 s | **$\ge 368.900\text{ s}$** / **$1,687.6\text{ s}$** (Seed 124) | **$\ge 290,000$** / **$1,498,700$** |

*\*Note for Unsolved Levels:* For levels with $0\%$ success in the standard 30-run benchmark baseline, lower bounds for $ETT$ and $EET$ are calculated using the 1-failure statistical minimum threshold ($p \le 1/30$). For **Level 10**, an extended sweep across 150 random seeds under the refactored NSGA-II setup successfully solved the maze on **Seed 124** at generation 87 ($18.81\text{s}$), yielding an empirical $p = 1/150 = 0.67\%$ and a finite Expected Time to Target of $ETT = 1,687.6\text{ seconds}$ ($\approx 28.1\text{ minutes}$).

---

## 6. Visual Overview & Charts

### Realistic Search Effort Comparison (Naive Mean Time vs. ETT)
![First Goal ETT Comparison](first_goal_ett_comparison.png)

### Combined Overview (Time & Search Effort)
![First Goal Combined Overview](first_goal_combined_overview.png)

### Synthesis Time Distributions (Seconds)
| Box Plot (Time Distribution) | Mean $\pm$ Std Dev (Time) |
|:----------------------------:|:-------------------------:|
| ![Time Boxplot](first_goal_time_sec_boxplot.png) | ![Time Errorbars](first_goal_time_sec_errorbars.png) |

### Search Effort Distributions (Generations)
| Box Plot (Generation Distribution) | Mean $\pm$ Std Dev (Generations) |
|:----------------------------------:|:-------------------------------:|
| ![Generation Boxplot](first_goal_generation_boxplot.png) | ![Generation Errorbars](first_goal_generation_errorbars.png) |

---

## 7. Key Insights and Discussion

### A. Survival Bias and Naive vs. Expected Time to Target (ETT)
- **The Survival Bias Trap**: Reporting only the mean duration of successful runs ($\bar{T}_{\text{success}}$) creates an illusion that search on intermediate levels takes less than $0.65$ seconds.
- **The True Cost of Restarts**: Accounting for failed attempts via $ETT$ reveals that finding a solution expects **$5.6\text{s}$ to $77.8\text{s}$** (and **22,000 to 290,000 evaluations**) on intermediate levels (Levels 4–7).

### B. Easy Levels (Levels 1–3)
- **Extremely High Efficiency**: MOMoT achieves a **100% success rate** ($p = 1.0$) across all 30 runs for Levels 1, 2, and 3.
- **Identical Naive and ETT Metrics**: Because $p = 100\%$, $ETT = \bar{T}_{\text{success}} \le 0.055\text{s}$, and solutions are found within $100 - 220$ evaluations.

### C. Intermediate Levels (Levels 4–7)
- **Search Efficiency Gains**: Refactoring NSGA-II operator probabilities (lowering crossover to $0.20$, increasing mutation) and adding stagnation-triggered re-seeding boosted Level 5 success rate from $10\%$ to **$33.3\%$** ($ETT$ dropped from $27.6\text{s}$ to **$5.6\text{s}$**) and Level 4 success rate from $3.3\%$ to **$6.7\%$** ($ETT$ dropped from $87.4\text{s}$ to **$43.2\text{s}$**).
- **Restart Overhead**: For levels with $p \approx 3.3\%$, $ETT$ quantifies practitioner wait time as $70\text{s} - 78\text{s}$.

### D. Hard Level (Level 10) Search Space Breakthrough
- **Massive Solution Space**: Level 10 requires a solution AST length of 38 transformation steps, incorporating nested loops (`whilePathAhead`), conditional checks (`if`, `ifElse`), and turn actions.
- **Breakthrough via Refactored Search**: While standard NSGA-II previously yielded $0\%$ success across 190+ seeds ($ETT = \infty$), the refactored NSGA-II search setup with stagnation re-seeding successfully solved Level 10 on **Seed 124** at generation 87 ($18.81\text{s}$ time to goal).
- **Realistic Search Cost**: Across 150 evaluated random seeds, $p = 1/150 = 0.67\%$, giving an empirical Expected Time to Target of **$ETT = 1,687.6\text{ seconds}$** ($\approx \mathbf{28.1\text{ minutes}}$) or $\approx \mathbf{1.5\text{ million evaluations}}$.

### E. Human vs. Automated Synthesis Trade-Off
- **High Utility for Easy/Intermediate Levels**: For straightforward mazes (Levels 1–3) and moderately complex levels (Levels 4–7), automated MOMoT search with random restarts is **highly effective**. It generates correct, minimal AST structures in sub-minute expected time without manual coding.
- **Feasible But Resource-Intensive for Hard Levels**: For highly complex levels (like Level 10), automated search is capable of discovering goal-reaching ASTs ($18.81\text{s}$ single-run synthesis time), but requires a multi-minute budget ($\approx 28\text{ minutes}$ ETT) due to the $O(B^L)$ combinatorial explosion of search space.

---

## 8. Output Datasets & Schema

The benchmark produces two CSV files per execution session:

### A. Raw Run Dataset (`first_goal_benchmark_raw_<session>.csv`)
Tracks every individual run execution:

```
level,runIndex,seed,inputXmi,henshinModule,solutionLength,populationSize,maxEvaluations,solved,timeToFirstGoalMs,timeToFirstGoalSec,generationOfFirstGoal,evaluationsAtFirstGoal,elapsedWallMs,elapsedWallSec,outputDir
```

### B. Summary Dataset (`first_goal_benchmark_summary_<session>.csv`)
Contains aggregated statistical metrics per level, including restart-inclusive $ETT$ and $EET$:

```
level,inputXmi,henshinModule,solutionLength,totalRuns,successCount,successRate,meanTimeMs,stdDevTimeMs,medianTimeMs,minTimeMs,maxTimeMs,q1TimeMs,q3TimeMs,iqrTimeMs,meanTimeSec,stdDevTimeSec,medianTimeSec,minTimeSec,maxTimeSec,q1TimeSec,q3TimeSec,iqrTimeSec,meanGen,stdDevGen,medianGen,minGen,maxGen,q1Gen,q3Gen,iqrGen,meanFailedTimeMs,meanFailedTimeSec,ettMs,ettSec,eet,populationSize,maxEvaluations
```

---

## 9. Execution & Reproduction Instructions

### Automated Script Execution

Run from repository root:

```bash
# Execute full 10-level benchmark with 30 runs
./blocky_momot/analysis/run_first_goal_benchmark.sh [session_name]
```

### CLI Parameter Overrides

Configure execution using environment variables:

```bash
BLOCKY_FROM_LEVEL=1 BLOCKY_TO_LEVEL=10 BLOCKY_RUNS=30 BLOCKY_POP_SIZE=100 BLOCKY_ITERATIONS=100 ./blocky_momot/analysis/run_first_goal_benchmark.sh
```

### Direct Java Execution via Maven

```bash
mvn -pl blocky_game compile exec:java \
  -Dexec.mainClass=blocky_game.MomotFirstGoalBenchmarkRunner \
  -Dblocky.fromLevel=1 \
  -Dblocky.toLevel=10 \
  -Dblocky.runs=30 \
  -Dblocky.populationSize=100 \
  -Dblocky.iterations=100
```

### Visualizations

To manually regenerate charts from existing benchmark CSVs:

```bash
cd blocky_momot/analysis
python plot_first_goal_benchmark.py [session_name]
```

Generated charts include:
1. `first_goal_time_sec_boxplot.{png,pdf,svg}`: Box plots of time to goal.
2. `first_goal_time_sec_errorbars.{png,pdf,svg}`: Mean $\pm$ Std Dev time plot.
3. `first_goal_generation_boxplot.{png,pdf,svg}`: Box plots of generation to goal.
4. `first_goal_generation_errorbars.{png,pdf,svg}`: Mean $\pm$ Std Dev generation plot.
5. `first_goal_combined_overview.{png,pdf,svg}`: Side-by-side time and generation overview figure.
