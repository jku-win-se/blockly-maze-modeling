# Blocky Maze – Project Summary

A **Java/JavaFX desktop application** that recreates the [Blockly Games Maze](https://blockly.games/maze) puzzle. Players drag-and-drop programming blocks to navigate a character ("Pegman") through a grid-based maze—from the Start cell to the Goal cell.

**Where to start:** For the main app and EMF model, see the workspace table below. For **search-based solution synthesis** (Henshin + MOMoT), start with [blocky_momot/README.md](blocky_momot/README.md). The **docs/henshin/** and **docs/momot/** folders contain agent-oriented reference docs used by the `blocky_momot` project. For **WebView↔Java sync** and extending the game (e.g. map editing via UI), see [docs/webview-sync/README.md](docs/webview-sync/README.md).

---

## Workspace Structure

| Project | Role | Key Technologies |
|---------|------|-----------------|
| `blocky_model` | EMF metamodel & generated code | Eclipse EMF, Ecore, XMI |
| `blocky_game` | Main application (UI + engine) | Java 17, JavaFX 21, WebView |
| `blocky_momot` | Search-based solution synthesis (Henshin + MOMoT) | Eclipse MOMoT, Henshin, EMF |
| `docs/henshin/` | Agent docs for Henshin textual rules (`.henshin_text`) | — |
| `docs/momot/` | Agent docs for MOMoT config (`.momot`) | — |

---

## MOMoT configuration (search-based model transformation)

For **generating or editing `.momot` configuration files** (MOMoT = Marrying Search-based Optimization and Model Transformation Technology), use the reference docs in this repo:

- **Start here**: [docs/momot/README.md](docs/momot/README.md) — overview, file format, and which doc to read for each task.
- **Syntax and structure**: [docs/momot/grammar-and-structure.md](docs/momot/grammar-and-structure.md) — full grammar-derived reference (sections, keywords, enums).
- **Examples and templates**: [docs/momot/examples-and-templates.md](docs/momot/examples-and-templates.md) — minimal/full templates and case study snippets (Stack, Class Modularization, CRA).
- **Links**: [docs/momot/references.md](docs/momot/references.md) — official MOMoT site, grammar, repo, case studies.

When asked to create or modify MOMoT config, read **README.md** first, then **grammar-and-structure.md** and **examples-and-templates.md** as needed.

---

## Henshin transformation language (graph transformation rules)

For **writing or editing Henshin transformation rules and units** (`.henshin` / `.henshin_text`), use the reference docs in this repo:

- **Start here**: [docs/henshin/README.md](docs/henshin/README.md) — overview, file formats (textual vs graphical), and which doc to read for each task.
- **Concepts**: [docs/henshin/meta-model-and-concepts.md](docs/henshin/meta-model-and-concepts.md) — transformation meta-model: rules (LHS/RHS), application conditions (PAC/NAC), rule-nesting (multi-rules), units (loop, sequential, conditional, priority, independent), parameters, annotations. Based on [Eclipse Henshin wiki](https://wiki.eclipse.org/Henshin/Transformation_Meta-Model).
- **Textual syntax**: [docs/henshin/grammar-and-syntax.md](docs/henshin/grammar-and-syntax.md) — grammar for `.henshin_text`: `ePackageImport`, `rule`, `graph`, nodes (preserve/create/delete/forbid/require), edges, `multiRule`, `matchingFormula`, units (`while`, `for`, `if-then-else`, `priority`, `independent`).
- **Grammar reference (Xtext)**: [docs/henshin/grammar-guide.md](docs/henshin/grammar-guide.md) — authoritative grammar derived from [Henshin_text.xtext](https://github.com/eclipse-henshin/henshin/blob/0105af629a80c225466687d8412cfa9f1672cbb9/plugins/org.eclipse.emf.henshin.text/src/org/eclipse/emf/henshin/text/Henshin_text.xtext): rules, RuleElement, GraphElements, Expression precedence, ParameterKind, Type enum, keyword list.
- **Examples and Interpreter API**: [docs/henshin/examples-and-templates.md](docs/henshin/examples-and-templates.md) — official Bank example (NAC, VAR parameters, attribute conditions, multi-rule with checkDangling=false), Interpreter API usage. In-repo: `Henshin-bank-example/org.henshin.bank/`.
- **Links**: [docs/henshin/references.md](docs/henshin/references.md) — official Eclipse Henshin site, wiki (Transformation Meta-Model, Textual Editor, Units, Parameters), examples, repository.

When asked to create or modify Henshin rules or units, read **README.md** first, then **meta-model-and-concepts.md**, **grammar-and-syntax.md**, **grammar-guide.md** (for exact grammar), and **examples-and-templates.md** as needed. Project transformations: `blocky_model/transformations/add_block_to_empty_slot.henshin_text`. Official Bank example: `Henshin-bank-example/org.henshin.bank/`.

---

## EMF Metamodel (`blocky_model`)

Defined in [`blocky.ecore`](file:///c:/Users/domin/eclipse-workspace-blocky/blocky_model/model/blocky.ecore). Generated Java API in `src-gen/blocky/`.

### Enums
- **Direction** – `NORTH`, `EAST`, `SOUTH`, `WEST`
- **TurnDirection** – `LEFT`, `RIGHT`
- **CellType** – `EMPTY`, `WALL`, `START`, `GOAL`
- **SensorDirection** – `AHEAD`, `LEFT`, `RIGHT`
- **GameStatus** – `RUNNING`, `WON`, `CRASHED`

### Core Classes
- **Game** – Root container. Owns a list of `Level` instances (`levels`).
- **Level** – Level container (`id`, `title`, `description`, `maxBlocks`, `allowLoops`, `allowConditionals`, `startOrientation`). Owns a `GridMap`, a `solution` (head `Block`), and `ExecutionTrace` list.
- **GridMap** – width × height grid. Contains `Cell` list.
- **Cell** – Has a `CellType` and bidirectional references `top`/`bottom`/`left`/`right` to neighbouring cells.
- **Block** *(abstract)* – Linked-list via `next` reference. Concrete subtypes:
  - `MoveForward`
  - `Turn` (direction: `TurnDirection`)
  - `RepeatUntilGoal` (body → `Block`)
  - `IfStatement` (condition: `SensorDirection`, thenBranch → `Block`, elseBranch → `Block`)
- **ExecutionTrace** → ordered list of **GameState** (step, position → Cell, orientation, status, executingBlock, next/previous linked-list).

---

## Application (`blocky_game`)

All visuals are rendered by an embedded **JavaFX WebView** loading the Blockly Games Maze web pages. There is no native JavaFX rendering.

### Entry Point
[`Main.java`](file:///c:/Users/domin/eclipse-workspace-blocky/blocky_game/src/blocky_game/Main.java) – Launches `BlockyUI` via `Application.launch()`.

### UI Layer – [`BlockyUI.java`](file:///c:/Users/domin/eclipse-workspace-blocky/blocky_game/src/blocky_game/BlockyUI.java)
- Extends `javafx.application.Application`.
- Embeds a **JavaFX WebView** to render the Blockly Games Maze web page locally. There is **no menu bar**; the only UI is the WebView (including the level nav 1–10 and **Model**).
- On WebView load, attaches a `JSBridge` instance to `window.javaBridge` and injects a JavaScript sync script (`injectSyncScript`).
- Keeps a **strong Java reference** to the `JSBridge` (field `jsBridge`) to prevent JavaFX WebView garbage collection of the bridge object.
- Injects a **Model** pill into the WebView level bar (next to levels 1–10). Only **Model** loads from XMI; levels 1–10 are predefined in the WebView and are not loaded from file.
- **Load** uses a single hardcoded file: **load.xmi** (searched as `blocky_game/load.xmi` then `load.xmi`). **Save** writes to **save.xmi** (same path convention). See *JSBridge* and *Data flow* below.
- When loading Model, applies the loaded level to the WebView via `applyLevelToWebView`: injects grid, start/goal, metadata, and Blockly XML; polls until `svgMaze` and `BlocklyInterface` exist; redraws maze with `Wd()`, recreates the `#look` element if missing (so "Run Program" does not throw), loads blocks with `BlocklyInterface.Kv()`, then resets pegman with `$d(false)`.
- Contains a Blockly XML parser (`parseBlocklyXml` → `parseBlockElement` → `firstBlockChild`) that converts Blockly's serialised XML into `List<Map<String, Object>>` for the engine.

### Game Engine – [`GameEngine.java`](file:///c:/Users/domin/eclipse-workspace-blocky/blocky_game/src/blocky_game/GameEngine.java)
- `initializeGame()` – Creates EMF `Resource` (XMI), a root `Game`, and an empty `Level` inside `Game.levels`.
- `setMapFromJson(String)` – Parses a 2D JSON grid from JS, builds `Cell` objects, links neighbours. Clears `startOrientation` via `eUnset()` so it is freshly set by `syncLevelMeta`.
- `syncLevelMeta(String)` – Parses level metadata JSON from JS, sets `id`, `title`, `startOrientation`, `maxBlocks`, `allowLoops`, `allowConditionals` on the EMF `Level`.
- `cycleCellType(int, int)` / `setUniqueCellType()` – Cell-type editing helpers. Uses `map.getWidth()` for index calculation.
- `rebuildProgram(List<Map<String, Object>>)` – Accepts block data from the WebView, builds EMF `Block` tree via `createBlockFromData()` and `buildSequence()`. All direction checks are **case-insensitive** (Blockly uses camelCase like `turnRight`, `isPathLeft`).
- `simulateUserProgram()` – Steps through the Block linked-list, producing `GameState` snapshots inside an `ExecutionTrace`. Calls `determineStartOrientation()` to resolve the starting direction.
- `determineStartOrientation(Cell)` – Returns `Level.startOrientation` if explicitly set (via `eIsSet`); otherwise infers direction from the first non-wall neighbour of the START cell.
- `executeSingle()` / `executeSequence()` – Recursive execution of blocks (Move, Turn, RepeatUntilGoal, IfStatement). Loop cap is `width × height × 2` steps.
- `checkSensor()`, `getAdjacent()`, `getRelativeDir()`, `calculateTurn()` – Maze navigation helpers.
- `parseCondition(String)` – Maps Blockly sensor strings (`isPathForward`, `isPathLeft`, `isPathRight`) to `SensorDirection` enum values. Case-insensitive; also recognises `"forward"` as alias for `"ahead"`.
- `saveModel()` – Sets the resource URI to **save.xmi** (`blocky_game/save.xmi` or `save.xmi`) and persists the EMF resource (root `Game`, including **execution traces**) to that file. Called **only once per "Run Program"**, at the end of `simulateUserProgram()` after the trace is built.
- **Load from file** – `loadFromFile(File)` loads the model from **load.xmi** (when using the Model button). It accepts both **Game-root** files (new) and legacy **Level-root** files (wrapped into a Game in-memory).

---

## JSBridge — WebView ↔ Java Interface

For agent-oriented documentation of the sync protocol and how to extend it (e.g. map editing via UI), see **[docs/webview-sync/README.md](docs/webview-sync/README.md)**.

The `JSBridge` is a public inner class of `BlockyUI`. An instance is stored in the `jsBridge` field (strong reference to prevent GC) and attached to the WebView's `window.javaBridge` after each page load. The injected JavaScript calls these methods to push data from the Blockly Maze runtime into the Java/EMF model.

### Bridge Methods

| Method | Signature | Called by JS when | Java action |
|--------|-----------|-------------------|-------------|
| **`logJS`** | `void logJS(String msg)` | Any JS `log()` call (debug helper) | Prints to `System.out` with `[WebView JS]` prefix |
| **`syncMap`** | `void syncMap(String mapJson)` | Workspace becomes active (500 ms after detection); also when Run is clicked (before simulation) | Calls `GameEngine.setMapFromJson(mapJson)` — rebuilds the EMF `GridMap` |
| **`syncLevelMeta`** | `void syncLevelMeta(String metaJson)` | Immediately after `syncMap`; also when Run is clicked | Calls `GameEngine.syncLevelMeta(metaJson)` — sets level config on EMF `Level` |
| **`syncModel`** | `void syncModel(String xml)` | Blockly workspace changes (via change listener + 1 s poll); also when Run is clicked | Parses Blockly XML → `List<Map>`, calls `GameEngine.rebuildProgram()` |
| **`loadModel`** | `void loadModel()` | User clicks the **Model** pill in the level nav | Loads **load.xmi** via `loadFromFile(getModelXmiFile())`, then navigates to maze URL and applies level to WebView |
| **`runSimulation`** | `void runSimulation()` | Run button hidden (MutationObserver on `#runButton` style) | Syncs state (blocks, map, meta) then calls `GameEngine.simulateUserProgram()`; engine calls `saveModel()` **after** simulation (so execution trace is saved to **save.xmi**) |

### Synced Variables: JS Global → Bridge Method → EMF Attribute

The table below traces every piece of runtime state from its JavaScript global variable in the Blockly Maze `compressed.js`, through the bridge method, to the final EMF attribute it populates.

| JS global | Meaning in Blockly Maze | Bridge method | JSON key | GameEngine method | EMF target (`Level` / `GridMap` / `Cell`) |
|-----------|------------------------|---------------|----------|-------------------|------------------------------------------|
| `window.X` | 2D grid array. Each row is `int[]`: `0`=wall, `1`=empty, `2`=start, `3`=goal. Indexed as `X[row][col]`. | `syncMap(mapJson)` | *(entire payload)* | `setMapFromJson()` | `GridMap.width`, `GridMap.height`, `Cell.x`, `Cell.y`, `Cell.type`, `Cell.top/bottom/left/right` |
| `window.K` | Current level number (1–10, from `?level=` URL param) | `syncLevelMeta(metaJson)` | `"level"` | `syncLevelMeta()` | `Level.id`, `Level.title` (set to `"Maze Level N"`) |
| `window.Od` | Max blocks allowed for the level. Per-level array: `[∞, ∞, 2, 5, 5, 5, 5, 10, 7, 10][K]`. `Infinity` means unlimited. | `syncLevelMeta(metaJson)` | `"maxBlocks"` (`-1` if unlimited) | `syncLevelMeta()` | `Level.maxBlocks` (0 = unlimited) |
| `T` (local) | Pegman direction. `$d()` reset always sets `T=1`. Encoding: `0`=NORTH, `1`=EAST, `2`=SOUTH, `3`=WEST. | `syncLevelMeta(metaJson)` | `"startDirection"` (hardcoded `1`) | `syncLevelMeta()` | `Level.startOrientation` (`Direction` enum) |
| `#toolbox` DOM | Toolbox XML element listing available block types. Contains `maze_forever` when `K > 2`, `maze_if` when `K >= 6`. | `syncLevelMeta(metaJson)` | `"allowLoops"` | `syncLevelMeta()` | `Level.allowLoops` |
| `#toolbox` DOM | *(same element)* | `syncLevelMeta(metaJson)` | `"allowConditionals"` | `syncLevelMeta()` | `Level.allowConditionals` |
| *(workspace DOM)* | Blockly block tree as XML (`<xml><block type="...">...</block></xml>`) | `syncModel(xml)` | *(entire payload — XML string)* | `rebuildProgram()` | `Level.solution` (`Block` linked-list tree) |
| `#runButton` hidden | Blockly hides button when user presses "Run" | `runSimulation()` | *(none — MutationObserver)* | `simulateUserProgram()` | `Level.traces` (`ExecutionTrace` → `GameState` list) |

### Variables NOT yet synced (and their current handling)

| JS variable | Meaning | Current Java handling | Risk |
|-------------|---------|----------------------|------|
| `window.Nd` | Skin index (pegman/astro/panda) | Not synced. Cosmetic only. | None — no EMF equivalent needed. |
| `Qd` / `Rd` | Grid rows / columns (derived from `X`) | Derived from `X` during `setMapFromJson`. | None — always consistent. |
| `nd` | `{x, y}` of the START cell | Found by scanning cells for `CellType.START` in `simulateUserProgram()`. | None — always consistent with `X`. |
| `od` | `{x, y}` of the GOAL cell | Found by scanning cells for `CellType.GOAL`. | None — always consistent with `X`. |

### Injected JavaScript Overview

The sync script is a self-executing function injected via `WebEngine.executeScript()` after the page loads. It performs four tasks:

1. **Workspace polling** — Polls every 1 s for the Blockly workspace (`getWS()`). Once found, attaches a change listener and a 1 s fallback poll that both call `sync(ws)`, which serialises the workspace to XML and calls `javaBridge.syncModel(xml)` when it changes.

2. **Map + metadata push** — 500 ms after the workspace is found, reads `window.X` (grid), `window.K` (level), `window.Od` (maxBlocks), and the `#toolbox` DOM (to detect `allowLoops`/`allowConditionals`), then calls `javaBridge.syncMap(JSON.stringify(X))` followed by `javaBridge.syncLevelMeta(JSON.stringify({...}))`.

3. **Run-button hook** — Polls every 500 ms for `#runButton`. Once found, attaches a `MutationObserver` watching for `style` attribute changes. When Blockly hides the button (`display: none`), the observer first syncs current state (workspace XML, map, level meta) to Java, then calls `javaBridge.runSimulation()`. The engine runs the simulation and then calls `saveModel()` once, so **save.xmi** includes the execution trace. No save is performed on block/map/meta sync outside of Run.

4. **Model pill** — After the level bar is present, a "Model" span is injected next to levels 1–10. Its click handler calls `javaBridge.loadModel()`, which loads **load.xmi** and applies the level to the WebView.

---

## Data Flow

```
  Blockly Maze (WebView / JS)
  ┌─────────────────────────────────────────────────────────┐
  │  window.X ──────────► syncMap(json)                     │
  │  window.K, Od, T ───► syncLevelMeta(json)               │
  │  #toolbox DOM ───────► (allowLoops, allowConditionals)  │
  │  workspace XML ─────► syncModel(xml)                    │
  │  #runButton hidden ─► runSimulation()                   │
  └──────────────┬──────────────────────────────────────────┘
                 │ JSBridge (window.javaBridge)
                 ▼
  BlockyUI.java ─── parses XML / forwards JSON ───►
                 │
                 ▼
  GameEngine.java
  ┌─────────────────────────────────────────────────────────┐
  │  setMapFromJson()    → GridMap, Cells, neighbour links  │
  │  syncLevelMeta()     → Level.id, Level.title,           │
  │                        Level.startOrientation,          │
  │                        Level.maxBlocks,                 │
  │                        Level.allowLoops,                │
  │                        Level.allowConditionals          │
  │  rebuildProgram()    → Level.solution (Block tree)      │
  │  simulateUserProgram → Level.traces (ExecutionTrace)    │
  │                                                         │
  │  saveModel() (after simulateUserProgram) ─► save.xmi      │
  │    (XMI root is Game → levels[0] is current level)       │
  │  loadFromFile(load.xmi) (Model button) ◄── load.xmi      │
  └─────────────────────────────────────────────────────────┘
```

---

## Embedded Web App (`blockly-games-web/`)

A local copy of the Blockly Games website lives inside `blocky_game/src/blocky_game/blockly-games-web/`. The WebView loads `maze.html` from this directory.

### Key JS globals in `maze/generated/en/compressed.js`

| Variable | Type | Description |
|----------|------|-------------|
| `K` | `int` | Level number (1–10), parsed from URL `?level=` param |
| `X` | `int[][]` | Map grid (`X[row][col]`): 0=wall, 1=path, 2=start, 3=goal |
| `Qd` | `int` | Number of rows (`X.length`) |
| `Rd` | `int` | Number of columns (`X[0].length`) |
| `Od` | `int\|Infinity` | Max blocks for this level |
| `nd` | `{x, y}` | Start cell (col, row) — found by scanning `X` for value `2` |
| `od` | `{x, y}` | Goal cell (col, row) — found by scanning `X` for value `3` |
| `Q` | `int` | Pegman current column |
| `S` | `int` | Pegman current row |
| `T` | `int` | Pegman direction: 0=N, 1=E, 2=S, 3=W. Reset to `1` (EAST) by `$d()`. |
| `H` | `Workspace` | The Blockly workspace instance |
| `Nd` | `int` | Skin index (0=pegman, 1=astro, 2=panda) |
| `U` | `Object` | Skin config (image paths, sounds, colours) |
| `P` | `int` | Animation step delay in ms (default 100) |

---

## Known Issues Fixed

| Issue | Root cause | Fix |
|-------|-----------|-----|
| Simulation always started facing NORTH | `startOrientation` EMF default was `NORTH`; never synced from WebView | JS now sends `startDirection` via `syncLevelMeta`; Java also infers from map layout as fallback |
| `turnRight` blocks treated as LEFT | `String.contains("right")` is case-sensitive; Blockly sends camelCase `turnRight` (capital R) | All direction checks now use `.toLowerCase()` before matching |
| `isPathRight`/`isPathLeft` conditions wrong | Same case-sensitivity issue in `parseCondition()` | Fixed with `.toLowerCase()`; also added `"forward"` as alias for `"ahead"` |
| Hard-coded grid width 8 in `cycleCellType` | `y * 8 + x` index calculation | Replaced with `y * map.getWidth() + x` |
| Hard-coded 50-step loop limit | Fixed constant regardless of maze size | Now uses `width × height × 2` |
| JSBridge silently garbage-collected | JavaFX WebView holds only weak references to Java objects | `BlockyUI` now keeps a strong `jsBridge` field |
| Run button click not detected | Blockly captures click/mousedown events before our listener | Replaced with `MutationObserver` watching for `#runButton` `style.display` change |
| Level metadata not stored on model | `id`, `title`, `allowLoops`, `allowConditionals` were never set | `syncLevelMeta` now sets all Level attributes from JS |
| `c.style` null when clicking Run after loading Model | Clearing `svgMaze` children and calling `Wd()` removed the `<g id="look">` element; `$d()` and run animation expect it | After `Wd()`, inject script recreates `#look` with the three `<path>` elements if missing |
| DanglingHREFException on save after loading Model | `GameState.position` pointed at `Cell`s from the old map after `setMapFromJson` replaced the map | `setMapFromJson` now clears `currentLevel.getTraces()` before replacing the map |
| XMI saved too often (every sync) | `saveModel()` was called from setMapFromJson, syncLevelMeta, rebuildProgram, etc. | Save only when user clicks "Run Program"; `saveModel()` is called once at the end of `simulateUserProgram()` |
| Execution trace not saved | Save was triggered before the simulation ran | Save moved to after `executeSequence()` in `simulateUserProgram()` so **save.xmi** includes the full trace |

---

## Dependencies

- **Java SE 17**
- **JavaFX SDK 21.0.10** (bundled in `javafx-sdk-21.0.10/`)
- **Eclipse EMF** (via PDE required-plugins)
- **Eclipse Modeling** (Ecore, GenModel, Sirius `.aird`)

---

## How to Run

1. Open the workspace in **Eclipse IDE** (with EMF and JavaFX support).
2. Ensure the `blocky_model` project is built (EMF code generation).
3. Run `blocky_game` → `Main.java` as a Java Application with JavaFX VM args:
   ```
   --module-path javafx-sdk-21.0.10/lib --add-modules javafx.controls,javafx.web
   ```
