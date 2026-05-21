package blocky_game;

import blocky.AtomicStatement;
import blocky.AtomicStatementKind;
import blocky.BlockyFactory;
import blocky.Body;
import blocky.Cell;
import blocky.CellType;
import blocky.ConditionKind;
import blocky.Container;
import blocky.Direction;
import blocky.ExecutionTrace;
import blocky.GameState;
import blocky.GameStatus;
import blocky.GridMap;
import blocky.IfStmt;
import blocky.Level;
import blocky.Loop;
import blocky.SensorDirection;
import blocky.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated helpers for debugger-like stepping:
 * - Build an {@link ExecutionTrace} starting from an arbitrary (x,y,dir) state
 * - Extract state positions for overlay rendering
 * - Provide a small JS snippet for debug overlay drawing
 *
 * Note: keep this class self-contained so the IDE can resolve it reliably.
 */
public final class DebuggingService {
    private DebuggingService() {}

    public static final int DEBUG_TICK_MS = 250;

    public static final String DEBUG_OVERLAY_GROUP_ID = "debugPrefix";
    public static final String DEBUG_OVERLAY_COLOR = "#4d90fe"; // blue
    public static final String DEBUG_CURRENT_COLOR = "#ffcc00"; // yellow
    public static final String DEBUG_PAST_PREFIX_COLOR = "#4d90fe"; // blue
    public static final String DEBUG_NEW_COMMON_COLOR = "#3bd46a";  // green
    public static final String DEBUG_NEW_DEVIATION_COLOR = "#ff8c1a"; // orange

    public static final class DebugTraceResult {
        public final ExecutionTrace trace;
        public final int[][] statePositions; // 1-to-1 with trace.getStates()
        public final List<String> logLines;  // 1-to-1 with trace.getStates()

        private DebugTraceResult(ExecutionTrace trace, int[][] statePositions, List<String> logLines) {
            this.trace = trace;
            this.statePositions = statePositions;
            this.logLines = logLines;
        }
    }

    public static DebugTraceResult computeTraceFromState(Level level, int startX, int startY, Direction startDir) {
        if (level == null || level.getMap() == null) {
            return new DebugTraceResult(BlockyFactory.eINSTANCE.createExecutionTrace(), new int[0][0], new ArrayList<>());
        }

        GridMap map = level.getMap();
        final CellType winCellType = SimUtils.determineWinCellType(level);
        Cell startCell = getCellAt(map, startX, startY);
        if (startCell == null) {
            // Graceful fallback: use any non-wall cell if the UI passed inconsistent coordinates.
            startCell = findFirstNonWallCell(map);
        }
        if (startCell == null) {
            startCell = map.getCells().isEmpty() ? null : map.getCells().get(0);
        }

        if (startDir == null) {
            startDir = Direction.NORTH;
        }

        ExecutionTrace trace = BlockyFactory.eINSTANCE.createExecutionTrace();
        List<String> logLines = new ArrayList<>();
        GameState initialState = BlockyFactory.eINSTANCE.createGameState();
        initialState.setStep(0);
        initialState.setPosition(startCell);
        initialState.setOrientation(startDir);
        initialState.setStatus(GameStatus.RUNNING);
        trace.getStates().add(initialState);
        logLines.add("Start: (" + startCell.getX() + "," + startCell.getY() + ") dir=" + startDir);

        boolean enforceConstraints = Boolean.parseBoolean(System.getProperty("blocky.sim.enforceConstraints", "false"));
        if (enforceConstraints && SimUtils.violatesLevelConstraints(level, level.getSolution())) {
            initialState.setStatus(GameStatus.CRASHED);
            logLines.add("Result: CRASH (Constraints violated)");
            return new DebugTraceResult(trace, extractStatePositions(trace), logLines);
        }

        Body solution = level.getSolution();
        if (solution != null) {
            executeContainerChain(solution.getFirstContainer(), initialState, trace, map, logLines, winCellType);
        }

        return new DebugTraceResult(trace, extractStatePositions(trace), logLines);
    }

    /**
     * Extracts a 2D array of positions (x,y) in trace state order.
     * This keeps debugging indices aligned with individual execution steps/states.
     */
    public static int[][] extractStatePositions(ExecutionTrace trace) {
        if (trace == null || trace.getStates() == null) return new int[0][0];
        List<GameState> states = trace.getStates();
        int[][] pts = new int[states.size()][2];
        for (int i = 0; i < states.size(); i++) {
            GameState s = states.get(i);
            Cell pos = s != null ? s.getPosition() : null;
            if (pos == null) {
                pts[i][0] = 0;
                pts[i][1] = 0;
            } else {
                pts[i][0] = pos.getX();
                pts[i][1] = pos.getY();
            }
        }
        return pts;
    }

