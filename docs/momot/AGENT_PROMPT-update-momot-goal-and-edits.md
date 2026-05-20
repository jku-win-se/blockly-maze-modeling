## Goal
Update the MoMoT configuration `blocky_momot/blocky.momot` so the search uses the **statement insertion/deletion/replacement** transformations (generated from `blocky_model/transformations/statement_insertions.henshin_text`) and optimizes **three objectives**:

1. **Closeness to input model’s solution** = **least number of edits** (minimize transformation length)
2. **Reaching GOAL** = program execution ends in `GameStatus.WON`
3. **Shortest path** = among goal-reaching solutions, minimize the number of executed steps / trace length

Keep the config valid, project-relative paths only, and aligned with the repository’s existing conventions in `blocky_momot/blocky.momot`.

---

## Context (repo-specific facts you must respect)

- Input model is loaded from the MoMoT project via a **project-relative** path (current config uses `"model/input/game.xmi"`).
- Blocky metamodel must be registered (current config already registers `BlockyPackage` in `initialization` and on the resource set in `search.model.adapt`).
- Blocky simulation is performed by `blocky_momot.BlockySimulator`.
- The new transformation module (compiled `.henshin`) lives in the **blocky_model** project under:
  - `blocky_model/transformations/statement_insertions_henshin_text.henshin`
  - In `blocky_momot/blocky.momot`, reference it as a relative path from the `blocky_momot` project, e.g. `"../blocky_model/transformations/statement_insertions_henshin_text.henshin"`.

---

## Required changes in `blocky_momot/blocky.momot`

### 1) Use the new Henshin module(s)

In `search.transformations.modules`, include the new module:

- Add: `"../blocky_model/transformations/statement_insertions_henshin_text.henshin"`

Decide whether to **keep** the old `"add_block_to_empty_slot_henshin_text.henshin"` module:

- If the goal is *only* to edit existing lists (insert/delete/replace), you can replace the module list with only the new module.
- If you still need “start from empty / fill empty slots” behavior, keep both modules but consider ignoring overlapping low-level units to keep the search space sane.

### 2) Reduce the operator search space to the intended API units (recommended)

To make “least edits” meaningful and avoid the search applying low-level plumbing rules directly, configure `ignoreUnits` so MoMoT mainly uses **high-level units**.

Recommended strategy:

- **Allow (do NOT ignore)**:
  - `CreateThenInsertAnywhere`
  - `ReplaceStatementAnywhere`
  - (optionally) deletion-only units if you expose them as units; if they are only rules, prefer not to allow them directly.
- **Ignore** low-level rules such as:
  - all `InsertStatement*` rules
  - all `Delete*` rules
  - all `Create*Statement` rules
  - all `InsertStatement*At/Before/Between/After*` anchored rules

If your MoMoT version only accepts unit names (not rules) in `ignoreUnits`, then:

- keep the low-level rules uncallable by ensuring MoMoT’s orchestration only picks **units** as operators, or
- accept that MoMoT will see them and explicitly ignore by name (MoMoT typically loads “units” from the module; in Henshin XMI, rules are also units).

### 3) Update/extend fitness to the 3-objective formulation

You must implement these three objectives:

#### Objective A — Reach GOAL (primary)

Keep an objective like the existing `GoalReached : maximize { ... }`:

- Return `1` if the executed program wins, else `0`.

Optionally add **hard penalty** objectives for requirements that should never be violated
but that you don't want to encode as Henshin rule guards (to avoid rule explosion).
In this repo, one such constraint is the level feature flags:

- `AllowControlFlowPenalty : minimize {`
  - `1000` if `(!level.allowLoops && solutionContainsLoop)` or `(!level.allowConditionals && solutionContainsIf)`
  - else `0`
  - `}`

This keeps the search space broad while still making the Pareto front prefer compliant solutions.

#### Objective B — Minimal edits / closeness to input solution

Use the existing transformation-length dimension:

- `Edits : minimize new TransformationLengthDimension`

This directly corresponds to “least number of edits”, because MoMoT starts from the input model and applies a transformation sequence; its length is the edit count.

#### Objective C — Shortest execution (path length)

Add a new objective:

- `ShortestPath : minimize { stepsToGoalOrPenalty(root) }`

Implementation details:

- If `GameStatus.WON`, return the **number of executed steps** (lower is better).
- If not won, return a **large penalty** (e.g. `100000`), so non-winning solutions rank poorly on this objective too.

**Important:** `BlockySimulator.run(level)` currently returns only `GameStatus`.
To get a step count, you must do ONE of:

- **Preferred:** extend `BlockySimulator` with a method that returns step count, e.g.:
  - `public static int stepsToGoal(Level level)` returning:
    - the last `GameState.step` when WON
    - a large penalty when CRASHED/RUNNING
  - or return an object `{status, steps}`.
- Then use that method from the `.momot` objective block.

Keep the objective computation **fast** (MoMoT evaluates it many times).

### 4) Ensure solution length supports exploration

Increase `search.solutionLength` if needed so the algorithm can reach GOAL while also optimizing edits and steps.

Guideline:

- If you allow both insert and replace, a reasonable starting value is `solutionLength = 30` (tune later).

### 5) Parameter values (if needed)

Your new operators have IN parameters like `kind:EInt` and `condition:EInt`.

MoMoT must be able to assign values to those parameters. Use `parameterValues` in the transformations section to provide value generators for:

- `kind` (0..2 for `AtomicStatementKind`)
- `condition` (0..2 for `ConditionKind`)

Use integer literals consistent with `blocky.ecore`:

- AtomicStatementKind: 0 TURN_LEFT, 1 TURN_RIGHT, 2 MOVE_FORWARD
- ConditionKind: 0 CHECK_FORWARD, 1 CHECK_LEFT, 2 CHECK_RIGHT

If your MoMoT setup does not require explicit generators (because you use parameter mutation), still prefer to provide bounded generators to avoid invalid values.

---

## Acceptance criteria

After the update:

- `blocky_momot/blocky.momot` validates and runs.
- Search uses the new transformations module containing statement insert/delete/replace.
- Fitness has **three objectives** reflecting:
  - maximize goal reaching (or enforce via constraint)
  - minimize edit count (transformation length)
  - minimize execution steps (shortest path)
- If the level uses feature flags (`allowLoops`, `allowConditionals`), add a hard penalty objective (e.g. `AllowControlFlowPenalty`) so solutions respect them.
- Parameter values for `kind` and `condition` are bounded to valid ranges.

---

## Notes / pitfalls to avoid

- **Paths** in `.momot` must be project-relative strings; no `platform:/resource/...`.
- Ensure the compiled `.henshin` uses resolvable types at runtime:
  - if needed, post-process `.henshin` to use the Blocky package nsURI (`http://www.example.org/blocky#//...`) as described in `docs/momot/05-ecore-henshin-integration.md`.
- Keep the search space manageable by ignoring low-level rules when possible.

