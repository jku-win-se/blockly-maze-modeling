# Blocky Maze

A **Java/JavaFX desktop application** that recreates the [Blockly Games Maze](https://blockly.games/maze) puzzle. Players drag-and-drop programming blocks to navigate a character (Pegman) through a grid-based maze from the Start cell to the Goal cell.

This project embeds a local copy of the [Blockly Games](https://github.com/maribethb/blockly-games) web app in a JavaFX WebView and syncs the maze state and block programs with an EMF-based model for simulation, persistence, and analysis.

## 🚀 Docker Quick Start (Easiest)

You can run the entire application, including the GUI and the MoMoT synthesis backend, using Docker. This is the fastest way to play the game without installing Java or IDEs.

### Windows (One-Click)
1.  Make sure you have **Docker Desktop** installed and running.
2.  Double-click **`run-game.bat`**.
3.  The game will open automatically in your browser at: **[http://localhost:6080](http://localhost:6080)**.

### Linux / macOS
1.  Make sure you have **Docker** and **Docker Compose** installed.
2.  Run the following in your terminal:
    ```bash
    ./run-game.sh
    ```
3.  Open your browser and navigate to: **[http://localhost:6080](http://localhost:6080)**.

---

## 🏗 Workspace structure

| Project        | Role                          | Technologies                    |
|----------------|-------------------------------|---------------------------------|
| **blocky_model** | EMF metamodel & generated code | Eclipse EMF, Ecore, XMI         |
| **blocky_game**  | Main application (UI + engine) | Java 17, JavaFX 21, WebView     |
| **blocky_momot** | Search-based program synthesis (optional) | Eclipse MOMoT, Henshin, EMF — see [blocky_momot/README.md](blocky_momot/README.md) |

- **blocky_model**: Domain model (Level, GridMap, Cell, Block types, ExecutionTrace) defined in `blocky.ecore`; Java API generated in `src-gen/`.
- **blocky_game**: Entry point `Main.java`; UI in `BlockyUI.java` (WebView + JSBridge); game/simulation logic in `GameEngine.java`. The WebView loads the Blockly Games Maze from `blocky_game/src/blocky_game/blockly-games-web/`.
- **blocky_momot**: MOMoT + Henshin search over Blocky XMI; depends on Eclipse plug-ins (use the shared [target platform](releng/blocky-modeling-2026-06.target) — see [INSTALL.md](INSTALL.md)).

See [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) for detailed architecture, JSBridge API, and data flow.

For WebView sync behavior and recent additions (Immediate Feedback overlays + debug controls), see:
- [docs/webview-sync/README.md](docs/webview-sync/README.md)
- [docs/webview-sync/03-immediate-feedback-debug.md](docs/webview-sync/03-immediate-feedback-debug.md)

Debug controls highlights:
- Java-driven `Step` / `Skip End` with deterministic start direction seeding for XMI-loaded models.
- Cumulative debug-path overlay during stepping (previous segments are preserved).
- Terminal-state handling: `Step`/`Skip End` auto-disable at `GOAL`/`CRASH`/`INFINITE_LOOP`.

**Installation:** For step-by-step setup in **Eclipse (with EMF, optional Henshin / Papyrus / MOMoT)** or **IntelliJ / any Maven-based IDE**, see **[INSTALL.md](INSTALL.md)** (including §1.2–1.3 for the modeling stack and [`releng/blocky-modeling-2026-06.target`](releng/blocky-modeling-2026-06.target)).

## Prerequisites

- **Java SE 17**
- **JavaFX**: either the trimmed SDK in the repo (`blocky_game/javafx-sdk-21.0.10/`) when using Eclipse, or **Maven** (see below) which pulls JavaFX from Maven Central.
- **Eclipse IDE** with EMF and PDE is required to edit the metamodel (`blocky.ecore`) or regenerate the EMF Java code (`blocky_model/src-gen/`). **blocky_momot** additionally needs Henshin, Papyrus, and MOMoT — easiest via the [target platform](releng/blocky-modeling-2026-06.target) documented in [INSTALL.md](INSTALL.md). For building and running **only** `blocky_game` + `blocky_model`, Maven is enough.

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

### Option C: Docker (Full Stack)

Run the entire application, including the GUI (via X11/Wayland forwarding) and the MOMoT synthesis backend, in a consistent containerized environment.

```bash
docker-compose build
docker-compose up
```

For detailed configuration instructions (X server setup for Windows/Linux/macOS, MOMoT synthesis commands), see **[DOCKER.md](DOCKER.md)**.

**Regenerating the EMF model (e.g. after editing `blocky.ecore`)** still requires Eclipse with EMF, or an EMF code-generation Maven plugin; the checked-in `blocky_model/src-gen/` is used by Maven as-is.

### ⚠️ IMPORTANT: No Manual Edits in `src-gen`
Files in `src-gen` directories (found in `blocky_model`, `blocky_momot`, etc.) are **automatically generated**. 
- **NEVER manually edit these files.** 
- If changes are needed, modify the source files (`.ecore`, `.momot`, etc.) and **rebuild/regenerate** in Eclipse.
- Manual changes in `src-gen` will be overwritten and lost.

## Direct Manipulation → MoMoT auto-run (solution synthesis)

Blocky supports **Direct Manipulation**: teleport Pegman to a chosen empty cell and treat that cell as an **intermediate goal** for synthesis.

- **DMG (Direct Manipulation Goal)** is represented in the model as `CellType.DMG` (distinct from the level’s `GOAL`).
- After a DM click, the app writes a request model to:
  - `blocky_game/direct_manipulation_request.xmi` (snapshot), and
  - `blocky_momot/model/input/direct_manipulation_request.xmi` (MoMoT input snapshot)
- MoMoT can be invoked automatically (when MoMoT is available on the runtime classpath). Progress is printed into the MoMoT panel, and solutions are listed from `blocky_momot/output*` (each run is saved under `blocky_momot/output_dm_*/`).

**Runtime notes (Java 17 / MoMoT):**
- MoMoT/MOEA may require: `--add-opens java.base/java.util=ALL-UNNAMED` (see [INSTALL.md](INSTALL.md)).
- Running MoMoT is typically done from **Eclipse with the modeling target platform** active (PDE + MoMoT bundles). Maven-only runs generally do not include MoMoT.

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
