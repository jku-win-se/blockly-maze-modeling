package blocky_game;

import blocky.Block;
import blocky.Cell;
import blocky.CellType;
import blocky.Direction;
import blocky.ExecutionTrace;
import blocky.GameState;
import blocky.GameStatus;
import blocky.GridMap;
import blocky.IfStatement;
import blocky.Level;
import blocky.MoveForward;
import blocky.RepeatUntilGoal;
import blocky.SensorDirection;
import blocky.Turn;
import blocky.TurnDirection;
import blocky.BlockyFactory;
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

    public static final class DebugTraceResult {
        public final ExecutionTrace trace;
        public final int[][] statePositions; // 1-to-1 with trace.getStates()

        private DebugTraceResult(ExecutionTrace trace, int[][] statePositions) {
            this.trace = trace;
            this.statePositions = statePositions;
        }
    }

    public static DebugTraceResult computeTraceFromState(Level level, int startX, int startY, Direction startDir) {
        if (level == null || level.getMap() == null) {
            return new DebugTraceResult(BlockyFactory.eINSTANCE.createExecutionTrace(), new int[0][0]);
        }

        GridMap map = level.getMap();
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
        GameState initialState = BlockyFactory.eINSTANCE.createGameState();
        initialState.setStep(0);
        initialState.setPosition(startCell);
        initialState.setOrientation(startDir);
        initialState.setStatus(GameStatus.RUNNING);
        trace.getStates().add(initialState);

        Block solution = level.getSolution();
        if (solution != null) {
            GameState last = executeSequence(solution, initialState, trace, map);
            // Keep trace as-is; the UI will just show positions up to the current debug index.
            if (last == null) {
                // no-op
            }
        }

        return new DebugTraceResult(trace, extractStatePositions(trace));
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
                + "  function __dbgRenderOverlay(prefixPath) { "
                + "    try { "
                + "      var svg = document.getElementById('svgMaze'); "
                + "      if (!svg) return; "
                + "      var old = document.getElementById('" + DEBUG_OVERLAY_GROUP_ID + "'); "
                + "      if (old && old.parentNode) old.parentNode.removeChild(old); "
                + "      var oldGreen = document.getElementById('pathNew'); "
                + "      if (oldGreen && oldGreen.parentNode) oldGreen.parentNode.removeChild(oldGreen); "
                + "      if (!prefixPath || !prefixPath.length) return; "
                + "      var ns = 'http://www.w3.org/2000/svg'; "
                + "      var g = document.createElementNS(ns, 'g'); "
                + "      g.setAttribute('id', '" + DEBUG_OVERLAY_GROUP_ID + "'); "
                + "      var last = prefixPath[prefixPath.length - 1]; "
                + "      if (prefixPath.length >= 2) { "
                + "        var pts = []; "
                + "        for (var i = 0; i < prefixPath.length; i++) { "
                + "          pts.push((50*prefixPath[i][0]+25) + ',' + (50*prefixPath[i][1]+25)); "
                + "        } "
                + "        var poly = document.createElementNS(ns, 'polyline'); "
                + "        poly.setAttribute('points', pts.join(' ')); "
                + "        poly.setAttribute('fill', 'none'); "
                + "        poly.setAttribute('stroke', '" + DEBUG_OVERLAY_COLOR + "'); "
                + "        poly.setAttribute('stroke-width', '6'); "
                + "        poly.setAttribute('stroke-linecap', 'round'); "
                + "        poly.setAttribute('stroke-linejoin', 'round'); "
                + "        poly.setAttribute('stroke-opacity', '0.9'); "
                + "        g.appendChild(poly); "
                + "      } "
                + "      var lc = document.createElementNS(ns, 'circle'); "
                + "      lc.setAttribute('cx', (50*last[0]+25)); "
                + "      lc.setAttribute('cy', (50*last[1]+25)); "
                + "      lc.setAttribute('r', '7'); "
                + "      lc.setAttribute('fill', '" + DEBUG_CURRENT_COLOR + "'); "
                + "      lc.setAttribute('fill-opacity', '0.95'); "
                + "      g.appendChild(lc); "
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

    private static GameState executeSequence(Block first, GameState state, ExecutionTrace trace, GridMap map) {
        Block current = first;
        GameState last = state;
        while (current != null && last.getStatus() == GameStatus.RUNNING) {
            last = executeSingle(current, last, trace, map);
            current = current.getNext();
        }
        return last;
    }

    private static GameState executeSingle(Block block, GameState prev, ExecutionTrace trace, GridMap map) {
        GameState next = BlockyFactory.eINSTANCE.createGameState();
        next.setStep(prev.getStep() + 1);
        next.setOrientation(prev.getOrientation());
        next.setPosition(prev.getPosition());
        next.setStatus(GameStatus.RUNNING);
        next.setExecutingBlock(block);
        next.setPrevious(prev);
        trace.getStates().add(next);

        if (block instanceof MoveForward) {
            Cell target = getAdjacent(next.getPosition(), next.getOrientation());
            if (target == null || target.getType() == CellType.WALL) {
                next.setStatus(GameStatus.CRASHED);
            } else {
                next.setPosition(target);
                if (target.getType() == CellType.GOAL) {
                    next.setStatus(GameStatus.WON);
                }
            }
        } else if (block instanceof Turn) {
            Turn t = (Turn) block;
            next.setOrientation(calculateTurn(next.getOrientation(), t.getDirection()));
        } else if (block instanceof RepeatUntilGoal) {
            RepeatUntilGoal r = (RepeatUntilGoal) block;
            GameState loop = next;
            int maxSteps = map.getWidth() * map.getHeight() * 2;
            while (loop.getStatus() == GameStatus.RUNNING && loop.getPosition().getType() != CellType.GOAL) {
                if (loop.getStep() > maxSteps) {
                    loop.setStatus(GameStatus.CRASHED);
                    break;
                }
                int previousStep = loop.getStep();
                loop = executeSequence(r.getBody(), loop, trace, map);
                if (loop.getStep() == previousStep) {
                    loop.setStatus(GameStatus.CRASHED);
                    break;
                }
            }
            return loop;
        } else if (block instanceof IfStatement) {
            IfStatement i = (IfStatement) block;
            boolean cond = checkSensor(next, i.getCondition());
            if (cond) {
                return executeSequence(i.getThenBranch(), next, trace, map);
            } else if (i.getElseBranch() != null) {
                return executeSequence(i.getElseBranch(), next, trace, map);
            }
        }

        return next;
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

    private static Direction calculateTurn(Direction d, TurnDirection td) {
        return getRelativeDir(d, td == TurnDirection.LEFT ? SensorDirection.LEFT : SensorDirection.RIGHT);
    }
}

