# WebView sync — Agent documentation

This folder documents how the **Blockly Maze WebView** and the **Java/EMF game model** stay in sync, and how to extend the sync (e.g. **modify the game map via the UI**).

## Purpose and scope

- **Current behaviour**: The embedded Blockly Games Maze (in a JavaFX WebView) pushes **map grid**, **level metadata**, and **Blockly workspace XML** into Java via a `JSBridge`. Java runs simulation and saves/loads XMI. There is **no** UI in the WebView today that lets the user edit the maze grid and push those edits back to the model.
- **Target readers**: AI agents that need to **evolve the game** (e.g. add map editing in the WebView and sync edits to EMF) without breaking existing sync or save/load.

## Read this first

1. **Where the sync code lives**
   - **Java**: `blocky_game/src/blocky_game/BlockyUI.java` (JSBridge, `injectSyncScript`, `applyLevelToWebView`) and `blocky_game/src/blocky_game/GameEngine.java` (`setMapFromJson`, `syncLevelMeta`, `rebuildProgram`, `simulateUserProgram`).
   - **JS**: The sync logic is **injected** as a string in `BlockyUI.injectSyncScript()` and executed in the WebView after page load. The Blockly Maze app is in `blocky_game/src/blocky_game/blockly-games-web/`; key globals (e.g. `window.X`, `window.K`) are in the minified `maze/generated/en/compressed.js`.

2. **How to reason about sync**
   - **JS → Java**: Injected script calls `window.javaBridge.syncMap(...)`, `syncLevelMeta(...)`, `syncModel(...)`, and `runSimulation()`. Each bridge method updates the EMF model (e.g. `GameEngine.setMapFromJson`).
   - **Java → JS**: When loading from XMI (Model button), Java sets `suppressSync = true`, injects data via `window.__inject*` and a poller that overwrites `window.X`, `window.K`, etc., then redraws the maze and loads Blockly XML. After that, `suppressSync` is cleared. Any new sync from JS (e.g. map edits) must **not** run while Java is applying state, or use the same suppress pattern.

## Glossary

| Term | Meaning |
|------|--------|
| **Workspace XML** | Blockly’s serialisation of the block program (drag-and-drop blocks). Synced via `syncModel(xml)`. |
| **Map grid** | 2D array `window.X` in JS: `X[row][col]` with values 0=wall, 1=empty, 2=start, 3=goal. Synced via `syncMap(JSON.stringify(X))`. |
| **Level meta** | Level number, max blocks, start direction, allow loops/conditionals. Synced via `syncLevelMeta(metaJson)`. |
| **Run hook** | When the user clicks “Run Program”, Blockly hides `#runButton`. A MutationObserver detects that and calls `javaBridge.runSimulation()` after syncing map, meta, and workspace. |
| **suppressSync** | Java flag. When true, `JSBridge.syncModel` returns without updating the model so that applying loaded state from Java does not overwrite the model with stale WebView state. |

## Document index

| Document | Content |
|----------|---------|
| [01-current-sync-protocol.md](01-current-sync-protocol.md) | Bridge methods, payload formats, and EMF mapping (source of truth in code). |
| [02-extension-map-editing-via-ui.md](02-extension-map-editing-via-ui.md) | How to add UI-driven map editing: new bridge method, JS hooks, invariants, patch format, testing. |

When extending the game (e.g. map editing via UI), read **README.md** (this file) first, then **01-current-sync-protocol.md**, then **02-extension-map-editing-via-ui.md**.