    public static int directionToT(Direction d) {
        if (d == null) return 1;
        switch (d) {
            case NORTH:
                return 0;
            case EAST:
                return 1;
            case SOUTH:
                return 2;
            case WEST:
                return 3;
            default:
                return 1;
        }
    }

    public static Direction tToDirection(int t) {
        switch (t) {
            case 0:
                return Direction.NORTH;
            case 1:
                return Direction.EAST;
            case 2:
                return Direction.SOUTH;
            case 3:
                return Direction.WEST;
            default:
                return Direction.EAST;
        }
    }

    public static String renderDebugOverlayJsSnippet() {
        // JS will:
        // - remove any existing debug overlay group
        // - clear the immediate-feedback green overlay while debugging
        // - draw the cumulative prefix path up to the current debug state
        // - draw a yellow circle at the latest executed point
        // Note: this snippet is inserted into an already-running <script> string.
        return ""
                + "  function __dbgRenderOverlay(prefixPath, pastPrefix, newPreview, commonLen) { "
                + "    try { "
                + "      var svg = document.getElementById('svgMaze'); "
                + "      if (!svg) return; "
                + "      var old = document.getElementById('" + DEBUG_OVERLAY_GROUP_ID + "'); "
                + "      if (old && old.parentNode) old.parentNode.removeChild(old); "
                + "      var oldGreen = document.getElementById('pathNew'); "
                + "      if (oldGreen && oldGreen.parentNode) oldGreen.parentNode.removeChild(oldGreen); "
                + "      var ns = 'http://www.w3.org/2000/svg'; "
                + "      var g = document.createElementNS(ns, 'g'); "
                + "      g.setAttribute('id', '" + DEBUG_OVERLAY_GROUP_ID + "'); "
                + "      function __dbgPts(path) { "
                + "        var pts = []; "
                + "        if (!path || !path.length) return pts; "
                + "        for (var i=0; i<path.length; i++) { "
                + "          pts.push((50*path[i][0]+20) + ',' + (50*path[i][1]+20)); "
                + "        } "
                + "        return pts; "
                + "      } "
                + "      function __dbgDrawPolyline(path, stroke, width, opacity, dash) { "
                + "        try { "
                + "          if (!path || path.length < 2) return; "
                + "          var poly = document.createElementNS(ns, 'polyline'); "
                + "          poly.setAttribute('points', __dbgPts(path).join(' ')); "
                + "          poly.setAttribute('fill', 'none'); "
                + "          poly.setAttribute('stroke', stroke); "
                + "          poly.setAttribute('stroke-width', String(width)); "
                + "          poly.setAttribute('stroke-linecap', 'round'); "
                + "          poly.setAttribute('stroke-linejoin', 'round'); "
                + "          poly.setAttribute('stroke-opacity', String(opacity)); "
                + "          if (dash) poly.setAttribute('stroke-dasharray', dash); "
                + "          g.appendChild(poly); "
                + "        } catch(e) {} "
                + "      } "
                + "      function __dbgDrawDots(path, color, r, opacity) { "
                + "        try { "
                + "          if (!path || !path.length) return; "
                + "          for (var j=0; j<path.length; j++) { "
                + "            var cx = (50*path[j][0]+20); "
                + "            var cy = (50*path[j][1]+20); "
                + "            var c = document.createElementNS(ns, 'circle'); "
                + "            c.setAttribute('cx', cx); "
                + "            c.setAttribute('cy', cy); "
                + "            c.setAttribute('r', String(r)); "
                + "            c.setAttribute('fill', color); "
                + "            c.setAttribute('fill-opacity', String(opacity)); "
                + "            g.appendChild(c); "
                + "          } "
                + "        } catch(e) {} "
                + "      } "
                + "      // 1) Past prefix (already navigated) – thicker blue.\n"
                + "      __dbgDrawPolyline(pastPrefix, '" + DEBUG_PAST_PREFIX_COLOR + "', 7, 0.35, null); "
                + "      // 2) Current executed prefix – solid blue.\n"
                + "      __dbgDrawPolyline(prefixPath, '" + DEBUG_OVERLAY_COLOR + "', 6, 0.95, null); "
                + "      try { "
                + "        window.__ifCurrentStep = prefixPath.length; "
                + "        if (newPreview) window.__injectNewPath = newPreview; "
                + "        if (pastPrefix) window.__injectPastPath = pastPrefix; "
                + "        if (typeof window.__ifRender === 'function') window.__ifRender(); "
                + "      } catch(eIF) {} "
                + "      var last = (prefixPath && prefixPath.length) ? prefixPath[prefixPath.length - 1] : null; "
                + "      if (last) { "
                + "        var lc = document.createElementNS(ns, 'circle'); "
                + "        lc.setAttribute('cx', (50*last[0]+20)); "
                + "        lc.setAttribute('cy', (50*last[1]+20)); "
                + "        lc.setAttribute('r', '7'); "
                + "        lc.setAttribute('fill', '" + DEBUG_CURRENT_COLOR + "'); "
                + "        lc.setAttribute('fill-opacity', '0.95'); "
                + "        g.appendChild(lc); "
                + "      } "
                + "      var peg = document.getElementById('pegman'); "
                + "      if (peg && peg.parentNode) svg.insertBefore(g, peg); else svg.appendChild(g); "
                + "    } catch(e) {} "
                + "  } "
                ;
    }

