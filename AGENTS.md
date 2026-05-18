# Agent Instructions

This repository contains **Blocky Maze**, a Java/JavaFX application integrating an EMF model with a Blockly web app via an embedded WebView.

## Architecture & Entry Points
- **JavaFX App**: `blocky_game/src/blocky_game/Main.java`
- **EMF Model**: `blocky_model/model/blocky.ecore` (metamodel) and `src-gen/` (generated API).
- **Blockly Web App**: Embedded in `blocky_game/src/blocky_game/blockly-games-web/`.
- **Sync Logic**: `BlockyUI.java` (JSBridge) and `GameEngine.java` (Logic).

## Critical Commands
- **Build**: `mvn clean compile` (run from root).
- **Run App**: `mvn -pl blocky_game javafx:run` (run from root).
- **Working Directory**: Must be `blocky_game` when running (Maven handles this via `-pl`). If running manually, `load.xmi` and assets are expected in the working directory.
- **EMF Codegen**: `src-gen/` is checked in. If you edit `.ecore`, you **must** regenerate code using Eclipse EMF (right-click `.genmodel` -> Generate Model Code). Maven only compiles existing code.

## Framework & Toolchain Quirks
- **JSBridge**: JavaFX WebView uses **weak references** for `window.javaBridge`. `BlockyUI` keeps a strong field reference `jsBridge` to prevent GC.
- **Case Sensitivity**: Blockly uses camelCase (e.g., `turnRight`). `GameEngine` methods like `rebuildProgram` and `parseCondition` use `.toLowerCase()` for robust matching—preserve this.
- **WebView Sync**: Injected JS in `BlockyUI.injectSyncScript` polls for workspace and hooks the "Run" button via `MutationObserver`.
- **Sync Safety**: Use the `suppressSync` flag in `BlockyUI` when applying state from Java to WebView to prevent stale JS state from overwriting the model during the update.
- **Deterministic Simulation**: `GameEngine.simulateUserProgram` relies on `determineStartOrientation`. If not explicitly set in EMF, it infers it from the `START` cell's neighbors.

## Modeling (Henshin & MOMoT)
- **Henshin Rules**: Rules are written in `.henshin_text` (textual) but MOMoT requires `.henshin` (XMI).
- **Rule Compilation**: Right-click `.henshin_text` -> **Transform to Henshin** in Eclipse.
- **NSURI Patching**: After compiling `.henshin`, ensures the EPackage URI is `http://www.example.org/blocky#` instead of a relative path to `.ecore`.
- **MOMoT Config**: `.momot` files must register the EMF package in `initialization` and `model.adapt`.

## File Storage
- **load.xmi / save.xmi**: Default locations for model persistence in `blocky_game/`.
- **direct_manipulation_request.xmi**: Written when the user clicks a cell in the WebView to trigger synthesis.

## Verification
- No automated Java tests are currently implemented.
- **Manual Verification**: Run the app, load a level, click "Run Program", and verify the execution trace appears in the console/output.
- **Linter/Checkstyle**: None configured in Maven; follow existing Eclipse/Java conventions (4-space indent).
