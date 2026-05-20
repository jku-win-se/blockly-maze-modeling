# Phase 4 — Verification

## Goal
Perform a full end-to-end verification of the containerized application.

## Inputs
- Fully built and configured Docker environment.

## Steps
1. Launch the full stack: `docker-compose up`.
2. Load a level via the UI (reading from volume-mounted `load.xmi`).
3. Run a Blockly program and verify the character moves in the WebView.
4. Save the state and verify that `save.xmi` on the host machine has been updated.
5. Trigger a "Direct Manipulation" request to verify the MOMoT integration pipeline.

## Deliverables
- `docs/features/docker-full-stack/04-verification/smoke-test-results.md`

## Acceptance Gate
- [ ] `save.xmi` on host matches the expected state after container execution.
- [ ] No `ClassNotFoundException` or `UnsatisfiedLinkError` in the container logs.
