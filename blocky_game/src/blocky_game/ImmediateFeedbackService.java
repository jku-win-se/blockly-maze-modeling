package blocky_game;

import blocky.AtomicStatement;
import blocky.BlockyFactory;
import blocky.BlockyPackage;
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
 * Computes and renders “immediate feedback” overlays for old vs new paths.
 *
 * Rule: all immediate-feedback computation and UI overlay code lives here.
 */
public final class ImmediateFeedbackService {
    private ImmediateFeedbackService() {}

    public static final int TILE_SIZE_PX = 50;
    public static final int TILE_CENTER_OFFSET_PX = 25;

    public static final String PAST_OVERLAY_ID = "pathPast";
    public static final String NEW_OVERLAY_ID = "pathNew";

    public static final String PAST_COLOR = "#ff4d4d";
    public static final String NEW_COLOR = "#3bd46a";

    public static final class Paths {
        public final int[][] pastPath;
        public final int[][] newPath;

        private Paths(int[][] pastPath, int[][] newPath) {
            this.pastPath = pastPath;
            this.newPath = newPath;
        }
    }

    public static Paths computePaths(Level level) {
        if (level == null || level.getMap() == null) {
            return new Paths(new int[0][0], new int[0][0]);
        }

        GridMap map = level.getMap();
        Cell startCell = findStartCell(map);
        if (startCell == null) {
            int[][] pastPath = extractPathPoints(firstTrace(level));
            return new Paths(pastPath, new int[0][0]);
        }
        Direction startDir = determineStartOrientation(level, startCell);
        level.setStartOrientation(startDir);

        int[][] pastPath = extractPathPoints(firstTrace(level));
        if (pastPath.length == 0 && startCell != null) {
            pastPath = new int[][] { new int[] { startCell.getX(), startCell.getY() } };
        }

        int[][] newPath;
        if (level.getSolution() == null) {
            newPath = new int[][] { new int[] { startCell.getX(), startCell.getY() } };
        } else {
            ExecutionTrace newTrace = simulateExecutionTraceBody(level.getSolution(), map, startCell, startDir);
            newPath = extractPathPoints(newTrace);
            if (newPath.length == 0 && startCell != null) {
                newPath = new int[][] { new int[] { startCell.getX(), startCell.getY() } };
            }
        }

        return new Paths(pastPath, newPath);
    }

