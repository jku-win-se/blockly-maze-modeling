# Immediate Feedback and Debug Controls

This document describes the current implementation of:

- **Immediate feedback overlays** (old trace vs new simulated trace when loading XMI)
- **Java-driven debug controls** in the WebView (`Pause/Resume`, `Stop`, `Step`, `Skip End`)

It is intended for contributors/agents extending `blocky_game` without breaking sync behavior.

## Where the code lives

- `blocky_game/src/blocky_game/ImmediateFeedbackService.java`
  - Computes old/new path overlays for loaded models.
- `blocky_game/src/blocky_game/DebuggingService.java`
  - Dedicated debugger helpers (trace computation from arbitrary state, debug overlay snippet).
- `blocky_game/src/blocky_game/GameEngine.java`
  - Stores immediate-feedback paths.
  - Holds debugger session state and step/tick methods.
- `blocky_game/src/blocky_game/BlockyUI.java`
  - Injects JS bridge hooks and debug buttons into WebView.
  - Applies map/model state and overlay rendering in WebView.

## Immediate feedback (XMI load)

### Goal

When loading an XMI model, show:

- **Past path**: execution trace stored in the loaded model
- **New path**: re-simulated trace from the currently loaded solution

with distinct colors so changes are visible without manual comparison.

### Flow

1. `BlockyUI` loads XMI via `GameEngine.loadFromFile(...)`.
2. `GameEngine` calls `ImmediateFeedbackService.computePaths(level)`.
3. Engine exposes paths via `getPastPath()` / `getNewPath()`.
4. `BlockyUI.applyLevelToWebView(...)` injects:
   - `window.__injectPastPath`
   - `window.__injectNewPath`
5. Injected JS draws SVG overlays:
   - `pathPast` (red)
   - `pathNew` (green)

### Notes

- `ImmediateFeedbackService` compresses consecutive duplicate positions (turn-only states).
- If a trace is sparse/missing, overlays degrade gracefully to start-cell markers.

## Debug controls

### Goal

Enable step-by-step program debugging directly in WebView:

- `Pause/Resume`
- `Stop`
- `Step` (one state then pause)
- `Skip End` (jump to final state: GOAL/CRASH/INFINITE_LOOP heuristic)

Users can edit blocks while paused; next resume/step recomputes from current state.

### Important design choices

- Debugging is **Java-driven**, not WebView-interpreter-driven.
- During debug stepping, JS sync only updates **program XML**.
  - It intentionally avoids `syncMap` / `syncLevelMeta` to prevent orientation instability from feeding `window.T` back into model metadata.
- Auto-run click after injection is disabled when debug controls are active.
- Run-button observer ignores style changes while a debug session is active.
- On XMI loads, debug start direction seeds from model start orientation (`window.__modelStartT`) instead of Maze reset defaults.

### Engine session state

`GameEngine` maintains:

- `debugTrace`
- `debugIndex`
- `debugPaused`
- `debugDirtySolution`
- current/start `(x,y,dir)` snapshots

Primary methods:

- `debugStart(q,s,t)`
- `debugTogglePause()`
- `debugStepOnce()`
- `debugTick()`
- `debugStop()`
- `debugSkipToEnd()`

`debugFrameJson()` returns UI frame data:

- current position/orientation
- current prefix path
- paused flag
- result status (`RUNNING`, `GOAL`, `CRASH`, `INFINITE_LOOP`)

### WebView behavior

Buttons are injected only by:

- default/page load path (`injectDebugControls(...)`)

`applyLevelToWebView(...)` does not create duplicate debug bindings.

Debug overlay is rendered via `DebuggingService.renderDebugOverlayJsSnippet()`.

Current behavior:

- removes green immediate-feedback overlay (`pathNew`) while debugging
- draws a **cumulative** path for all debug steps in the current prefix
- highlights current cell
- disables `Step`/`Skip End` when a terminal frame is reached (`GOAL`, `CRASH`, `INFINITE_LOOP`)
- uses a step in-flight lock so rapid clicking cannot queue overlapping step calls

## Known caveats

- JavaFX `executeScript(...)` uses large concatenated JS strings; syntax errors can break all injected behavior.
- If adding new JS comments inside injected strings, avoid `//` in single-line contexts; prefer block comments.
- Maven may not be installed in all dev environments; use IDE build diagnostics as fallback.
- WebView script injections are wrapped in Java-side try/catch logging; check `[BlockyUI] inject* failed` lines when load/debug UI appears broken.

## Regression checklist

1. Load default level (no XMI) -> debug buttons appear.
2. Load model from XMI -> immediate feedback red/green overlays appear.
3. Start debugging -> green path is cleared, debug segment appears.
4. On XMI-loaded levels, `debugStart` uses model start orientation (no forced EAST reset).
5. `Step` changes exactly one trace state and pauses.
6. Rapidly clicking `Step` does not skip visual updates or enqueue parallel steps.
7. `Skip End` jumps to final state and pauses.
8. At terminal result, `Step`/`Skip End` are disabled with terminal tooltip.
9. While paused, edit blocks -> next `Step/Resume` uses updated logic from current state.

