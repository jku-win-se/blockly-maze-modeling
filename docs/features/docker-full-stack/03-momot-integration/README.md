# Phase 3 — MOMoT Integration

## Goal
Ensure the search-based synthesis features of MOMoT 2.0 work correctly within the container.

## Inputs
- https://github.com/hadiDHD/momot-2.0
- `blocky_momot/`

## Steps
1. Verify that the `Dockerfile` includes all necessary MOMoT 2.0 libraries and EMF dependencies.
2. Configure `JAVA_OPTS` in the container to include `--add-opens java.base/java.util=ALL-UNNAMED` as required by MOMoT on Java 17+.
3. Test a synthesis command via `docker-compose run`.

## Deliverables
- Finalized `Dockerfile` with MOMoT support.
- Configured entrypoint/command in `docker-compose.yml`.

## Acceptance Gate
- [ ] `docker-compose run blocky-maze-app bash -c "cd blocky_momot && mvn ..."` (or equivalent) executes without `IllegalAccessError`.
- [ ] Synthesis output files appear in the expected volume-mounted directory.
