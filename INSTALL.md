# Blocky Maze — Installation guide

This guide walks you through setting up and running Blocky Maze in **Eclipse (with EMF)** or in **any other Java IDE** (e.g. IntelliJ, VS Code) using Maven. It also covers the **Eclipse modeling stack** (Henshin, Papyrus, MoMoT) used by the **`blocky_momot`** plug-in project.

---

## Prerequisites (all setups)

- **Java SE 17** (JDK 17).  
  Check: `java -version` and `javac -version` show 17.
- **Git** (to clone the repo, if needed).

---

## 1. Eclipse with EMF

Use this path if you want to **edit the EMF metamodel** (`blocky.ecore`) or **regenerate** the model code in `blocky_model/src-gen/`. Eclipse also works for “run only” without Maven.

For **search-based synthesis** (`blocky_momot`), use **Eclipse Modeling Tools** (or equivalent) and either the **shared target platform** (recommended) or the manual update-site steps in §1.2.

### 1.1 Install Eclipse IDE

1. Download an Eclipse package that includes **EMF** and **PDE (Plug-in Development Environment)**:
   - **Recommended (full workspace):** [Eclipse Modeling Tools](https://www.eclipse.org/downloads/packages/) — includes EMF and modeling components; matches how Henshin and Papyrus are tested.
   - **Minimal:** **Eclipse IDE for RCP and RAP Developers** or **Java Developers**, then install EMF and PDE via **Help → Install New Software** (your release site, e.g. `https://download.eclipse.org/releases/2026-06/`).
2. Install and launch Eclipse. Use a dedicated **workspace directory** (e.g. `eclipse-workspace-blocky`).

### 1.2 Eclipse modeling stack — target platform (recommended)

The repo defines a **Target Platform** that pulls in the **Eclipse simultaneous release**, **Papyrus for UML** (from the dedicated **Papyrus-Desktop** repository, not only SimRel), **Henshin SDK**, and **MoMoT 2.0**. This avoids missing-dependency errors (Henshin’s SDK requires Papyrus GMF infrastructure such as `org.eclipse.papyrus.infra.gmfdiag.tooling.runtime`).

**Target file:** [`releng/blocky-modeling-2026-06.target`](releng/blocky-modeling-2026-06.target) (aligned with **Eclipse 2026-06**; adjust the release URL inside the file if your team standardizes on another train).

**Steps:**

1. **File → Open File…** → select `releng/blocky-modeling-2026-06.target`.
2. In the editor, click **Set as Active Target Platform** (or **Reload Target Platform** after changes).
3. Wait for p2 to resolve and the workspace to rebuild.

**Repositories referenced by the target (for reference):**

| What | Update site |
|------|-------------|
| Eclipse SDK | `https://download.eclipse.org/releases/2026-06/` |
| **Papyrus for UML** (Papyrus-Desktop) | `https://download.eclipse.org/modeling/mdt/papyrus/papyrus-desktop/updates/releases/2025-06/` ([directory listing](https://download.eclipse.org/modeling/mdt/papyrus/papyrus-desktop/updates/releases/2025-06/)) |
| Henshin SDK | `https://download.eclipse.org/modeling/emft/henshin/updates/release` |
| MoMoT 2.0 (develop) | `https://jku-win-se.github.io/MOMoT2/eclipse/updates/latest/develop/` |

**Note:** On milestone builds (e.g. 2026-06 M1), **`org.eclipse.papyrus.uml.feature.feature.group` often does not appear** on the main `releases/2026-06` composite. The target uses the official [Papyrus-Desktop updates](https://download.eclipse.org/modeling/mdt/papyrus/papyrus-desktop/updates/releases/2025-06/) composite (versions such as 7.0.0 / 7.1.0) instead.

MoMoT p2 metadata lives on the [`main` branch of MOMoT2](https://github.com/jku-win-se/MOMoT2/tree/main/docs/eclipse/updates/latest/develop) under `docs/eclipse/` and is served via GitHub Pages.

If a unit still fails to resolve, open the target in the **Target Definition** editor, use **Add…** on the **Papyrus-Desktop** repository above, and re-select **Papyrus for UML** so Eclipse writes the correct feature id. For a newer Eclipse train, check [Papyrus downloads](https://eclipse.dev/papyrus/download.html) for an updated `papyrus-desktop/updates/releases/…` URL and replace that location in the `.target` file.

### 1.3 Manual install (without using the `.target` file)

If you prefer installing into the running IDE instead of a target platform:

1. **Help → Install New Software…** → **Manage…** — ensure your **Eclipse release** repository is enabled (e.g. `https://download.eclipse.org/releases/2026-06/`).
2. Add **Papyrus-Desktop** if **Papyrus for UML** is missing from the release site: **Add…** → `https://download.eclipse.org/modeling/mdt/papyrus/papyrus-desktop/updates/releases/2025-06/` ([repo](https://download.eclipse.org/modeling/mdt/papyrus/papyrus-desktop/updates/releases/2025-06/)). Install **Papyrus for UML** (category *Papyrus Desktop*) **before** Henshin, so `org.eclipse.papyrus.infra.gmfdiag.tooling.runtime` is present.
3. Add **Henshin:** site [Henshin release updates](https://wiki.eclipse.org/Henshin/Installation_instructions) — install **Henshin SDK** (not only optional extras). Optionally enable **“Contact all update sites during install…”**.
4. Add **MoMoT 2.0:** `https://jku-win-se.github.io/MOMoT2/eclipse/updates/latest/develop/` — install the MoMoT features your workspace needs (typically **MoMoT** + **MoMoT Lang**).

Then import **`blocky_momot`** and **`blocky_model`** (see §1.6) and follow [blocky_momot/README.md](blocky_momot/README.md) for Henshin text compilation and runs.

### 1.4 Install EMF (if not already present)

If you did **not** use **Eclipse Modeling Tools** or the **target platform** in §1.2 (which already include EMF):

1. In Eclipse: **Help → Eclipse Marketplace** (or **Help → Install New Software**).
2. **Option A — Marketplace:** Search for **“EMF”** or **“Eclipse Modeling Framework”**, install **EMF - Eclipse Modeling Framework** (and any dependencies it proposes). Accept the licenses and finish.
3. **Option B — Install from update site:**  
   **Help → Install New Software → Add…**  
   - Name: `EMF`  
   - Location: `https://download.eclipse.org/modeling/emf/emf/updates/releases/`  
   Choose **EMF** (and **Ecore**, **XMI** if listed). Next → Finish.

If the project is an Eclipse **plug-in project** (has `META-INF/MANIFEST.MF`, `build.properties`), ensure **PDE (Plug-in Development Environment)** is installed: **Help → Install New Software** → select your Eclipse release → **General Purpose Tools → Eclipse Plug-in Development Environment**.

### 1.5 JavaFX SDK (for Eclipse)

The app needs JavaFX at runtime. The repo includes a **trimmed** JavaFX SDK under `blocky_game/javafx-sdk-21.0.10/` (only the modules used). If that folder is missing:

1. Download **JavaFX 21 SDK** (e.g. from [gluonhq.com/products/javafx](https://gluonhq.com/products/javafx/)).
2. Unzip it and place the **entire** folder (e.g. `javafx-sdk-21.0.10`) inside the **blocky_game** project directory so that the path `blocky_game/javafx-sdk-21.0.10/lib` exists.

### 1.6 Import the projects into Eclipse

1. **File → Open Projects from File System…** (or **File → Import… → Existing Projects into Workspace**).
2. Set **Directory** to the **repository root** (the folder that contains `blocky_model`, `blocky_game`, and optionally `blocky_momot`).
3. Select **blocky_model** and **blocky_game** (and **blocky_momot** if you use MOMoT). Finish.
4. If Eclipse does not detect them as projects: use **File → Import… → Existing Projects into Workspace** and browse to the same root; select the projects you need.

### 1.7 Build order and EMF code generation

1. **blocky_model** must be built first (it contains the EMF-generated code used by **blocky_game**).
2. If you changed `blocky_model/model/blocky.ecore`:  
   - Right‑click **blocky_model** → **Generate Model Code** (or use the *.genmodel if present).  
   - Otherwise, the existing `blocky_model/src-gen/` is sufficient.
3. **Project → Build Project** (or leave “Build automatically” on) so that **blocky_game** compiles against **blocky_model**.
4. If **blocky_momot** is imported: resolve the target platform (§1.2) first, then build **blocky_momot** after **blocky_model** (it depends on the `blocky` plug-in and MoMoT/Henshin).

### 1.8 Run configuration (VM arguments for JavaFX)

1. Right‑click **blocky_game** → **Run As → Java Application** and choose **Main** (class `blocky_game.Main`).  
   If the window fails to open or you see “JavaFX runtime components are missing”, add VM arguments:
2. **Run → Run Configurations…** → select your **blocky_game** / **Main** configuration.
3. Open the **Arguments** tab. In **VM arguments**, set:
   ```text
   --module-path "${project_loc}/javafx-sdk-21.0.10/lib" --add-modules javafx.controls,javafx.web
   ```
   (`project_loc` is an Eclipse variable for the project directory, i.e. `blocky_game`.)

4. If you run **MoMoT** from inside the game (Direct Manipulation → auto-run), also add:
   ```text
   --add-opens java.base/java.util=ALL-UNNAMED
   ```
   This is required because MoMoT/MOEA uses reflection into `java.util` internals on Java 17+.
5. **Apply** → **Run**.

The Blockly Maze window should open. Use the level bar for levels 1–10 or **Model** to load/save XMI (`load.xmi` / `save.xmi`).

### 1.9 Optional: Share launch configs

The run configuration is stored under `.metadata/.plugins/org.eclipse.debug.core/.launches/`. To share the same VM args with others, either commit that folder (if you version Eclipse metadata) or document the VM arguments (as above) in this guide or in the [README](README.md).

---

## 2. IntelliJ IDEA (or other Maven-based IDEs)

Use this path to **build and run without Eclipse**: Maven resolves **EMF** and **JavaFX** from Maven Central. No local JavaFX SDK and no EMF installation are required. This also applies to **VS Code** (with Java + Maven extensions), **NetBeans**, or any IDE that supports Maven.

### 2.1 Requirements

- **JDK 17** (IntelliJ: **File → Project Structure → Project → SDK 17**).
- **Maven** (IntelliJ usually bundles it; otherwise install [Maven](https://maven.apache.org/download.cgi) and point IntelliJ to it under **Settings/Preferences → Build, Execution, Deployment → Build Tools → Maven**).

### 2.2 Import the project

1. **File → Open** (or **Open** on the welcome screen).
2. Select the **repository root** (the folder that contains `pom.xml`, `blocky_model`, and `blocky_game`).
3. Choose **Open as Project** and accept **Maven** when IntelliJ asks how to open it.
4. Wait for Maven to import and index (bottom right). All EMF and JavaFX dependencies will be downloaded automatically.

### 2.3 Build

- **Build → Build Project**, or  
- Open the **Maven** tool window (**View → Tool Windows → Maven**), expand **blocky-maze-parent → Lifecycle**, and double‑click **compile**.

From the command line (repo root):

```bash
mvn clean compile
```

### 2.4 Run the application

**Option A — Maven (recommended)**

1. Open the **Maven** tool window.
2. Expand **blocky_game → Plugins → javafx**.
3. Double‑click **javafx:run**.

Or from the terminal (repo root):

```bash
mvn -pl blocky_game javafx:run
```

**Option B — IntelliJ Run Configuration**

1. **Run → Edit Configurations…** → **+** → **Application**.
2. **Name:** e.g. `Blocky Maze`.
3. **Module:** `blocky_game` (or leave unset and set classpath below).
4. **Main class:** `blocky_game.Main`.
5. **Working directory:** set to the **blocky_game** module directory (e.g. `$MODULE_WORKING_DIR$` or the full path to `blocky_game`).  
   This is required so the app finds `load.xmi`, `save.xmi`, and `src/blocky_game/blockly-games-web/maze.html`.
6. **VM options:**  
   ```text
   --module-path "${MAVEN_REPOSITORY}/org/openjfx/javafx-base/21.0.1/javafx-base-21.0.1.jar;${MAVEN_REPOSITORY}/org/openjfx/javafx-graphics/21.0.1/javafx-graphics-21.0.1.jar;${MAVEN_REPOSITORY}/org/openjfx/javafx-controls/21.0.1/javafx-controls-21.0.1.jar;${MAVEN_REPOSITORY}/org/openjfx/javafx-web/21.0.1/javafx-web-21.0.1.jar" --add-modules javafx.controls,javafx.web
   ```  
   (On macOS/Linux use `:` instead of `;` in the module path.)  
   Or, if you use the **JavaFX SDK** in `blocky_game/javafx-sdk-21.0.10/lib`:  
   ```text
   --module-path "path/to/blocky_game/javafx-sdk-21.0.10/lib" --add-modules javafx.controls,javafx.web
   ```
7. **Apply** → **OK**, then run.

Using **javafx:run** (Option A) avoids manual module path setup; the plugin sets it for you.

### 2.5 Regenerating the EMF model (optional)

The generated code in `blocky_model/src-gen/` is committed. If you need to **regenerate** it after editing `blocky.ecore`, you must either:

- Use **Eclipse with EMF** (see Section 1), generate there, then commit the updated `src-gen/`, or  
- Add an EMF code-generation Maven plugin to `blocky_model` and run that goal.

Normal build and run in IntelliJ do **not** require Eclipse or EMF to be installed.

---

## 3. Quick reference

| Goal                    | Eclipse (EMF)                          | IntelliJ / other IDE (Maven)        |
|-------------------------|----------------------------------------|-------------------------------------|
| Build                   | Build **blocky_model**, then **blocky_game** (and **blocky_momot** if used) | `mvn clean compile`                  |
| Run game                | Run **Main** with VM args §1.8         | `mvn -pl blocky_game javafx:run` or Run **Main** with module path |
| MOMoT / Henshin tooling | Target §1.2 or manual §1.3; see [blocky_momot/README.md](blocky_momot/README.md) | Not supported via this repo’s Maven layout |
| Edit metamodel          | Yes (EMF editor, regenerate code)      | Edit `.ecore` only; regenerate in Eclipse or via Maven plugin |
| JavaFX / EMF from net   | JavaFX: local SDK in `blocky_game/…`  | Maven pulls JavaFX + EMF from Maven Central |

For more on the project structure and run options, see the [README](README.md). For architecture and **blocky_momot**, see [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md).
