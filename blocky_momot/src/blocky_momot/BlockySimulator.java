package blocky_momot;

import blocky.AtomicStatement;
import blocky.AtomicStatementKind;
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

/**
 * Headless simulator for Blocky maze programs. Executes a Level's solution
 * on its map and returns the final game status (WON, CRASHED, or RUNNING).
 * Used by MOMoT fitness to evaluate whether a transformed model reaches the goal.
 */
public final class BlockySimulator {

    private BlockySimulator() {}

    /**
     * Run the level's solution on the level's map and return the status of the
     * last game state (WON if goal reached, CRASHED on wall or loop limit, RUNNING if incomplete).
     *
     * @param level the level with map and solution (may be null solution)
     * @return final GameStatus after execution
     */
    public static GameStatus run(Level level) {
        if (level == null || level.getMap() == null) {
            return GameStatus.CRASHED;
        }
        Body solution = level.getSolution();
        if (solution == null) {
            return GameStatus.RUNNING; // no program: never wins
        }

        GridMap map = level.getMap();
        Cell startCell = null;
        for (Cell c : map.getCells()) {
            if (c.getType() == CellType.START) {
                startCell = c;
                break;
            }
        }
        if (startCell == null) {
            startCell = map.getCells().isEmpty() ? null : map.getCells().get(0);
        }
        if (startCell == null) {
            return GameStatus.CRASHED;
        }

        Direction startDir = determineStartOrientation(level, startCell);
        GameState initialState = BlockyFactory.eINSTANCE.createGameState();
        initialState.setStep(0);
        initialState.setPosition(startCell);
        initialState.setOrientation(startDir);
        initialState.setStatus(GameStatus.RUNNING);

        ExecutionTrace trace = BlockyFactory.eINSTANCE.createExecutionTrace();
        trace.getStates().add(initialState);

        GameState last = executeBody(solution, initialState, trace, level);
        return last != null ? last.getStatus() : GameStatus.CRASHED;
    }

    /**
     * Executes the level's solution and returns the executed step count if the goal is reached;
     * otherwise returns a large penalty value. Intended for MOMoT fitness evaluation.
     */
    public static int stepsToGoalOrPenalty(Level level) {
        return stepsToGoalOrPenalty(level, 100000);
    }

    /**
     * Executes the level's solution and returns the executed step count if the goal is reached;
     * otherwise returns {@code penalty}.
     */
    public static int stepsToGoalOrPenalty(Level level, int penalty) {
        if (level == null || level.getMap() == null) {
            return penalty;
        }
        Body solution = level.getSolution();
        if (solution == null) {
            return penalty;
        }

        GridMap map = level.getMap();
        Cell startCell = null;
        for (Cell c : map.getCells()) {
            if (c.getType() == CellType.START) {
                startCell = c;
                break;
            }
        }
        if (startCell == null) {
            startCell = map.getCells().isEmpty() ? null : map.getCells().get(0);
        }
        if (startCell == null) {
            return penalty;
        }

        Direction startDir = determineStartOrientation(level, startCell);
        GameState state = BlockyFactory.eINSTANCE.createGameState();
        state.setStep(0);
        state.setPosition(startCell);
        state.setOrientation(startDir);
        state.setStatus(GameStatus.RUNNING);

        GameState last = executeBodyLite(solution, state, level);
        return (last != null && last.getStatus() == GameStatus.WON) ? last.getStep() : penalty;
    }

    private static Direction determineStartOrientation(Level level, Cell start) {
        if (level != null && level.eIsSet(BlockyPackage.Literals.LEVEL__START_ORIENTATION)) {
            return level.getStartOrientation();
        }
        if (start.getTop() != null && start.getTop().getType() != CellType.WALL) return Direction.NORTH;
        if (start.getRight() != null && start.getRight().getType() != CellType.WALL) return Direction.EAST;
        if (start.getBottom() != null && start.getBottom().getType() != CellType.WALL) return Direction.SOUTH;
        if (start.getLeft() != null && start.getLeft().getType() != CellType.WALL) return Direction.WEST;
        return Direction.NORTH;
    }

    private static SensorDirection conditionKindToSensor(ConditionKind ck) {
        // Align with generated EMF default: missing condition behaves as CHECK_FORWARD.
        if (ck == null) ck = ConditionKind.CHECK_FORWARD;
        if (ck == ConditionKind.CHECK_LEFT) return SensorDirection.LEFT;
        if (ck == ConditionKind.CHECK_RIGHT) return SensorDirection.RIGHT;
        return SensorDirection.AHEAD;
    }

    private static boolean checkCondition(GameState state, ConditionKind ck) {
        return checkSensor(state, conditionKindToSensor(ck));
    }

    private static GameState executeBody(Body body, GameState state, ExecutionTrace trace, Level level) {
        if (body == null) return state;
        return executeContainerChain(body.getFirstContainer(), state, trace, level);
    }

