# Phase 1 — Docker Infrastructure

## Goal
Establish the base Docker image and orchestration setup to build and run the Blocky Maze application.

## Inputs
- `pom.xml`
- `PROJECT_SUMMARY.md`
- Reference: https://github.com/hadiDHD/momot-2.0

## Steps
1. Create a `Dockerfile` in the repo root using a multi-stage build:
    - Stage 1: Build with Maven and OpenJDK 21.
    - Stage 2: Runtime image with OpenJDK 21, JavaFX native dependencies (libgtk, libgl, etc.), and MOMoT 2.0 libraries.
2. Create `docker-compose.yml` to manage build arguments and volume mounts for `blocky_game/load.xmi` and `blocky_game/save.xmi`.

## Deliverables
- `Dockerfile`
- `docker-compose.yml`

## Acceptance Gate
- [ ] `docker-compose build` exits 0.
- [ ] `docker inspect blocky-maze-app` confirms volume mounts are configured.