    private static Cell getCellAt(GridMap map, int x, int y) {
        if (map == null || map.getCells() == null) return null;
        for (Cell c : map.getCells()) {
            if (c != null && c.getX() == x && c.getY() == y) return c;
        }
        return null;
    }

    private static Cell findFirstNonWallCell(GridMap map) {
        if (map == null || map.getCells() == null) return null;
        for (Cell c : map.getCells()) {
            if (c != null && c.getType() != CellType.WALL) return c;
        }
        return null;
    }

    private static SensorDirection conditionKindToSensor(ConditionKind ck) {
        // Align with MoMoT headless simulator defaults: missing condition behaves as CHECK_FORWARD.
        if (ck == null) ck = ConditionKind.CHECK_FORWARD;
        if (ck == ConditionKind.CHECK_LEFT) return SensorDirection.LEFT;
        if (ck == ConditionKind.CHECK_RIGHT) return SensorDirection.RIGHT;
        return SensorDirection.AHEAD;
    }

    private static boolean checkCondition(GameState state, ConditionKind ck) {
        return checkSensor(state, conditionKindToSensor(ck));
    }

    private static GameState executeContainerChain(
            Container first,
            GameState state,
            ExecutionTrace trace,
            GridMap map,
            List<String> logLines,
            CellType winCellType) {
        Container current = first;
        GameState last = state;
        while (current != null && last.getStatus() == GameStatus.RUNNING && last.getPosition().getType() != winCellType) {
            Statement stmt = current.getStatement();
            last = executeSingle(stmt, last, trace, map, logLines, winCellType);
            current = current.getNext();
        }
        return last;
    }

