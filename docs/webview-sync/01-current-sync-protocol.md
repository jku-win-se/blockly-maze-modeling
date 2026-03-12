# Current sync protocol

Source of truth: `blocky_game/src/blocky_game/BlockyUI.java` (JSBridge and injected script) and `blocky_game/src/blocky_game/GameEngine.java` (model updates).

## Bridge object

After each WebView load, Java attaches a single bridge instance to `window.javaBridge`. The injected script and any custom JS must use `window.javaBridge` (or `window.parent.javaBridge` in frames). The bridge is a public inner class `BlockyUI.JSBridge`; Java keeps a strong reference in `BlockyUI.jsBridge` so the WebView does not garbage-collect it.

## Bridge methods (JS → Java)

| Method | Signature | When JS calls it | Java action |
|--------|-----------|-------------------|-------------|
| `logJS` | `void logJS(String msg)` | Debug / `window.onerror` | Prints to `System.out` with `[WebView JS]` prefix. |
| `syncMap` | `void syncMap(String mapJson)` | 500 ms after workspace found; also before Run | `GameEngine.setMapFromJson(mapJson)` — rebuilds GridMap and Cells, clears traces, clears startOrientation so meta can set it. |
| `syncLevelMeta` | `void syncLevelMeta(String metaJson)` | Right after syncMap; also before Run | `GameEngine.syncLevelMeta(metaJson)` — sets Level.id, title, maxBlocks, allowLoops, allowConditionals, startOrientation. |
| `syncModel` | `void syncModel(String xml)` | Workspace change listener + 1 s poll; also before Run | If `!suppressSync`: parses Blockly XML to `List<Map>`, calls `GameEngine.rebuildProgram(data)`. |
| `loadModel` | `void loadModel()` | User clicks “Model” pill | Loads load.xmi, navigates to maze URL, sets `pendingApplyLevel`; on next load, `applyLevelToWebView` runs. |
| `runSimulation` | `void runSimulation()` | MutationObserver sees `#runButton` style display none | After syncing workspace, map, and meta, calls `GameEngine.simulateUserProgram()`; engine calls `saveModel()` once at the end. |

There is also `saveModelNow()` (calls `engine.saveModel()`) but the normal flow saves only after Run via `simulateUserProgram()`.

## Payload formats

### syncMap — mapJson

- **Shape**: JSON string of a 2D number array, e.g. `"[[0,0,1,...],[...],...]"`.
- **Semantics**: Row-major grid; indexing is `[row][col]`. Values: `0` = wall, `1` = empty, `2` = start, `3` = goal.
- **Java**: `GameEngine.setMapFromJson(String)` parses it, creates `GridMap` with `Cell` list, sets `Cell.x`, `Cell.y`, `Cell.type`, and links neighbours (`top`/`bottom`/`left`/`right`). Clears `currentLevel.getTraces()` and unsets `Level.startOrientation`.

### syncLevelMeta — metaJson

- **Shape**: JSON object with keys: `level` (int), `maxBlocks` (int, -1 = unlimited), `startDirection` (int 0–3: N/E/S/W), `allowLoops` (bool), `allowConditionals` (bool).
- **Java**: `GameEngine.syncLevelMeta(String)` uses regex extraction and sets `Level.id`, `Level.title` ("Maze Level " + levelNum), `Level.maxBlocks` (0 when meta says unlimited), `Level.allowLoops`, `Level.allowConditionals`, `Level.startOrientation` (from startDirection code).

### syncModel — xml

- **Shape**: Blockly workspace XML string (e.g. `<xml><block type="maze_moveForward">...</block></xml>`).
- **Java**: `BlockyUI.parseBlocklyXml` → `parseBlockElement` produces `List<Map<String, Object>>`; `GameEngine.rebuildProgram(data)` builds the EMF `Block` tree (MoveForward, Turn, RepeatUntilGoal, IfStatement) and sets `Level.solution`. Direction and condition parsing are case-insensitive.

## Java → WebView (apply level)

When loading from XMI (Model), Java does **not** call a bridge method from JS. It:

1. Sets `suppressSync = true`.
2. Builds grid JSON and injects it and other data into `window.__injectGridJson`, `window.__injectQd`, `window.__injectRd`, `window.__injectNd`, `window.__injectOd`, `window.__injectK`, `window.__injectOdVal`, `window.__injectT`, `window.__loadXml`.
3. Injects a poller that waits for `#svgMaze` and `BlocklyInterface`, then assigns `window.X = __injectGridJson`, updates `Qd`, `Rd`, `nd`, `od`, `K`, `Od`, `T`, clears `svgMaze` children, calls `Wd()`, recreates `#look` if missing, calls `BlocklyInterface.Kv(__loadXml)`, then `$d(false)` to reset pegman.
4. Sets `suppressSync = false` in a `finally` block.

So “Java → WebView” is done by writing to `window` and running a one-off script; the existing bridge methods are only for “WebView → Java”.

## Invariants to preserve

- **Exactly one START and one GOAL** in the grid when editing cells (GameEngine has `setUniqueCellType` for this).
- **Traces cleared** when the map or solution changes (already done in `setMapFromJson` and `rebuildProgram`).
- **No sync during apply**: `syncModel` must no-op when `suppressSync` is true.
- **Save once per Run**: `saveModel()` is called only at the end of `simulateUserProgram()`, not on every sync.
