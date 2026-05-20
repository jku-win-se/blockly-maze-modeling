# Docker Full Stack Run — Overview

> **Entry point for humans.** If you want an agent to actually build this,
> open `COORDINATOR.md`.

## Problem
Developers and users need a consistent environment to run both the Blocky Maze JavaFX application and the MOMoT-based synthesis backend. Setting up the Eclipse modeling stack, JavaFX dependencies, and MoMoT 2.0 manually can be error-prone and platform-dependent. A Docker-based solution provides a "one-click" setup for the entire stack.

## Solution shape
We will create a multi-stage `Dockerfile` and a `docker-compose.yml` that:
1. Builds the project using Maven.
2. Sets up a runtime environment with Java 21, JavaFX dependencies, and MOMoT 2.0 libraries.
3. Configures X11 forwarding to allow the JavaFX GUI to be displayed on the host machine.
4. Mounts local volumes for model persistence (`load.xmi`, `save.xmi`).

## What exists vs. what's new

Existing in repo (do **not** duplicate):
- `pom.xml` (Maven build configuration)
- `blocky_game/src/blocky_game/Main.java` (JavaFX Entry point)
- `blocky_momot/` (MOMoT search configuration)

To be added by this feature:
- `Dockerfile` (Container definition)
- `docker-compose.yml` (Orchestration and volume/display mapping)
- `scripts/run-docker.sh` (Helper script for X11 socket setup)

## Folder map

```text
docs/features/docker-full-stack/
├── COORDINATOR.md          ← agent trigger
├── README.md               ← this file
├── PROGRESS.md             ← checklist
├── 01-docker-infra/        ← Dockerfile and compose setup
├── 02-gui-display-config/  ← X11/Wayland rendering config
├── 03-momot-integration/   ← MOMoT 2.0 docker patterns
├── 04-verification/        ← System-wide tests
└── 05-rollout/             ← Documentation and PR
```

## Success criteria
1. `docker-compose build` succeeds without errors.
2. `docker-compose up` launches the Blocky Maze JavaFX window on the host.
3. Clicking "Run Program" or triggering synthesis works inside the container.
4. `mvn -q clean compile` exits 0 from the repo root.
5. Persistent files (`save.xmi`) are correctly updated on the host machine via volumes.
