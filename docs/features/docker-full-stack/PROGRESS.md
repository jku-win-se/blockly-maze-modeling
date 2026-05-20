# Progress Tracker — Docker Full Stack Run

> The Coordinator updates this file after each phase.

**Overall:** 5 / 5 phases complete.

---

## Phase 1 — Docker Infrastructure  `01-docker-infra/`

- [x] Create `Dockerfile` referencing MOMoT 2.0 dependencies.
- [x] Create `docker-compose.yml` with volume mappings for `load.xmi` and `save.xmi`.
- [x] Gate: `docker-compose build` exits 0.

---

## Phase 2 — GUI Display Config  `02-gui-display-config/`

- [x] Add `DISPLAY` environment variable to `docker-compose.yml`.
- [x] Configure `.Xauthority` or `/tmp/.X11-unix` volume mounts.
- [x] Gate: A simple JavaFX application launches and renders on the host display from within the container.

---

## Phase 3 — MOMoT Integration  `03-momot-integration/`

- [x] Align MOMoT dependencies in `Dockerfile` with https://github.com/hadiDHD/momot-2.0.
- [x] Add `--add-opens` flags to the container launch command.
- [x] Gate: `docker-compose run blocky-maze mvn -pl blocky_momot ...` (or similar) executes a synthesis task successfully.

---

## Phase 4 — Verification  `04-verification/`

- [x] Scripted manual check: Load a level, run a program, and verify state change in `save.xmi` on host.
- [x] Gate: `docker-compose up` leads to a fully interactive session where "Run Program" works.

---

## Phase 5 — Rollout  `05-rollout/`

- [x] Add `DOCKER.md` with instructions for Windows (VcXsrv) and Linux (X11).
- [x] Create `PR-BODY.md`.
- [x] Gate: `test -f docs/features/docker-full-stack/05-rollout/PR-BODY.md`.
