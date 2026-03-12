# Extension: map editing via UI

This document describes how an agent can add **game map editing in the WebView** and sync those edits to the EMF model, so that “modify the game map via UI” is a supported evolution of the game.

## Goal

- **User action**: The user edits the maze grid in the WebView (e.g. clicks on a cell to cycle wall/empty/start/goal).
- **Effect**: The WebView’s grid state is pushed to Java; the EMF `GridMap` and `Cell` instances are updated; invariants (exactly one START, one GOAL) are preserved; execution traces are cleared; and optionally the visual grid in the WebView is updated if not already driven by the same edit.

## Where to implement

### Java side

- **File**: `blocky_game/src/blocky_game/BlockyUI.java`.
- **Add a new JSBridge method** that receives the updated grid from JS and forwards it to the engine. Two options:
  - **Full grid**: `void setMap(String mapJson)` — same payload as `syncMap` (2D array JSON). Implementation: call `engine.setMapFromJson(mapJson)`. Reuses existing logic and clears traces.
  - **Single-cell patch**: `void applyMapEdit(String patchJson)` — payload e.g. `{"x":col,"y":row,"type":0|1|2|3}`. Implementation: get current map from engine, apply one cell change ensuring uniqueness of START/GOAL (e.g. call `GameEngine.cycleCellType(x,y)` or a new method that sets one cell and clears other START/GOAL if needed), then clear traces.
- **GameEngine** already has `cycleCellType(int x, int y)` and `setUniqueCellType(Cell, CellType)`; a new bridge method can call these or a thin wrapper that accepts (x, y, type) and enforces invariants.

### JS side (injected script)

- **Where**: The script is built as a string in `BlockyUI.injectSyncScript(WebEngine)`. To add map editing, either:
  - Append a second script that adds click (or context) handlers to the maze SVG, or
  - Extend the single injected script with a block that, after the maze is ready (e.g. when `#svgMaze` and `BlocklyInterface` exist), attaches listeners to the maze cells (or to a transparent overlay).
- **Behaviour on cell click**: Map the click coordinates to (row, col), compute the new cell type (e.g. cycle: empty → wall → start → goal → empty), update `window.X[row][col]`, then either:
  - Call `javaBridge.syncMap(JSON.stringify(window.X))` (full grid sync), or
  - Call `javaBridge.applyMapEdit(JSON.stringify({ x: col, y: row, type: newType }))` if that method exists.
- **Visual update**: Blockly Maze typically redraws the maze from `window.X` via a function like `Wd()`. After changing `window.X`, call the redraw so the user sees the new state. If the redraw is not exposed, full grid sync plus a page/level reload is a fallback (heavier).

## Invariants

- **Exactly one START and one GOAL**: Use the same logic as `GameEngine.setUniqueCellType` — when setting a cell to START or GOAL, clear that type from all other cells first.
- **Clear execution traces** after any map change: `setMapFromJson` already does this; for a patch-based API, the Java handler must clear `currentLevel.getTraces()`.
- **Avoid feedback loops**: Do not trigger sync while Java is applying level to WebView (`suppressSync`). Map edits are user-driven from JS, so they occur when the user clicks; ensure the click handler does not run during the apply-level poller (e.g. do not call bridge from the same script that applies `__inject*` until after apply is done).
- **Coordinate system**: JS uses `window.X[row][col]` (row-major); Java `Cell` has `x` (column) and `y` (row). When passing (x, y) in JSON, document whether they are (col, row) to match EMF; the recommended patch format below uses (x, y) as (col, row) to match `Cell.getX()`/`getY()`.

## Recommended patch format (incremental edit)

If implementing a single-cell edit bridge method, use a JSON object with integer fields:

```json
{ "x": 2, "y": 1, "type": 3 }
```

- **x**: column index (same as `Cell.x`).
- **y**: row index (same as `Cell.y`).
- **type**: 0 = wall, 1 = empty, 2 = start, 3 = goal.

The Java handler should: resolve the cell at (x, y), set its type while enforcing at most one START and one GOAL (e.g. call `setUniqueCellType` when type is 2 or 3), then clear `currentLevel.getTraces()`.

## Testing and verification checklist

- After implementing the new bridge method and JS hook:
  - **Logs**: Confirm in console that the bridge method is called with the expected payload when a cell is edited in the UI.
  - **Model**: Load or inspect the model after edit; the corresponding `Cell` in `currentLevel.getMap().getCells()` has the new `CellType`; START and GOAL each appear exactly once.
  - **Traces**: After an edit, `currentLevel.getTraces()` is empty (or previous traces are cleared).
  - **Run**: After editing the map, “Run Program” still runs simulation and saves; the saved XMI contains the updated map and no dangling references.
  - **Load Model**: Load an XMI that had a custom map; then edit the map in the WebView and sync; then save. Reload and confirm the edited state is persisted.
- If the WebView redraws the maze from `window.X`, confirm that after a full-grid sync the visible grid matches the EMF model.
