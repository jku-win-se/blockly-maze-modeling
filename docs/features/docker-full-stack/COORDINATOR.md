# COORDINATOR — Docker Full Stack Run
> **You are the Coordinator agent.** A human triggered you by saying
> *"execute COORDINATOR.md"* (or similar) inside `docs/features/docker-full-stack/`.
> Implement this feature end-to-end and prove it works by walking the phases
> in this folder in order. Do not deviate. Do not skip the acceptance gates.

---

## 0. Mission

Provide a complete Docker-based environment for the Blocky Maze application, including both the JavaFX frontend (via X11/Wayland forwarding) and the MOMoT synthesis backend. This ensures a consistent, reproducible environment for development and search-based synthesis, leveraging the MOMoT 2.0 Docker repository as a reference. Success is a fully functional application running from within a container.

**Hard constraints (non-negotiable):**
- Must support JavaFX 21 GUI rendering (host display connection).
- Must include MOMoT 2.0 dependencies and search capabilities.
- Must preserve access to local model files (`load.xmi`, `save.xmi`) via volume mounts.
- House rules from `AGENTS.md` apply (see § 3).

---

## 1. How to "trigger" me

1. Read this file fully.
2. Read `README.md` and `PROGRESS.md`.
3. Start (or resume) at the first phase whose checkbox in `PROGRESS.md` is
   still unchecked.
4. For each phase folder, open its `README.md` and follow Steps →
   Deliverables → Acceptance Gate.
5. After completing a phase, tick its boxes in `PROGRESS.md`. Do NOT proceed
   past a failing gate.
6. After the last phase, run the **Final Verification Loop** (§ 4).

---

## 2. Phase order (strictly top-to-bottom)

| # | Folder | Goal | Exit gate |
|---|--------|------|-----------|
| 01 | `docker-infra` | Create Dockerfile and docker-compose.yml | `docker-compose build` exits 0 |
| 02 | `gui-display-config` | Configure X11/Wayland forwarding for JavaFX | JavaFX "Hello World" renders on host |
| 03 | `momot-integration` | Incorporate MOMoT 2.0 Docker-based repo patterns | MOMoT search runs inside container |
| 04 | `verification` | Full system check (App + Synthesis) | `docker-compose up` leads to successful run/synthesis |
| 05 | `rollout` | Documentation and PR body | `PR-BODY.md` exists and docs are updated |

---

## 3. House rules

These come from `AGENTS.md` and `README.md` / `INSTALL.md`:

- **Build gate (mandatory):** `mvn -q clean compile` from the repo root must
  exit 0 before any commit. The repo has **no automated test suite** today,
  so the build is the only machine gate — do not skip it.
- **Run command:** `mvn -pl blocky_game javafx:run` (working directory is
  forced to `blocky_game/` by Maven; `load.xmi`, `save.xmi`, and the
  embedded Blockly web app are resolved relative to it).
- **EMF codegen is manual:** Maven only compiles existing `src-gen/`. If
  you edit `blocky_model/model/blocky.ecore`, you **must** regenerate via
  Eclipse: right-click `blocky_model/model/blocky.genmodel` → **Generate
  Model Code**, then commit the regenerated `blocky_model/src-gen/`.
- **JSBridge GC-safety:** JavaFX WebView holds only a weak reference to
  `window.javaBridge`. `BlockyUI` keeps a strong field `jsBridge` to prevent
  collection — do not remove or rename it.
- **Sync discipline:** When applying state from Java to the WebView (e.g.
  loading an XMI), set `suppressSync = true` in `BlockyUI` first and clear
  it only after the WebView state is consistent, to prevent stale JS state
  from clobbering the model.
- **Case-insensitive Blockly matching:** Blockly emits camelCase
  (`turnRight`, `isPathLeft`); `GameEngine.rebuildProgram` and
  `parseCondition` must keep `.toLowerCase()` before matching — do not
  hand-roll case-sensitive comparisons.
- **Deterministic starts:** `GameEngine.determineStartOrientation` uses
  `Level.startOrientation` when `eIsSet` is true, otherwise infers from the
  `START` cell's non-wall neighbour. Preserve this fallback.
- **Loop cap:** simulation step cap is `width × height × 2` (do not
  reintroduce a hard-coded 50).
- **Henshin compilation:** rules live in `.henshin_text` (textual), but
  MOMoT requires `.henshin` (XMI). Generate via Eclipse: right-click
  `.henshin_text` → **Transform to Henshin**. After compilation, ensure
  the EPackage URI in the produced `.henshin` is exactly
  `http://www.example.org/blocky#` (NSURI patch), not a relative path to
  the `.ecore`.
- **MOMoT config:** `.momot` files must register the EMF package in
  `initialization` and `model.adapt`; MOEA/MOMoT runs on Java 17 may need
  `--add-opens java.base/java.util=ALL-UNNAMED` (see `INSTALL.md`).
- **Eclipse modeling stack:** for `blocky_momot`, use the shared target
  platform `releng/blocky-modeling-2026-06.target` (Eclipse 2026-06 +
  Papyrus + Henshin SDK + MoMoT 2.0). Do not silently drift it.
- **Reuse first:** no new Maven dependencies without justification — the
  existing `pom.xml` already provides EMF (`org.eclipse.emf.*` 2.39.x) and
  JavaFX (`org.openjfx:javafx-controls`, `javafx-web` 21.x). Do not add a
  redundant dep.
- **Java conventions:** 4-space indent, no checkstyle/linter configured;
  follow the style already in `BlockyUI.java` / `GameEngine.java`.

- **Docker specific:** The container MUST have access to the X server (Linux) or XQuartz/VcXsrv (Windows/macOS).
- **MOMoT 2.0 Repo:** Reference https://github.com/hadiDHD/momot-2.0 for dependency alignment in the Dockerfile.

---

## 4. Final Verification Loop

```bash
# Build (mandatory)
mvn -q clean compile

# Build the docker image
docker-compose build

# Run the app in docker
# (Note: Requires local X11 server running and DISPLAY set)
docker-compose up
```

The feature is **DONE** when:

- [ ] Every phase folder has every box ticked.
- [ ] `PROGRESS.md` shows 100%.
- [ ] `mvn -q clean compile` exits 0 from the repo root.
- [ ] The application launches successfully from `docker-compose up` and the JavaFX window appears on the host display.
- [ ] A MOMoT synthesis task can be triggered and completed from within the container.
- [ ] PR description written in `05-rollout/PR-BODY.md`.

---

## 5. If you get stuck

- Re-read the failing phase's `README.md`.
- Check the cross-cutting reference docs:
  - Architecture: `PROJECT_SUMMARY.md`.
  - Install / target platform: `INSTALL.md`.
  - MOMoT 2.0 Docker repo: https://github.com/hadiDHD/momot-2.0.
- If a constraint is genuinely blocking (e.g., driver issues for X11 forwarding), STOP and surface it to the human.

---

## 6. Quick-start prompt (what the human pastes to trigger you)

> "Execute `docs/features/docker-full-stack/COORDINATOR.md`. Follow the phases in
> order, update `PROGRESS.md` after each phase, and stop at any failing
> acceptance gate so I can review."
