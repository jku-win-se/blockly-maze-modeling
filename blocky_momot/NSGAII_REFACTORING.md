# MOMoT NSGA-II Search Setup Refactoring

This document details the architectural and algorithmic refactoring performed on the NSGA-II multi-objective search setup in MOMoT for the **Blocky Maze** synthesis problem.

---

## 1. Motivation and Problem Diagnosis

Prior benchmark evaluations revealed that standard NSGA-II settings suffered from low success rates on complex maze levels (e.g., Level 4–10). Diagnostic analysis identified three root causes:

1. **High Crossover Disruption**: Standard single-point crossover (`OnePointCrossover`) operating at high probability ($0.75 - 0.85$) destructively split structured block sequences, corrupting valid control structures and partial paths.
2. **Fitness Traps and Local Optima**: In complex mazes with walls and dead ends, the Manhattan distance metric (`closestToGoal`) creates deceptive local optima where candidate solutions get stuck in local minima.
3. **Distance Evaluation Discrepancy**: During model copying/simulation, cell distance annotations were missing in unannotated grid clones, causing `closestToGoal` evaluations to default to high penalty values ($100,000.0$) rather than actual BFS shortest-path distances.

---

## 2. Refactoring Interventions

### A. Operator Probability Rebalancing (`blocky_custom.java` & `blocky.momot`)
The evolutionary operator rates were rebalanced to favor exploratory mutation and minimize sequence fragmentation:

- **Crossover Rate (`OnePointCrossover`)**: Reduced from `0.85` / `0.75` to **`0.20`**.
- **Placeholder Mutation Rate (`TransformationPlaceholderMutation`)**: Set to **`0.35`** to encourage structural insertion/deletion of AST nodes.
- **Parameter Mutation Rate (`TransformationParameterMutation`)**: Set to **`0.25`** to tune action directions (`TURN_LEFT`, `TURN_RIGHT`, `MOVE_FORWARD`) and conditions (`CHECK_FORWARD`, `CHECK_LEFT`, `CHECK_RIGHT`).

*Configuration Flexibility*: Operator probabilities can be dynamically overridden via JVM system properties:
- `-Dblocky.crossoverRate=0.20`
- `-Dblocky.placeholderMutationRate=0.35`
- `-Dblocky.parameterMutationRate=0.25`

---

### B. Stagnation-Triggered Population Diversity Injection (`blocky_custom.java`)
To escape local minima and fitness traps, a stagnation listener was integrated into the NSGA-II execution:

- **Progress Tracking**: Tracks the global best `closestToGoal` value in the active population across generations.
- **Stagnation Threshold**: Triggered after **15 consecutive generations** without improvement in `closestToGoal`.
- **Diversity Re-seeding**: When triggered, the population is sorted by objective quality, and the worst **20%** (`reseedRatio = 0.2`) of candidate solutions are replaced with fresh, randomly generated solutions.
- **Counter Reset**: Resets the stagnation counter upon re-seeding to allow newly injected candidates to propagate through mutation.

---

### C. Distance Evaluation Repair (`BlockySimulator.java`)
Fixed distance computation in `BlockySimulator.closestToGoalOrPenalty`:

- Added explicit `annotateCells(level)` initialization at the start of distance evaluation to ensure BFS distance-to-goal annotations are present on all simulated grid copies.
- Ensures smooth gradient feedback for `closestToGoal` ranging from $0.0$ (goal reached) to the exact tile distance from the avatar's final position.

---

## 3. Impact & Verification

- **Easy Levels (Levels 1–3)**: Maintain 100% success rate with ultra-fast search times ($\le 60\text{ ms}$).
- **Hard Level (Level 10)**: Successfully broke through the $p = 0\%$ search barrier, achieving a goal-reaching AST (38 transformation steps) in 87 generations ($18.81\text{s}$ synthesis time).
