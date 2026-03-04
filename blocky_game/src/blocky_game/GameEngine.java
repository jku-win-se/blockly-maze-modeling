package blocky_game;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import blocky.*;

public class GameEngine {

    private Level currentLevel;
    private Resource resource;

    public void initializeGame() {
        BlockyPackage.eINSTANCE.eClass();
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

        ResourceSet resSet = new ResourceSetImpl();
        java.io.File xmiFile = new java.io.File("blocky_game/level1_state.xmi").getAbsoluteFile();
        if (!xmiFile.getParentFile().exists()) {
            // Try alternate paths
            xmiFile = new java.io.File("level1_state.xmi").getAbsoluteFile();
        }
        System.out.println("[GameEngine] XMI file path: " + xmiFile.getAbsolutePath());
        resource = resSet.createResource(URI.createFileURI(xmiFile.getAbsolutePath()));

        currentLevel = BlockyFactory.eINSTANCE.createLevel();
        resource.getContents().add(currentLevel);
    }

    public void setMapFromJson(String mapJson) {
        // mapJson looks like:
        // [[0,0,0...],[...],...]
        mapJson = mapJson.replaceAll("\\s+", "");
        if (mapJson.startsWith("[[")) {
            mapJson = mapJson.substring(2, mapJson.length() - 2);
        }

        String[] rowStrs = mapJson.split("\\],\\[");
        int height = rowStrs.length;
        int width = rowStrs[0].split(",").length;

        GridMap map = BlockyFactory.eINSTANCE.createGridMap();
        map.setWidth(width);
        map.setHeight(height);
        currentLevel.setMap(map);

        Cell[][] grid = new Cell[width][height];

        for (int y = 0; y < height; y++) {
            String[] colStrs = rowStrs[y].split(",");
            for (int x = 0; x < width; x++) {
                int val = Integer.parseInt(colStrs[x]);
                Cell cell = BlockyFactory.eINSTANCE.createCell();
                cell.setX(x);
                cell.setY(y);

                switch (val) {
                    case 0:
                        cell.setType(CellType.WALL);
                        break;
                    case 1:
                        cell.setType(CellType.EMPTY);
                        break;
                    case 2:
                        cell.setType(CellType.START);
                        break;
                    case 3:
                        cell.setType(CellType.GOAL);
                        break;
                }

                grid[x][y] = cell;
                map.getCells().add(cell);
            }
        }

        // Link cells
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = grid[x][y];
                if (y > 0)
                    cell.setTop(grid[x][y - 1]);
                if (y < height - 1)
                    cell.setBottom(grid[x][y + 1]);
                if (x > 0)
                    cell.setLeft(grid[x - 1][y]);
                if (x < width - 1)
                    cell.setRight(grid[x + 1][y]);
            }
        }
        saveModel();
    }

    public void cycleCellType(int x, int y) {
        int index = y * 8 + x;
        Cell cell = currentLevel.getMap().getCells().get(index);

        switch (cell.getType()) {
            case EMPTY:
                cell.setType(CellType.WALL);
                break;
            case WALL:
                setUniqueCellType(cell, CellType.START);
                break;
            case START:
                setUniqueCellType(cell, CellType.GOAL);
                break;
            case GOAL:
                cell.setType(CellType.EMPTY);
                break;
        }
        saveModel();
    }

    private void setUniqueCellType(Cell targetCell, CellType type) {
        for (Cell c : currentLevel.getMap().getCells()) {
            if (c.getType() == type) {
                c.setType(CellType.EMPTY);
            }
        }
        targetCell.setType(type);
    }

    // --- Block Management ---

    public void rebuildProgram(java.util.List<Map<String, Object>> blockData) {
        System.out.println("[GameEngine] Rebuilding model solution...");
        currentLevel.getTraces().clear(); // Clear old traces—they reference old solution blocks
        currentLevel.setSolution(null);
        if (blockData == null || blockData.isEmpty()) {
            System.out.println("[GameEngine] Program cleared.");
            saveModel();
            return;
        }

        currentLevel.setSolution(buildSequence(blockData));
        System.out.println("[GameEngine] Solution rebuilt. Main sequence length: " + blockData.size());
        saveModel();
    }

    private Block createBlockFromData(Map<String, Object> data) {
        String type = (String) data.get("type");
        Block block = null;

        if ("maze_moveForward".equals(type) || "move_forward".equals(type)) {
            block = BlockyFactory.eINSTANCE.createMoveForward();
        } else if ("maze_turn".equals(type) || "turn_left".equals(type) || "turn_right".equals(type)) {
            Turn t = BlockyFactory.eINSTANCE.createTurn();
            // Default to LEFT if not specified, but Blockly maze_turn usually has a field
            String dir = (String) data.get("DIR");
            if (dir == null)
                dir = type.contains("right") ? "turn_right" : "turn_left";
            t.setDirection(dir.contains("right") ? TurnDirection.RIGHT : TurnDirection.LEFT);
            block = t;
        } else if ("maze_forever".equals(type) || "repeat_until_goal".equals(type)) {
            RepeatUntilGoal r = BlockyFactory.eINSTANCE.createRepeatUntilGoal();
            Map<String, Object> bodyData = (Map<String, Object>) data.get("body");
            if (bodyData != null) {
                r.setBody(createBlockFromData(bodyData));
            }
            block = r;
        } else if (type != null && (type.startsWith("maze_if") || type.startsWith("if_path"))) {
            IfStatement i = BlockyFactory.eINSTANCE.createIfStatement();
            String dir = (String) data.get("DIR");
            if (dir != null) {
                i.setCondition(parseCondition(dir));
            } else {
                i.setCondition(parseCondition(type));
            }
            Map<String, Object> thenData = (Map<String, Object>) data.get("body"); // Blockly uses 'body' or 'then'
            if (thenData == null)
                thenData = (Map<String, Object>) data.get("then");
            if (thenData != null) {
                i.setThenBranch(createBlockFromData(thenData));
            }
            // Handle else branch (Blockly maze_ifElse blocks)
            Map<String, Object> elseData = (Map<String, Object>) data.get("elseBranch");
            if (elseData == null)
                elseData = (Map<String, Object>) data.get("else");
            if (elseData != null) {
                i.setElseBranch(createBlockFromData(elseData));
            }
            block = i;
        }

        // Recursively handle next block
        if (block != null) {
            Map<String, Object> nextData = (Map<String, Object>) data.get("next");
            if (nextData != null) {
                block.setNext(createBlockFromData(nextData));
            }
        }

        return block;
    }

    private Block buildSequence(java.util.List<Map<String, Object>> dataList) {
        Block first = null;
        Block last = null;
        for (Map<String, Object> data : dataList) {
            Block b = createBlockFromData(data);
            if (b != null) {
                if (first == null)
                    first = b;
                if (last != null)
                    last.setNext(b);
                last = b;
            }
        }
        return first;
    }

    private SensorDirection parseCondition(String type) {
        if (type.contains("ahead"))
            return SensorDirection.AHEAD;
        if (type.contains("left"))
            return SensorDirection.LEFT;
        if (type.contains("right"))
            return SensorDirection.RIGHT;
        return SensorDirection.AHEAD;
    }

    // --- Simulation ---

    public void simulateUserProgram() {
        System.out.println("\n[GameEngine] Starting simulation...");
        currentLevel.getTraces().clear();
        ExecutionTrace trace = BlockyFactory.eINSTANCE.createExecutionTrace();
        currentLevel.getTraces().add(trace);

        GameState initialState = BlockyFactory.eINSTANCE.createGameState();
        initialState.setStep(0);

        Cell startNode = null;
        for (Cell c : currentLevel.getMap().getCells()) {
            if (c.getType() == CellType.START)
                startNode = c;
        }
        if (startNode == null)
            startNode = currentLevel.getMap().getCells().get(0);

        initialState.setPosition(startNode);
        initialState.setOrientation(currentLevel.getStartOrientation());
        initialState.setStatus(GameStatus.RUNNING);
        trace.getStates().add(initialState);

        System.out.println(
                "[GameEngine] Initial State: Pos=" + getPosStr(startNode) + ", Dir=" + initialState.getOrientation());

        executeSequence(currentLevel.getSolution(), initialState, trace);
        saveModel();
        System.out.println("[GameEngine] Simulation finished. Model saved.\n");
    }

    private String getPosStr(Cell c) {
        if (c == null)
            return "null";
        return "(" + c.getX() + "," + c.getY() + ")";
    }

    private GameState executeSequence(Block first, GameState state, ExecutionTrace trace) {
        Block current = first;
        GameState last = state;
        while (current != null && last.getStatus() == GameStatus.RUNNING) {
            last = executeSingle(current, last, trace);
            current = current.getNext();
        }
        return last;
    }

    private GameState executeSingle(Block block, GameState prev, ExecutionTrace trace) {
        GameState next = BlockyFactory.eINSTANCE.createGameState();
        next.setStep(prev.getStep() + 1);
        next.setOrientation(prev.getOrientation());
        next.setPosition(prev.getPosition());
        next.setStatus(GameStatus.RUNNING);
        next.setExecutingBlock(block);
        next.setPrevious(prev);
        trace.getStates().add(next);

        String typeName = block.getClass().getSimpleName().replace("Impl", "");
        System.out.print("[GameEngine] Step " + next.getStep() + ": " + typeName + " -> ");

        if (block instanceof MoveForward) {
            Cell target = getAdjacent(next.getPosition(), next.getOrientation());
            if (target == null || target.getType() == CellType.WALL) {
                next.setStatus(GameStatus.CRASHED);
                System.out.println("CRASHED at " + getPosStr(next.getPosition()));
            } else {
                next.setPosition(target);
                System.out.println("Moved to " + getPosStr(target));
                if (target.getType() == CellType.GOAL) {
                    next.setStatus(GameStatus.WON);
                    System.out.println("[GameEngine] SUCCESS: Goal reached!");
                }
            }
        } else if (block instanceof Turn) {
            Turn t = (Turn) block;
            next.setOrientation(calculateTurn(next.getOrientation(), t.getDirection()));
            System.out.println("Turned " + t.getDirection() + ". New Dir: " + next.getOrientation());
        } else if (block instanceof RepeatUntilGoal) {
            System.out.println("Loop Start");
            RepeatUntilGoal r = (RepeatUntilGoal) block;
            GameState loop = next;
            while (loop.getStatus() == GameStatus.RUNNING && loop.getPosition().getType() != CellType.GOAL) {
                if (loop.getStep() > 50) {
                    loop.setStatus(GameStatus.CRASHED);
                    System.out.println("[GameEngine] Infinite loop detected!");
                    break;
                }
                int previousStep = loop.getStep();
                loop = executeSequence(r.getBody(), loop, trace);
                if (loop.getStep() == previousStep) {
                    loop.setStatus(GameStatus.CRASHED);
                    System.out.println("[GameEngine] Empty loop body or zero progress in loop! Crashing.");
                    break;
                }
            }
            return loop;
        } else if (block instanceof IfStatement) {
            IfStatement i = (IfStatement) block;
            boolean cond = checkSensor(next, i.getCondition());
            System.out.println("If (" + i.getCondition() + ") is " + cond);
            if (cond) {
                return executeSequence(i.getThenBranch(), next, trace);
            } else if (i.getElseBranch() != null) {
                return executeSequence(i.getElseBranch(), next, trace);
            }
        }
        return next;
    }

    private boolean checkSensor(GameState state, SensorDirection sensor) {
        Direction actual = getRelativeDir(state.getOrientation(), sensor);
        Cell target = getAdjacent(state.getPosition(), actual);
        return target != null && target.getType() != CellType.WALL;
    }

    private Cell getAdjacent(Cell c, Direction d) {
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

    private Direction getRelativeDir(Direction curr, SensorDirection sensor) {
        if (sensor == SensorDirection.AHEAD)
            return curr;
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

    private Direction calculateTurn(Direction d, TurnDirection td) {
        return getRelativeDir(d, td == TurnDirection.LEFT ? SensorDirection.LEFT : SensorDirection.RIGHT);
    }

    public void saveModel() {
        try {
            resource.save(null);
            System.out.println("[GameEngine] Model saved to: " + resource.getURI().toFileString());
        } catch (Exception e) {
            System.err.println("[GameEngine] SAVE FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }
}