    private static GameState executeSingle(
            Statement stmt,
            GameState prev,
            ExecutionTrace trace,
            GridMap map,
            List<String> logLines,
            CellType winCellType) {
        GameState next = BlockyFactory.eINSTANCE.createGameState();
        next.setStep(prev.getStep() + 1);
        next.setOrientation(prev.getOrientation());
        next.setPosition(prev.getPosition());
        next.setStatus(GameStatus.RUNNING);
        next.setExecutingStatement(stmt);
        next.setPrevious(prev);
        trace.getStates().add(next);

        if (stmt == null) {
            if (logLines != null) {
                logLines.add("Step " + next.getStep() + ": (empty)");
            }
            return next; // empty container -> no-op
        }

        if (stmt instanceof AtomicStatement) {
            AtomicStatement a = (AtomicStatement) stmt;
            // Align with MoMoT headless simulator defaults: missing kind behaves as TURN_LEFT.
            AtomicStatementKind kind = a.getKind();
            if (kind == null) kind = AtomicStatementKind.TURN_LEFT;
            switch (kind) {
            case MOVE_FORWARD: {
                Cell target = getAdjacent(next.getPosition(), next.getOrientation());
                if (target == null || target.getType() == CellType.WALL) {
                    next.setStatus(GameStatus.CRASHED);
                    if (logLines != null) {
                        logLines.add("Step " + next.getStep() + ": MoveForward -> CRASH at (" + safeX(next.getPosition()) + "," + safeY(next.getPosition()) + ")");
                    }
                } else {
                    next.setPosition(target);
                    if (target.getType() == winCellType) {
                        next.setStatus(GameStatus.WON);
                        if (logLines != null) {
                            logLines.add("Step " + next.getStep() + ": MoveForward -> (" + target.getX() + "," + target.getY() + ") GOAL");
                        }
                    } else {
                        if (logLines != null) {
                            logLines.add("Step " + next.getStep() + ": MoveForward -> (" + target.getX() + "," + target.getY() + ")");
                        }
                    }
                }
                break;
            }
            case TURN_LEFT: {
                Direction before = next.getOrientation();
                next.setOrientation(getRelativeDir(next.getOrientation(), SensorDirection.LEFT));
                if (logLines != null) {
                    logLines.add("Step " + next.getStep() + ": TurnLeft -> " + before + "→" + next.getOrientation());
                }
                break;
            }
            case TURN_RIGHT: {
                Direction before = next.getOrientation();
                next.setOrientation(getRelativeDir(next.getOrientation(), SensorDirection.RIGHT));
                if (logLines != null) {
                    logLines.add("Step " + next.getStep() + ": TurnRight -> " + before + "→" + next.getOrientation());
                }
                break;
            }
            default:
                break;
            }
        } else if (stmt instanceof Loop) {
            Loop r = (Loop) stmt;
            GameState loop = next;
            if (logLines != null) {
                logLines.add("Step " + next.getStep() + ": Loop");
            }
            int maxSteps = map.getWidth() * map.getHeight() * 2;
            while (loop.getStatus() == GameStatus.RUNNING && loop.getPosition().getType() != winCellType) {
                if (loop.getStep() > maxSteps) {
                    loop.setStatus(GameStatus.CRASHED);
                    break;
                }
                int previousStep = loop.getStep();
                // Loop body is a real Loop in the model: execute it once per iteration.
                Body b = r.getBody();
                loop = executeContainerChain(b != null ? b.getFirstContainer() : null, loop, trace, map, logLines, winCellType);
                if (loop.getStep() == previousStep) {
                    loop.setStatus(GameStatus.CRASHED);
                    break;
                }
            }
            return loop;
        } else if (stmt instanceof IfStmt) {
            IfStmt i = (IfStmt) stmt;
            boolean cond = checkCondition(next, i.getCondition());
            if (logLines != null) {
                String branch = cond ? "then" : (i.getElseBody() != null ? "else" : "skip");
                logLines.add("Step " + next.getStep() + ": If " + conditionKindToSensor(i.getCondition()) + " -> " + cond + " (" + branch + ")");
            }
            if (cond) {
                Body b = i.getThenBody();
                return executeContainerChain(b != null ? b.getFirstContainer() : null, next, trace, map, logLines, winCellType);
            }
            if (i.getElseBody() != null) {
                Body b = i.getElseBody();
                return executeContainerChain(b != null ? b.getFirstContainer() : null, next, trace, map, logLines, winCellType);
            }
        }

        return next;
    }


    private static int safeX(Cell c) {
        return c != null ? c.getX() : 0;
    }

    private static int safeY(Cell c) {
        return c != null ? c.getY() : 0;
    }

    private static boolean checkSensor(GameState state, SensorDirection sensor) {
        Direction actual = getRelativeDir(state.getOrientation(), sensor);
        Cell target = getAdjacent(state.getPosition(), actual);
        return target != null && target.getType() != CellType.WALL;
    }

    private static Cell getAdjacent(Cell c, Direction d) {
        if (c == null || d == null) return null;
        switch (d) {
            case NORTH:
                return c.getTop();
            case SOUTH:
                return c.getBottom();
            case EAST:
                return c.getRight();
            case WEST:
                return c.getLeft();
        }
        return null;
    }

    private static Direction getRelativeDir(Direction curr, SensorDirection sensor) {
        if (sensor == SensorDirection.AHEAD) return curr;
        if (sensor == SensorDirection.LEFT) {
            switch (curr) {
                case NORTH:
                    return Direction.WEST;
                case WEST:
                    return Direction.SOUTH;
                case SOUTH:
                    return Direction.EAST;
                case EAST:
                    return Direction.NORTH;
            }
        } else {
            switch (curr) {
                case NORTH:
                    return Direction.EAST;
                case EAST:
                    return Direction.SOUTH;
                case SOUTH:
                    return Direction.WEST;
                case WEST:
                    return Direction.NORTH;
            }
        }
        return curr;
    }
}

