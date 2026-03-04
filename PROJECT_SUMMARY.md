# Blocky Maze – Project Summary

A **Java/JavaFX desktop application** that recreates the [Blockly Games Maze](https://blockly.games/maze) puzzle. Players drag-and-drop programming blocks to navigate a character ("Pegman") through a grid-based maze—from the Start cell to the Goal cell.

---

## Workspace Structure

| Project | Role | Key Technologies |
|---------|------|-----------------|
| `blocky_model` | EMF metamodel & generated code | Eclipse EMF, Ecore, XMI |
| `blocky_game` | Main application (UI + engine) | Java 17, JavaFX 21, WebView |
| `website_0f769626` | Downloaded Blockly Games Maze pages | HTML, JS (reference/embedded UI) |

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
- **Level** – Top-level container (title, id, description, maxBlocks, allowLoops, allowConditionals, startOrientation). Owns a `GridMap`, a `solution` (head `Block`), and `ExecutionTrace` list.
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
- Embeds a **JavaFX WebView** to render the Blockly Games Maze web page locally.
- Injects a JavaScript sync script (`injectSyncScript`) that bridges the Blockly workspace ↔ EMF model:
  - `JSBridge` inner class exposes `logJS()`, `runSimulation()`, and `syncModel(json)` to JavaScript.
  - Includes a JSON parser (`parseJsonArray`, `parseJsonObject`, `splitTopLevelItems`) to convert block structures without external libraries.

### Game Engine – [`GameEngine.java`](file:///c:/Users/domin/eclipse-workspace-blocky/blocky_game/src/blocky_game/GameEngine.java)
- `initializeGame()` – Creates EMF `Resource` (XMI), builds a `Level`, calls `generateLevel1Map()`.
- `generateLevel1Map()` – Programmatically builds a grid of `Cell` objects with walls, start, and goal.
- `cycleCellType()` / `setUniqueCellType()` – Cell-type editing helpers.
- `rebuildProgram(List<Map<String, Object>> blockData)` – Accepts block data from the WebView, builds EMF `Block` tree via `createBlockFromData()` and `buildSequence()`.
- `simulateUserProgram()` – Steps through the Block linked-list, producing `GameState` snapshots inside an `ExecutionTrace`.
- `executeSingle()` / `executeSequence()` – Recursive execution of blocks (Move, Turn, RepeatUntilGoal, IfStatement).
- `checkSensor()`, `getAdjacent()`, `getRelativeDir()`, `calculateTurn()` – Maze navigation helpers.
- `saveModel()` – Persists the EMF resource to [`level1_state.xmi`](file:///c:/Users/domin/eclipse-workspace-blocky/blocky_game/level1_state.xmi).

---

## Web Reference (`website_0f769626`)

Downloaded Blockly Games Maze pages used as the embedded WebView content:
- `index.html` – Main entry
- `html/page_2.html` … `page_11.html` – Individual level pages
- `js/boot.js`, `compressed.js`, `en.js`, `prettify.js` – Blockly runtime
- `css/` – Stylesheets
- `images/` – Web assets

### Embedded Web App (`blockly-games-web/`)

A local copy of the Blockly Games website lives inside `blocky_game/src/blocky_game/blockly-games-web/`. The WebView loads `maze.html` from this directory.

---

## Data Flow

```
┌──────────────┐   JSON    ┌──────────────┐   EMF     ┌────────────────────┐
│  Blockly     │ ───────►  │  BlockyUI    │ ───────►  │   GameEngine       │
│  WebView     │ syncModel │  (JSBridge)  │ rebuild   │ rebuildProgram()   │
│  (JS)        │           │              │ Program   │ simulateProgram()  │
└──────────────┘           └──────────────┘           └────────────────────┘
                                                              │
                                                     saveModel() ──► level1_state.xmi
```

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