    private static GameState executeBodyLite(Body body, GameState state, Level level) {
        if (body == null) return state;
        return executeContainerChainLite(body.getFirstContainer(), state, level);
    }

    private static GameState executeContainerChain(Container first, GameState state, ExecutionTrace trace, Level level) {
        Container current = first;
        GameState last = state;
        while (current != null && last.getStatus() == GameStatus.RUNNING) {
            Statement stmt = current.getStatement();
            last = executeSingle(stmt, last, trace, level);
            current = current.getNext();
        }
        return last;
    }

    private static GameState executeContainerChainLite(Container first, GameState state, Level level) {
        Container current = first;
        GameState last = state;
        while (current != null && last.getStatus() == GameStatus.RUNNING) {
            Statement stmt = current.getStatement();
            last = executeSingleLite(stmt, last, level);
            current = current.getNext();
        }
        return last;
    }

    private static GameState executeSingle(Statement stmt, GameState prev, ExecutionTrace trace, Level level) {
        GameState next = BlockyFactory.eINSTANCE.createGameState();
        next.setStep(prev.getStep() + 1);
        next.setOrientation(prev.getOrientation());
        next.setPosition(prev.getPosition());
        next.setStatus(GameStatus.RUNNING);
        next.setExecutingStatement(stmt);
        next.setPrevious(prev);
        trace.getStates().add(next);

        if (stmt == null) {
            return next; // empty container -> no-op
        }

        if (stmt instanceof AtomicStatement) {
            AtomicStatement a = (AtomicStatement) stmt;
            // Align with generated EMF default: missing kind behaves as TURN_LEFT.
            AtomicStatementKind kind = a.getKind();
            if (kind == null) {
                kind = AtomicStatementKind.TURN_LEFT;
            }
            switch (kind) {
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
            GridMap map = level.getMap();
            int maxSteps = map.getWidth() * map.getHeight() * 2;
            while (loop.getStatus() == GameStatus.RUNNING && loop.getPosition().getType() != CellType.GOAL) {
                if (loop.getStep() > maxSteps) {
                    loop.setStatus(GameStatus.CRASHED);
                    break;
                }
                int previousStep = loop.getStep();
                loop = executeBody(r.getBody(), loop, trace, level);
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
                return executeBody(i.getThenBody(), next, trace, level);
            }
            if (i.getElseBody() != null) {
                return executeBody(i.getElseBody(), next, trace, level);
            }
        }
        return next;
    }

    private static GameState executeSingleLite(Statement stmt, GameState prev, Level level) {
        GameState next = BlockyFactory.eINSTANCE.createGameState();
        next.setStep(prev.getStep() + 1);
        next.setOrientation(prev.getOrientation());
        next.setPosition(prev.getPosition());
        next.setStatus(GameStatus.RUNNING);
        next.setExecutingStatement(stmt);
        next.setPrevious(prev);

        if (stmt == null) {
            return next; // empty container -> no-op
        }

        if (stmt instanceof AtomicStatement) {
            AtomicStatement a = (AtomicStatement) stmt;
            // Align with generated EMF default: missing kind behaves as TURN_LEFT.
            AtomicStatementKind kind = a.getKind();
            if (kind == null) {
                kind = AtomicStatementKind.TURN_LEFT;
            }
            switch (kind) {
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
            GridMap map = level.getMap();
            int maxSteps = map.getWidth() * map.getHeight() * 2;
            while (loop.getStatus() == GameStatus.RUNNING && loop.getPosition().getType() != CellType.GOAL) {
                if (loop.getStep() > maxSteps) {
                    loop.setStatus(GameStatus.CRASHED);
                    break;
                }
                int previousStep = loop.getStep();
                loop = executeBodyLite(r.getBody(), loop, level);
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
                return executeBodyLite(i.getThenBody(), next, level);
            }
            if (i.getElseBody() != null) {
                return executeBodyLite(i.getElseBody(), next, level);
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
        switch (d) {
            case NORTH: return c.getTop();
            case SOUTH: return c.getBottom();
            case EAST:  return c.getRight();
            case WEST:  return c.getLeft();
        }
        return null;
    }

    private static Direction getRelativeDir(Direction curr, SensorDirection sensor) {
        // Align with generated EMF default: missing orientation behaves as NORTH.
        if (curr == null) curr = Direction.NORTH;
        if (sensor == SensorDirection.AHEAD) return curr;
        if (sensor == SensorDirection.LEFT) {
            switch (curr) {
                case NORTH: return Direction.WEST;
                case WEST:  return Direction.SOUTH;
                case SOUTH: return Direction.EAST;
                case EAST:  return Direction.NORTH;
            }
        } else {
            switch (curr) {
                case NORTH: return Direction.EAST;
                case EAST:  return Direction.SOUTH;
                case SOUTH: return Direction.WEST;
                case WEST:  return Direction.NORTH;
            }
        }
        return curr;
    }
}
