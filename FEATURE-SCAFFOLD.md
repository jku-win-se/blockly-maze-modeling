# FEATURE-SCAFFOLD — Meta Coordinator

> **You (the agent reading this) are the Scaffold Coordinator.** The human
> triggered you by pasting something like
> *"Execute `FEATURE-SCAFFOLD.md` for feature: \<one-line description\>"*.
> Your job is to **generate a complete, coordinator-driven MD folder
> structure** under `docs/features/<feature-slug>/` that another agent
> (or you, later) can use to implement that feature end-to-end against
> the Blocky Maze codebase (Java 17 / JavaFX 21 / Maven / EMF / optional
> Henshin + MoMoT, embedded Blockly Games WebView).
>
> Don't implement the feature itself here. Just produce the planning folder.
> Use `docs/webview-sync/` as the closest in-repo reference for shape,
> tone, and depth (it documents an existing cross-cutting concern with
> numbered sub-docs and a `README.md` index).

---

## 1. Trigger format

The user will send one of:

- *"Execute `FEATURE-SCAFFOLD.md` for feature: \<short description\>"*
- *"Scaffold a feature: \<description\>"*
- A paragraph describing what they want, with no explicit invocation.

In all cases, treat it as a request to **generate the folder, not to
build the feature**.

## 2. Scoping — questions to settle before writing any file

Before scaffolding, you MUST know the following. If any are unclear from the
prompt, ask the user in **one combined question batch** (use the
multiple-choice question tool when possible) — do not start writing files
with TBDs.

1. **Feature slug** — kebab-case folder name. Suggest one from the description
   (e.g., "Edit the maze grid from the WebView" → `webview-map-editor`,
   "Add a new Block type for `WhileLoop`" → `while-loop-block`). Confirm.
2. **Problem statement** — one paragraph, plain language.
3. **Audience** — `end-user` (player) / `developer` (working in Eclipse or
   Maven) / `synthesis-only` (MoMoT/Henshin pipeline) / `internal-only`.
4. **Surfaces touched** — pick any of:
   - EMF metamodel (`blocky_model/model/blocky.ecore` + regenerated `src-gen/`)
   - sample/seed XMI under `blocky_game/` (e.g. `load.xmi`, level fixtures)
   - `GameEngine` simulation / execution logic
   - `BlockyUI` JavaFX shell, `File` menu, dialogs
   - JS sync (`BlockyUI.injectSyncScript`, `JSBridge`, `applyLevelToWebView`)
   - embedded Blockly Games web app (`blocky_game/.../blockly-games-web/`)
   - Henshin transformations (`.henshin_text` → `.henshin`)
   - MoMoT search configuration (`.momot` files in `blocky_momot/`)
   - Maven build (`pom.xml`, modules, JavaFX plugin), launch configs
   - Eclipse target platform (`releng/blocky-modeling-2026-06.target`)
