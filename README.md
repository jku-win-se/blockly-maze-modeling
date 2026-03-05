# Blocky Maze

A **Java/JavaFX desktop application** that recreates the [Blockly Games Maze](https://blockly.games/maze) puzzle. Players drag-and-drop programming blocks to navigate a character (Pegman) through a grid-based maze from the Start cell to the Goal cell.

This project embeds a local copy of the [Blockly Games](https://github.com/maribethb/blockly-games) web app in a JavaFX WebView and syncs the maze state and block programs with an EMF-based model for simulation, persistence, and analysis.

## Workspace structure

| Project        | Role                          | Technologies                    |
|----------------|-------------------------------|---------------------------------|
| **blocky_model** | EMF metamodel & generated code | Eclipse EMF, Ecore, XMI         |
| **blocky_game**  | Main application (UI + engine) | Java 17, JavaFX 21, WebView     |

- **blocky_model**: Domain model (Level, GridMap, Cell, Block types, ExecutionTrace) defined in `blocky.ecore`; Java API generated in `src-gen/`.
- **blocky_game**: Entry point `Main.java`; UI in `BlockyUI.java` (WebView + JSBridge); game/simulation logic in `GameEngine.java`. The WebView loads the Blockly Games Maze from `blocky_game/src/blocky_game/blockly-games-web/`.

See [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) for detailed architecture, JSBridge API, and data flow.

**Installation:** For step-by-step setup in **Eclipse (with EMF)** or **IntelliJ / any Maven-based IDE**, see **[INSTALL.md](INSTALL.md)**.

## Prerequisites

- **Java SE 17**
- **JavaFX**: either the trimmed SDK in the repo (`blocky_game/javafx-sdk-21.0.10/`) when using Eclipse, or **Maven** (see below) which pulls JavaFX from Maven Central.
- **Eclipse IDE** with EMF and PDE is only required if you want to edit the metamodel (`blocky.ecore`) or regenerate the EMF Java code (`blocky_model/src-gen/`). For building and running, Maven is enough.

## How to run

### Option A: Maven (no Eclipse, no local JavaFX SDK)

All EMF and JavaFX dependencies are resolved from Maven Central. You only need **Java 17** and **Maven**.

```bash
# From the repo root
mvn clean compile
mvn -pl blocky_game javafx:run
```

The app runs with working directory `blocky_game`, so `load.xmi` / `save.xmi` and `src/blocky_game/blockly-games-web/maze.html` are found there.

- **EMF** is pulled in as: `org.eclipse.emf:org.eclipse.emf.ecore`, `org.eclipse.emf.common`, `org.eclipse.emf.ecore.xmi` (version 2.35.0).
- **JavaFX** is pulled in as: `org.openjfx:javafx-controls`, `org.openjfx:javafx-web` (version 21.0.1).

### Option B: Eclipse

1. Open the workspace in **Eclipse** (with EMF and JavaFX support).
2. Build the **blocky_model** project (EMF code generation, if you changed the metamodel).
3. Run **blocky_game** → `blocky_game.Main` as a Java Application.
4. The launch configs use:
   ```text
   --module-path "${project_loc}/javafx-sdk-21.0.10/lib" --add-modules javafx.controls,javafx.web
   ```

The window shows the Blockly Maze in a WebView. Use the level bar for levels 1–10 or **Model** to load/save from XMI (`load.xmi` / `save.xmi`).

**Regenerating the EMF model (e.g. after editing `blocky.ecore`)** still requires Eclipse with EMF, or an EMF code-generation Maven plugin; the checked-in `blocky_model/src-gen/` is used by Maven as-is.

## Licenses

**This project is licensed under the [Apache License, Version 2.0](LICENSE).**

You may use, modify, and distribute this code publicly under the terms of the Apache 2.0 license. Third-party components used by this project are under the following licenses:

| Component        | Location / use                    | License |
|-----------------|------------------------------------|---------|
| **Blockly Games** | Embedded in `blocky_game/.../blockly-games-web/` | [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) — [maribethb/blockly-games](https://github.com/maribethb/blockly-games) (based on Google Blockly Games) |
| **JavaFX 21**   | Runtime (e.g. `javafx-sdk-21.0.10`) | [GPL v2 with Classpath Exception](https://openjdk.org/legal/gplv2+ce.html) — [OpenJFX](https://openjfx.io/) |
| **Eclipse EMF** | Metamodel & generated code (blocky_model) | [Eclipse Public License 2.0 (EPL-2.0)](https://www.eclipse.org/legal/epl-2.0/) |

- **Apache 2.0** and **EPL 2.0** allow use, modification, and distribution with appropriate notices.
- **JavaFX (GPL v2 + Classpath Exception)** allows linking this application with JavaFX without requiring this project to be licensed under GPL; the exception applies when the application is used as a “library” in the sense of the exception.

Attribution and license texts for these components are included in this repository where applicable (e.g. JavaFX legal notices under `blocky_game/javafx-sdk-21.0.10/legal/`). See also [NOTICE](NOTICE).
