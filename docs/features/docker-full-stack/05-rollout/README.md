# Phase 5 — Rollout

## Goal
Finalize documentation and prepare the feature for merging.

## Inputs
- Results from Phase 4.

## Steps
1. Create `DOCKER.md` in the repo root with clear instructions for different host operating systems.
2. Update the main `README.md` to mention Docker support.
3. Draft the `PR-BODY.md`.

## Deliverables
- `DOCKER.md`
- `PR-BODY.md`

## Acceptance Gate
- [ ] `test -f DOCKER.md`
- [ ] `grep "Docker" README.md` returns ≥ 1 match.
