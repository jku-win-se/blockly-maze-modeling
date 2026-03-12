package blocky_momot;

import blocky.Block;
import blocky.BlockyFactory;
import blocky.BlockyPackage;
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
        Block solution = level.getSolution();
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

        GameState last = executeSequence(solution, initialState, trace, level);
        return last != null ? last.getStatus() : GameStatus.CRASHED;
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

    private static GameState executeSequence(Block first, GameState state, ExecutionTrace trace, Level level) {
        Block current = first;
        GameState last = state;
        while (current != null && last.getStatus() == GameStatus.RUNNING) {
            last = executeSingle(current, last, trace, level);
            current = current.getNext();
        }
        return last;
    }

    private static GameState executeSingle(Block block, GameState prev, ExecutionTrace trace, Level level) {
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
            GridMap map = level.getMap();
            int maxSteps = map.getWidth() * map.getHeight() * 2;
            while (loop.getStatus() == GameStatus.RUNNING && loop.getPosition().getType() != CellType.GOAL) {
                if (loop.getStep() > maxSteps) {
                    loop.setStatus(GameStatus.CRASHED);
                    break;
                }
                int previousStep = loop.getStep();
                loop = executeSequence(r.getBody(), loop, trace, level);
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
                return executeSequence(i.getThenBranch(), next, trace, level);
            }
            if (i.getElseBranch() != null) {
                return executeSequence(i.getElseBranch(), next, trace, level);
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

    private static Direction calculateTurn(Direction d, TurnDirection td) {
        return getRelativeDir(d, td == TurnDirection.LEFT ? SensorDirection.LEFT : SensorDirection.RIGHT);
    }
}