    public static String toJsonArray(int[][] path) {
        if (path == null || path.length == 0) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < path.length; i++) {
            int[] pt = path[i];
            if (i > 0) sb.append(",");
            if (pt == null || pt.length < 2) {
                sb.append("[0,0]");
            } else {
                sb.append("[").append(pt[0]).append(",").append(pt[1]).append("]");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static String buildWindowInjectPathsScript(int[][] pastPath, int[][] newPath) {
        return "window.__injectPastPath = " + toJsonArray(pastPath) + ";"
                + "window.__injectNewPath = " + toJsonArray(newPath) + ";";
    }

    /**
     * JavaScript snippet that draws both overlays as SVG polylines + circles.
     * Must be inserted into the WebView initialization script after `Wd()` and `$d(false)`.
     */
    public static String buildOverlayRenderJs() {
        // Use double quotes in JS since this snippet is inserted at runtime by Java string concatenation.
        return ""
                + "  try { "
                + "    var svg = document.getElementById('svgMaze'); "
                + "    var ns = 'http://www.w3.org/2000/svg'; "
                + "    function drawOverlay(id, path, color) { "
                + "      var old = document.getElementById(id); "
                + "      if (old && old.parentNode) old.parentNode.removeChild(old); "
                + "      if (!path || !path.length || !svg) return; "
                + "      var g = document.createElementNS(ns, 'g'); "
                + "      g.setAttribute('id', id); "
                + "      g.setAttribute('data-immediate-feedback', 'true'); "
                + "      if (path.length >= 2) { "
                + "        var pts = []; "
                + "        for (var i=0; i<path.length; i++) { "
                + "          pts.push((50*path[i][0]+25) + ',' + (50*path[i][1]+25)); "
                + "        } "
                + "        var poly = document.createElementNS(ns, 'polyline'); "
                + "        poly.setAttribute('points', pts.join(' ')); "
                + "        poly.setAttribute('fill', 'none'); "
                + "        poly.setAttribute('stroke', color); "
                + "        poly.setAttribute('stroke-width', '6'); "
                + "        poly.setAttribute('stroke-linecap', 'round'); "
                + "        poly.setAttribute('stroke-linejoin', 'round'); "
                + "        poly.setAttribute('stroke-opacity', '0.85'); "
                + "        g.appendChild(poly); "
                + "      } "
                + "      for (var j=0; j<path.length; j++) { "
                + "        var cx = (50*path[j][0]+25); "
                + "        var cy = (50*path[j][1]+25); "
                + "        var c = document.createElementNS(ns, 'circle'); "
                + "        c.setAttribute('cx', cx); "
                + "        c.setAttribute('cy', cy); "
                + "        c.setAttribute('r', '5'); "
                + "        c.setAttribute('fill', color); "
                + "        c.setAttribute('fill-opacity', '0.75'); "
                + "        g.appendChild(c); "
                + "      } "
                + "      var peg = document.getElementById('pegman'); "
                + "      if (peg && peg.parentNode) svg.insertBefore(g, peg); else svg.appendChild(g); "
                + "    } "
                + "    drawOverlay('" + PAST_OVERLAY_ID + "', window.__injectPastPath, '" + PAST_COLOR + "'); "
                + "    drawOverlay('" + NEW_OVERLAY_ID + "', window.__injectNewPath, '" + NEW_COLOR + "'); "
                + "  } catch(e) { if (window.javaBridge) window.javaBridge.logJS('ImmediateFeedback overlay: ' + e); }";
    }

    private static ExecutionTrace firstTrace(Level level) {
        if (level.getTraces() == null || level.getTraces().isEmpty()) return null;
        return level.getTraces().get(0);
    }

    private static Cell findStartCell(GridMap map) {
        if (map == null || map.getCells() == null || map.getCells().isEmpty()) return null;
        for (Cell c : map.getCells()) {
            if (c.getType() == CellType.START) return c;
        }
        // Fallback to first cell to keep UI stable.
        return map.getCells().get(0);
    }

    private static Direction determineStartOrientation(Level level, Cell start) {
        if (level != null && level.eIsSet(BlockyPackage.Literals.LEVEL__START_ORIENTATION)) {
            return level.getStartOrientation();
        }
        if (start == null) return Direction.NORTH;

        // Prefer any non-wall neighbour in a fixed order: NORTH, EAST, SOUTH, WEST.
        if (start.getTop() != null && start.getTop().getType() != CellType.WALL) return Direction.NORTH;
        if (start.getRight() != null && start.getRight().getType() != CellType.WALL) return Direction.EAST;
        if (start.getBottom() != null && start.getBottom().getType() != CellType.WALL) return Direction.SOUTH;
        if (start.getLeft() != null && start.getLeft().getType() != CellType.WALL) return Direction.WEST;

        return Direction.NORTH;
    }

    private static int[][] extractPathPoints(ExecutionTrace trace) {
        if (trace == null || trace.getStates() == null || trace.getStates().isEmpty()) return new int[0][0];

        List<int[]> points = new ArrayList<>();
        Integer lastX = null;
        Integer lastY = null;

        for (GameState s : trace.getStates()) {
            if (s == null) continue;
            Cell pos = s.getPosition();
            if (pos == null) continue;
            int x = pos.getX();
            int y = pos.getY();
            if (lastX != null && lastX == x && lastY == y) continue; // compress duplicates (turn steps)
            points.add(new int[] { x, y });
            lastX = x;
            lastY = y;
        }

        if (points.isEmpty()) return new int[0][0];

        int[][] arr = new int[points.size()][2];
        for (int i = 0; i < points.size(); i++) {
            int[] pt = points.get(i);
            arr[i][0] = pt[0];
            arr[i][1] = pt[1];
        }
        return arr;
    }

    private static ExecutionTrace simulateExecutionTraceBody(Body solution, GridMap map, Cell startCell, Direction startDir) {
        ExecutionTrace trace = BlockyFactory.eINSTANCE.createExecutionTrace();
        GameState initialState = BlockyFactory.eINSTANCE.createGameState();
        initialState.setStep(0);
        initialState.setPosition(startCell);
        initialState.setOrientation(startDir);
        initialState.setStatus(GameStatus.RUNNING);
        trace.getStates().add(initialState);

        if (solution == null) return trace;

        executeBody(solution, initialState, trace, map);
        return trace;
    }

    private static SensorDirection conditionKindToSensor(ConditionKind ck) {
        if (ck == ConditionKind.CHECK_LEFT) return SensorDirection.LEFT;
        if (ck == ConditionKind.CHECK_RIGHT) return SensorDirection.RIGHT;
        return SensorDirection.AHEAD;
    }

    private static boolean checkCondition(GameState state, ConditionKind ck) {
        return checkSensor(state, conditionKindToSensor(ck));
    }

    private static GameState executeBody(Body body, GameState state, ExecutionTrace trace, GridMap map) {
        if (body == null) return state;
        return executeContainerChain(body.getFirstContainer(), state, trace, map);
    }

    private static GameState executeContainerChain(Container first, GameState state, ExecutionTrace trace, GridMap map) {
        Container current = first;
        GameState last = state;
        while (current != null && last.getStatus() == GameStatus.RUNNING) {
            Statement stmt = current.getStatement();
            last = executeSingle(stmt, last, trace, map);
            current = current.getNext();
        }
        return last;
    }

    private static GameState executeSingle(Statement stmt, GameState prev, ExecutionTrace trace, GridMap map) {
        GameState next = BlockyFactory.eINSTANCE.createGameState();
        next.setStep(prev.getStep() + 1);
        next.setOrientation(prev.getOrientation());
        next.setPosition(prev.getPosition());
        next.setStatus(GameStatus.RUNNING);
        next.setExecutingStatement(stmt);
        next.setPrevious(prev);
        trace.getStates().add(next);

        if (stmt instanceof AtomicStatement) {
            AtomicStatement a = (AtomicStatement) stmt;
            switch (a.getKind()) {
            case MOVE_FORWARD: {
                Cell target = getAdjacent(next.getPosition(), next.getOrientation());
                if (target == null || target.getType() == CellType.WALL) {
                    next.setStatus(GameStatus.CRASHED);
                } else {
                    next.setPosition(target);
                    if (target.getType() == CellType.GOAL) {
                        next.setStatus(GameStatus.WON);
                    }
                }
                break;
            }
            case TURN_LEFT:
                next.setOrientation(getRelativeDir(next.getOrientation(), SensorDirection.LEFT));
                break;
            case TURN_RIGHT:
                next.setOrientation(getRelativeDir(next.getOrientation(), SensorDirection.RIGHT));
                break;
            default:
                break;
            }
        } else if (stmt instanceof Loop) {
            Loop r = (Loop) stmt;
            GameState loop = next;
            int maxSteps = map.getWidth() * map.getHeight() * 2;
            while (loop.getStatus() == GameStatus.RUNNING && loop.getPosition().getType() != CellType.GOAL) {
                if (loop.getStep() > maxSteps) {
                    loop.setStatus(GameStatus.CRASHED);
                    break;
                }
                int previousStep = loop.getStep();
                loop = executeBody(r.getBody(), loop, trace, map);
                if (loop.getStep() == previousStep) {
                    loop.setStatus(GameStatus.CRASHED);
                    break;
                }
            }
            return loop;
        } else if (stmt instanceof IfStmt) {
            IfStmt i = (IfStmt) stmt;
            boolean cond = checkCondition(next, i.getCondition());
            if (cond) {
                return executeBody(i.getThenBody(), next, trace, map);
            }
            if (i.getElseBody() != null) {
                return executeBody(i.getElseBody(), next, trace, map);
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
}

