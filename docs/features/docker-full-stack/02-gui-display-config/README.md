# Phase 2 — GUI Display Config

## Goal
Enable the containerized JavaFX application to render its GUI on the host machine's display.

## Inputs
- `docker-compose.yml` (from Phase 1)

## Steps
1. Identify the host OS requirements:
    - Linux: Map `/tmp/.X11-unix` and set `DISPLAY`.
    - Windows: Instructions for VcXsrv/XQuartz and setting `DISPLAY` to host IP.
2. Update `docker-compose.yml` with the necessary environment variables and volume mappings.
3. Test rendering with a minimal JavaFX "Hello World" or the main `Main.java`.

## Deliverables
- Updated `docker-compose.yml`
- `scripts/x11-setup.sh` (optional helper)

## Acceptance Gate
- [ ] Running `docker-compose up` results in a JavaFX window appearing on the host screen.
- [ ] The window is interactive (can be moved/resized).