5. **Hard constraints** — anything immovable. Common examples for this repo:
   - must not regenerate `src-gen/` (Maven won't run EMF codegen — Eclipse only).
   - must remain runnable via `mvn -pl blocky_game javafx:run` (no new
     mandatory native deps).
   - must not require MoMoT on the classpath for the core app to start.
   - must preserve case-insensitive Blockly matching in `GameEngine`.
   - must keep the `JSBridge` reachable (`BlockyUI.jsBridge` strong reference).
6. **Success criteria** — what observable behavior proves "done"?
   Because the repo has **no automated test suite**, success is almost always
   a scripted manual check (e.g., "load `level-7.xmi`, click Run Program,
   verify the execution trace ends with `status = WON` in the saved
   `save.xmi`").

If the user is impatient and says "just go", make reasonable defaults
documented in `README.md` and proceed.

## 3. Always-included files

Every scaffold MUST produce these three files at the feature root:

```
docs/features/<slug>/
├── COORDINATOR.md   # the trigger; references the phases below
├── README.md        # human-readable overview
└── PROGRESS.md      # checklist mirroring the phases
```

Templates for all three live in § 6 of this document — copy verbatim,
fill the placeholders, do not invent new top-level sections.

## 4. Phase library — pick the ones the feature actually needs

These are the building-block phases. For each feature, **select a subset**
based on the answers to § 2.4 (surfaces touched) and lay them out
**in this order** with `NN-` prefixes (`01-…`, `02-…`, etc., contiguous).

| ID | Folder name | When to include | What it does | Standard sub-files |
|----|-------------|-----------------|--------------|--------------------|
| **P-GAP**    | `model-gap-analysis` | feature reads/writes model data we don't fully cover today | inventory current `blocky.ecore` classes/refs; list required fields; emit `gap-report.md` | `current-inventory.md`, `required-fields.md`, `gap-report.md` |
| **P-ECORE**  | `ecore-extensions`   | new EClasses, EEnums, EReferences, or EAttributes | concrete `.ecore` diff + **explicit Eclipse codegen step** (right-click `.genmodel` → Generate Model Code) + impact on `src-gen/` | `ecore-changes.md`, `regen-instructions.md` |
| **P-XMI**    | `xmi-fixtures`       | new sample/seed XMI files (levels, requests) must ship | idempotent XMI assets + a checklist of where they live (`blocky_game/`, `blocky_momot/model/input/`) | (README only, unless many fixtures) |
| **P-ENGINE** | `engine-logic`       | non-trivial changes to `GameEngine` (new block kinds, sensor types, loop semantics, trace shape) | folder layout sketch + method contracts + case-insensitivity / loop-cap reminders | `block-semantics.md` when adding block types; `trace-shape.md` when changing `ExecutionTrace`/`GameState` |
| **P-SYNC**   | `webview-sync`       | new/changed `JSBridge` methods, injected JS, or `applyLevelToWebView` flow | bridge method signatures, JSON payload shape, `suppressSync` discipline, GC-safety notes | `bridge-contract.md`, `injected-js.md` |
| **P-UI**     | `javafx-ui`          | JavaFX shell changes: menu items, dialogs, file choosers, status bar | UI tree + interaction contracts + error/loading states | (README only) |
| **P-WEB**    | `blockly-web-app`    | edits to the embedded Blockly Games copy (`blockly-games-web/`) — toolbox XML, `compressed.js` hooks, new buttons | files touched, JS globals consumed, sync hand-off | `js-globals.md` when adding globals |
| **P-HENSHIN** | `henshin-rules`     | new/changed `.henshin_text` graph transformation rules | LHS/RHS sketch, parameter kinds (IN/OUT/VAR), `Transform to Henshin` step, **NSURI patch reminder** (`http://www.example.org/blocky#`) | `rules.md`, `nsuri-patch.md` |
| **P-MOMOT**  | `momot-search`       | `.momot` config additions/edits (search-based synthesis) | initialization + `model.adapt` registration, objectives/constraints, runtime add-opens | `momot-config.md` |
| **P-INFRA**  | `build-infra`        | `pom.xml`, JavaFX plugin args, target platform, launch configs, env vars | what changes in `pom.xml` / `releng/*.target` / Eclipse launch profiles | (README only) |
| **P-VERIFY** | `verification`       | always (the repo has no automated test suite) | scripted **manual verification** steps (commands + observable assertions), optional smoke script, optional Java unit tests if introduced | `manual-checks.md`, `smoke-script.md` |
| **P-ROLL**   | `rollout`            | always, unless the feature is doc-only | observability (logs added), regression risk, go-live checklist, `PR-BODY.md` | `observability.md`, `PR-BODY.md` |

### Rules of thumb

- The first 1–2 phases must produce **planning artifacts**, never code.
- The last 2 phases are always **verification** then **rollout**.
- Skip P-XMI if the feature ships zero new XMI assets.
- Skip P-UI if the feature is engine-only; skip P-ENGINE if pure-UI.
- Skip P-HENSHIN and P-MOMOT for anything outside `blocky_momot/`.
- For pure refactors, replace P-GAP with `current-state-audit/` and P-ECORE
  with `target-state-spec/`.
- **If the feature touches `.ecore`**, P-ECORE MUST explicitly remind the
  agent that Maven does NOT regenerate code — only Eclipse's `Generate Model
  Code` does — and list the resulting `src-gen/` files that must be
  committed.
- **If the feature touches Henshin**, the rules MUST include the NSURI
  post-processing step (`http://www.example.org/blocky#`), because MOMoT
  rejects relative `.ecore` paths.
- **If the feature touches the JSBridge**, the spec MUST mention the
  GC-safety pattern (`BlockyUI.jsBridge` strong field) and the
  `suppressSync` discipline.

## 5. Phase README template

Every phase folder MUST contain a `README.md` that follows this exact
skeleton (vary only the body content):

```markdown
# Phase N — <Phase Title>

## Goal
<One paragraph: what success looks like.>

## Inputs
- <files / earlier phase outputs / external docs the agent must read first>

## Steps
1. <Concrete, ordered actions. No "explore" — every step is a do.>
2. ...

## Deliverables
- <Files / commits / artifacts produced by this phase.>

## Acceptance Gate
- [ ] <Verifiable check 1>
- [ ] <Verifiable check 2>
- [ ] <…>

## Hand-off (optional)
<Which downstream phase consumes what.>
```

Hard rules:
- Acceptance Gate items MUST be **machine-checkable** or **scripted-manual**
  with an exact, observable assertion. Examples that qualify:
  - `mvn -q -pl blocky_model,blocky_game compile` exits 0.
  - `rg "syncMap" blocky_game/src` returns ≥ 1 match.
  - A new file exists at a given path (`test -f blocky_game/level-7.xmi`).
  - Running `mvn -pl blocky_game javafx:run`, loading a specific XMI, and
    confirming an exact log line (`grep "[GameEngine] status=WON" run.log`).
  - For Henshin: the generated `.henshin` file's root `EPackage` URI equals
    `http://www.example.org/blocky#` (grep check on the XMI).
  Items like "looks good", "works in Eclipse", or "subjectively faster" are
  NOT acceptable.
- Steps must reference real repo paths (`blocky_model/model/blocky.ecore`,
  `blocky_game/src/blocky_game/GameEngine.java`,
  `blocky_game/src/blocky_game/blockly-games-web/maze.html`,
  `blocky_momot/model/input/…`, `releng/blocky-modeling-2026-06.target`),
  not invented locations.
- If you add extra sibling .md files in a phase, list them at the bottom of
  the README under a `## Reading order` heading.

## 6. File templates (copy these verbatim, then fill `{{placeholders}}`)

### 6.1 `COORDINATOR.md`

```markdown
# COORDINATOR — {{Feature Title}}

> **You are the Coordinator agent.** A human triggered you by saying
> *"execute COORDINATOR.md"* (or similar) inside `docs/features/{{slug}}/`.
> Implement this feature end-to-end and prove it works by walking the phases
> in this folder in order. Do not deviate. Do not skip the acceptance gates.

---

## 0. Mission

{{One paragraph from the problem statement. End with the user-observable
success criterion.}}

**Hard constraints (non-negotiable):**
- {{constraint 1}}
- {{constraint 2}}
- House rules from `AGENTS.md` apply (see § 3).

---

## 1. How to "trigger" me

1. Read this file fully.
2. Read `README.md` and `PROGRESS.md`.
3. Start (or resume) at the first phase whose checkbox in `PROGRESS.md` is
   still unchecked.
4. For each phase folder, open its `README.md` and follow Steps →
   Deliverables → Acceptance Gate.
5. After completing a phase, tick its boxes in `PROGRESS.md`. Do NOT proceed
   past a failing gate.
6. After the last phase, run the **Final Verification Loop** (§ 4).

---

## 2. Phase order (strictly top-to-bottom)

| # | Folder | Goal | Exit gate |
|---|--------|------|-----------|
{{One row per selected phase, copied from the picked phase library entries.}}

---

## 3. House rules

These come from `AGENTS.md` and `README.md` / `INSTALL.md`:

- **Build gate (mandatory):** `mvn -q clean compile` from the repo root must
  exit 0 before any commit. The repo has **no automated test suite** today,
  so the build is the only machine gate — do not skip it.
- **Run command:** `mvn -pl blocky_game javafx:run` (working directory is
  forced to `blocky_game/` by Maven; `load.xmi`, `save.xmi`, and the
  embedded Blockly web app are resolved relative to it).
- **EMF codegen is manual:** Maven only compiles existing `src-gen/`. If
  you edit `blocky_model/model/blocky.ecore`, you **must** regenerate via
  Eclipse: right-click `blocky_model/model/blocky.genmodel` → **Generate
  Model Code**, then commit the regenerated `blocky_model/src-gen/`.
- **JSBridge GC-safety:** JavaFX WebView holds only a weak reference to
  `window.javaBridge`. `BlockyUI` keeps a strong field `jsBridge` to prevent
  collection — do not remove or rename it.
- **Sync discipline:** When applying state from Java to the WebView (e.g.
  loading an XMI), set `suppressSync = true` in `BlockyUI` first and clear
  it only after the WebView state is consistent, to prevent stale JS state
  from clobbering the model.
- **Case-insensitive Blockly matching:** Blockly emits camelCase
  (`turnRight`, `isPathLeft`); `GameEngine.rebuildProgram` and
  `parseCondition` must keep `.toLowerCase()` before matching — do not
  hand-roll case-sensitive comparisons.
- **Deterministic starts:** `GameEngine.determineStartOrientation` uses
  `Level.startOrientation` when `eIsSet` is true, otherwise infers from the
  `START` cell's non-wall neighbour. Preserve this fallback.
- **Loop cap:** simulation step cap is `width × height × 2` (do not
  reintroduce a hard-coded 50).
- **Henshin compilation:** rules live in `.henshin_text` (textual), but
  MOMoT requires `.henshin` (XMI). Generate via Eclipse: right-click
  `.henshin_text` → **Transform to Henshin**. After compilation, ensure
  the EPackage URI in the produced `.henshin` is exactly
  `http://www.example.org/blocky#` (NSURI patch), not a relative path to
  the `.ecore`.
- **MOMoT config:** `.momot` files must register the EMF package in
  `initialization` and `model.adapt`; MOEA/MOMoT runs on Java 17 may need
  `--add-opens java.base/java.util=ALL-UNNAMED` (see `INSTALL.md`).
- **Eclipse modeling stack:** for `blocky_momot`, use the shared target
  platform `releng/blocky-modeling-2026-06.target` (Eclipse 2026-06 +
  Papyrus + Henshin SDK + MoMoT 2.0). Do not silently drift it.
- **Reuse first:** no new Maven dependencies without justification — the
  existing `pom.xml` already provides EMF (`org.eclipse.emf.*` 2.39.x) and
  JavaFX (`org.openjfx:javafx-controls`, `javafx-web` 21.x). Do not add a
  redundant dep.
- **Java conventions:** 4-space indent, no checkstyle/linter configured;
  follow the style already in `BlockyUI.java` / `GameEngine.java`.

{{Any feature-specific extra rules — e.g., "must keep `load.xmi` accepted as
both Game-root and legacy Level-root", "must not break `DMG` cell type",
"must remain runnable without MoMoT on the classpath".}}

---

## 4. Final Verification Loop

```bash
# Build (mandatory)
mvn -q clean compile

# Smoke-run the app and perform the scripted manual check below
mvn -pl blocky_game javafx:run
{{any feature-specific smoke / synthesis scripts, e.g.
# Trigger Direct Manipulation + MoMoT:
#   click an empty cell, wait for output_dm_*/ to appear under blocky_momot/
}}
```

The feature is **DONE** when:

- [ ] Every phase folder has every box ticked.
- [ ] `PROGRESS.md` shows 100%.
- [ ] `mvn -q clean compile` exits 0 from the repo root.
- [ ] {{One observable user-side check, e.g., "Loading
      `blocky_game/level-7.xmi`, clicking Run Program, saving via the File
      menu, and confirming `save.xmi` contains an `ExecutionTrace` whose
      last `GameState.status` is `WON`"}}.
- [ ] If `.ecore` changed: `blocky_model/src-gen/` is regenerated and
      committed (Eclipse-only step).
- [ ] If Henshin changed: every produced `.henshin` references
      `http://www.example.org/blocky#` (NSURI patch verified).
- [ ] PR description written in `{{last-phase}}/PR-BODY.md` (if rollout
      phase included).

---

## 5. If you get stuck

- Re-read the failing phase's `README.md`.
- Check the cross-cutting reference docs:
  - WebView ↔ Java sync: `docs/webview-sync/README.md` and
    `docs/webview-sync/01-current-sync-protocol.md`.
  - Henshin authoring: `docs/henshin/README.md` (then 02–06 and 09).
  - MoMoT config: `docs/momot/README.md` (then 02 syntax, 05 integration,
    07 generation guide).
  - Architecture: `PROJECT_SUMMARY.md` (JSBridge tables, EMF metamodel).
  - Install / target platform: `INSTALL.md` §1.2–1.3.
- If a constraint is genuinely blocking (Eclipse-only codegen, missing
  Papyrus feature on the target site, MoMoT not on the runtime classpath,
  etc.), STOP and surface it to the human — do not fabricate generated
  code, fake `src-gen/` diffs, or hand-edit XMI to look like Eclipse output.

---

## 6. Quick-start prompt (what the human pastes to trigger you)

> "Execute `docs/features/{{slug}}/COORDINATOR.md`. Follow the phases in
> order, update `PROGRESS.md` after each phase, and stop at any failing
> acceptance gate so I can review."
```

### 6.2 `README.md`

```markdown
# {{Feature Title}} — Overview

> **Entry point for humans.** If you want an agent to actually build this,
> open `COORDINATOR.md`.

## Problem
{{Plain-language paragraph. Who the user is (player / developer /
synthesis pipeline), what they want, why now.}}

## Solution shape
{{Two-to-five sentence sketch + optional ASCII diagram. Mention which of
the three layers — EMF model, GameEngine, WebView/JSBridge,
Henshin/MoMoT — carry the change.}}

## What exists vs. what's new

Existing in repo (do **not** duplicate):
- {{repo path 1 — what to reuse, e.g.
  `blocky_game/src/blocky_game/BlockyUI.java#injectSyncScript`}}
- {{repo path 2 — e.g. `blocky_model/src-gen/blocky/*` generated API}}

To be added by this feature:
- {{new Ecore classes / `JSBridge` methods / JavaFX dialogs / Henshin rules
  / `.momot` blocks / XMI fixtures}}

## Folder map

```text
docs/features/{{slug}}/
├── COORDINATOR.md          ← agent trigger
├── README.md               ← this file
├── PROGRESS.md             ← checklist
{{tree of selected phase folders, one line each, with a short suffix description}}
```

## Success criteria
1. {{Behavior 1 — observable, time-boxed, e.g. "Clicking an empty cell in
   the WebView marks it `CellType.DMG` in the saved XMI"}}.
2. {{Behavior 2}}.
3. `mvn -q clean compile` exits 0 from the repo root.
4. {{If applicable: "Loading the legacy `Level`-root `load.xmi` still works
   (wrapped into a Game in-memory)."}}
5. {{If applicable: "Feature degrades cleanly when MoMoT is not on the
   classpath."}}
```

### 6.3 `PROGRESS.md`

```markdown
# Progress Tracker — {{Feature Title}}

> The Coordinator updates this file after each phase.

**Overall:** 0 / {{N}} phases complete.

---

{{For each phase, emit a section like:}}

## Phase {{N}} — {{Phase Title}}  `{{NN-folder}}/`

- [ ] {{Deliverable 1}}
- [ ] {{Deliverable 2}}
- [ ] Gate: {{single-line statement of the acceptance gate}}
```

## 7. House rules to embed in every scaffold

Always copy these from `AGENTS.md` / `README.md` / `INSTALL.md` into the
generated `COORDINATOR.md` § 3 — do not paraphrase the meaning, do not omit:

- **Build gate (mandatory):** `mvn -q clean compile` from the repo root.
  No automated test suite exists yet; the build is the only machine gate.
- **Run command:** `mvn -pl blocky_game javafx:run` (working dir auto-set
  to `blocky_game/`).
- **EMF codegen is Eclipse-only:** regenerate `blocky_model/src-gen/` via
  `blocky_model/model/blocky.genmodel` → **Generate Model Code** whenever
  `blocky.ecore` changes. Commit `src-gen/`.
- **JSBridge GC-safety:** keep `BlockyUI.jsBridge` as a strong field —
  JavaFX WebView only weak-refs `window.javaBridge`.
- **Sync discipline:** use `suppressSync` when Java pushes state into the
  WebView; clear it only after state is consistent.
- **Case-insensitive Blockly matching:** keep `.toLowerCase()` in
  `GameEngine.rebuildProgram` / `parseCondition` (Blockly camelCase:
  `turnRight`, `isPathLeft`, …).
- **Deterministic starts:** `Level.startOrientation` via `eIsSet`, else
  infer from `START` neighbour. Loop cap `width × height × 2`.
- **Henshin:** edit `.henshin_text`, then **Transform to Henshin** in
  Eclipse. After compilation, the `.henshin` EPackage URI must be
  `http://www.example.org/blocky#` (NSURI patch).
- **MOMoT:** `.momot` files register the EMF package in `initialization`
  and `model.adapt`. Runtime may need
  `--add-opens java.base/java.util=ALL-UNNAMED`.
- **Eclipse target platform:** `releng/blocky-modeling-2026-06.target`
  (Eclipse 2026-06 + Papyrus + Henshin SDK + MoMoT 2.0).
- **Reuse first:** no new top-level dependencies without justification —
  EMF (`org.eclipse.emf.*` 2.39.x) and JavaFX (`org.openjfx:*` 21.x) are
  already in `pom.xml`.
- **Java conventions:** 4-space indent, follow the style already in
  `BlockyUI.java` / `GameEngine.java`.

## 8. Quality checklist for the generated scaffold

Before declaring the scaffold complete, verify:

- [ ] Folder lives under `docs/features/<slug>/` and slug is kebab-case.
- [ ] `COORDINATOR.md`, `README.md`, `PROGRESS.md` all present at the root.
- [ ] All phase folders use the `NN-name/` numbering (no gaps).
- [ ] Every phase has a `README.md` matching the § 5 skeleton.
- [ ] Every Acceptance Gate item is machine-checkable or scripted-manual
      with an exact, observable assertion (no "looks good").
- [ ] `PROGRESS.md` lists every phase with at least one checkbox.
- [ ] House rules from § 7 appear in `COORDINATOR.md` § 3.
- [ ] If the feature touches `blocky.ecore`, P-ECORE explicitly states
      that Maven does NOT regenerate code and lists the Eclipse step.
- [ ] If the feature touches Henshin, the NSURI patch
      (`http://www.example.org/blocky#`) is explicitly required.
- [ ] If the feature touches the JSBridge, the GC-safety field and
      `suppressSync` discipline are explicitly required.
- [ ] No "TODO", "TBD", or placeholder `{{…}}` strings remain in the
      generated files (only the templates in this document keep them).
- [ ] The Quick-start prompt at the end of `COORDINATOR.md` uses the real
      `docs/features/<slug>/COORDINATOR.md` path.
- [ ] Repo paths cited in the scaffold actually exist (sanity-check with
      a quick `ls` / `rg` before declaring done).

## 9. Reference example

There is no existing `docs/features/` folder in this repo yet (this
scaffold introduces that convention). The closest in-repo references for
**shape, tone, and depth** are:

- `docs/webview-sync/` — README index + `01-…`, `02-…`, `03-…` numbered
  sub-docs documenting a cross-cutting concern (JS ↔ Java sync). Mirror
  this when laying out per-phase READMEs.
- `docs/henshin/` and `docs/momot/` — reference-doc style with a
  README index pointing at numbered topic files. Useful when a phase
  needs supplementary reading-order docs.
- `PROJECT_SUMMARY.md` — JSBridge tables, EMF metamodel summary, and JS
  global tables. Useful for the "what exists" section of a feature README.

When in doubt about depth, structure, or tone, mirror those folders.

## 10. What you (the Scaffold Coordinator) hand back

After creating the files:

1. Print a short summary listing every file you created (relative path +
   1-line purpose).
2. Print the exact Quick-start prompt the human can paste next time to
   trigger the new coordinator.
3. Stop. Do NOT begin implementing the feature unless the human asks.
