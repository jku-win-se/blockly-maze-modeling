package blocky_game;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import blocky.*;
import blocky.BlockyPackage;

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

        // Clear execution traces: they reference Cell instances from the previous map, which would become dangling after we replace the map and cause DanglingHREFException on save.
        currentLevel.getTraces().clear();

        // Clear any previously-synced start orientation so syncLevelMeta will set it fresh.
        currentLevel.eUnset(BlockyPackage.Literals.LEVEL__START_ORIENTATION);

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
    }

    public void cycleCellType(int x, int y) {
        int index = y * currentLevel.getMap().getWidth() + x;
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
            return;
        }

        currentLevel.setSolution(buildSequence(blockData));
        System.out.println("[GameEngine] Solution rebuilt. Main sequence length: " + blockData.size());
    }

    private Block createBlockFromData(Map<String, Object> data) {
        String type = (String) data.get("type");
        Block block = null;

        if ("maze_moveForward".equals(type) || "move_forward".equals(type)) {
            block = BlockyFactory.eINSTANCE.createMoveForward();
        } else if ("maze_turn".equals(type) || "turn_left".equals(type) || "turn_right".equals(type)) {
            Turn t = BlockyFactory.eINSTANCE.createTurn();
            String dir = (String) data.get("DIR");
            System.out.println("[GameEngine]   Turn block: type=" + type + ", DIR field=" + dir + ", all keys=" + data.keySet());
            if (dir == null)
                dir = type;
            t.setDirection(dir.toLowerCase().contains("right") ? TurnDirection.RIGHT : TurnDirection.LEFT);
            System.out.println("[GameEngine]   -> resolved dir=" + dir + " -> " + t.getDirection());
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
        String lower = type.toLowerCase();
        if (lower.contains("forward") || lower.contains("ahead"))
            return SensorDirection.AHEAD;
        if (lower.contains("left"))
            return SensorDirection.LEFT;
        if (lower.contains("right"))
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
        Direction startDir = determineStartOrientation(startNode);
        initialState.setOrientation(startDir);
        currentLevel.setStartOrientation(startDir);
        initialState.setStatus(GameStatus.RUNNING);
        trace.getStates().add(initialState);

        System.out.println(
                "[GameEngine] Initial State: Pos=" + getPosStr(startNode) + ", Dir=" + initialState.getOrientation());

        executeSequence(currentLevel.getSolution(), initialState, trace);
        saveModel();
        System.out.println("[GameEngine] Simulation finished. Model (with execution trace) saved.\n");
    }

    private String getPosStr(Cell c) {
        if (c == null)
            return "null";
        return "(" + c.getX() + "," + c.getY() + ")";
    }

    /**
     * Determine a sensible starting orientation based on the map layout.
     * If an explicit start orientation is already stored on the level, that wins.
     * Otherwise, we look for a non-wall neighbour around the START cell and face it.
     */
    private Direction determineStartOrientation(Cell start) {
        // EMF default for startOrientation is NORTH; treat it as "unset" unless explicitly set.
        if (currentLevel != null && currentLevel.eIsSet(BlockyPackage.Literals.LEVEL__START_ORIENTATION)) {
            return currentLevel.getStartOrientation();
        }
        if (start == null) {
            return Direction.NORTH;
        }

        // Prefer any non-wall neighbour in a fixed order: NORTH, EAST, SOUTH, WEST.
        if (start.getTop() != null && start.getTop().getType() != CellType.WALL) {
            return Direction.NORTH;
        }
        if (start.getRight() != null && start.getRight().getType() != CellType.WALL) {
            return Direction.EAST;
        }
        if (start.getBottom() != null && start.getBottom().getType() != CellType.WALL) {
            return Direction.SOUTH;
        }
        if (start.getLeft() != null && start.getLeft().getType() != CellType.WALL) {
            return Direction.WEST;
        }

        // Fallback: keep a deterministic default.
        return Direction.NORTH;
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
            GridMap map = currentLevel.getMap();
            int maxSteps = map.getWidth() * map.getHeight() * 2;
            while (loop.getStatus() == GameStatus.RUNNING && loop.getPosition().getType() != CellType.GOAL) {
                if (loop.getStep() > maxSteps) {
                    loop.setStatus(GameStatus.CRASHED);
                    System.out.println("[GameEngine] Infinite loop detected! (exceeded " + maxSteps + " steps)");
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

    // --- Level Metadata Sync ---

    /**
     * Called from the JS bridge when the WebView reports level metadata.
     * JSON shape: { "level": K, "maxBlocks": Od, "startDirection": T,
     *               "allowLoops": bool, "allowConditionals": bool }
     * T encoding: 0=NORTH, 1=EAST, 2=SOUTH, 3=WEST (Blockly Maze always resets to T=1=EAST).
     */
    public void syncLevelMeta(String metaJson) {
        System.out.println("[GameEngine] Syncing level metadata: " + metaJson);
        try {
            int maxBlocks        = extractJsonInt(metaJson, "maxBlocks",        -1);
            int startDirCode     = extractJsonInt(metaJson, "startDirection",    1);
            int levelNum         = extractJsonInt(metaJson, "level",             1);
            boolean allowLoops   = extractJsonBool(metaJson, "allowLoops",   false);
            boolean allowConds   = extractJsonBool(metaJson, "allowConditionals", false);

            currentLevel.setId(levelNum);
            currentLevel.setTitle("Maze Level " + levelNum);
            currentLevel.setMaxBlocks(maxBlocks < 0 ? 0 : maxBlocks);
            currentLevel.setAllowLoops(allowLoops);
            currentLevel.setAllowConditionals(allowConds);

            Direction dir;
            switch (startDirCode) {
                case 0:  dir = Direction.NORTH; break;
                case 1:  dir = Direction.EAST;  break;
                case 2:  dir = Direction.SOUTH; break;
                case 3:  dir = Direction.WEST;  break;
                default: dir = Direction.EAST;
            }
            currentLevel.setStartOrientation(dir);

            System.out.println("[GameEngine] Level=" + levelNum
                    + ", maxBlocks=" + (maxBlocks < 0 ? "unlimited" : maxBlocks)
                    + ", startDir=" + dir
                    + ", allowLoops=" + allowLoops
                    + ", allowConditionals=" + allowConds);
        } catch (Exception e) {
            System.err.println("[GameEngine] Failed to parse level metadata: " + e.getMessage());
        }
    }

    private int extractJsonInt(String json, String key, int defaultVal) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + key + "\"\\s*:\\s*(-?[0-9]+)")
                .matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : defaultVal;
    }

    private boolean extractJsonBool(String json, String key, boolean defaultVal) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + key + "\"\\s*:\\s*(true|false)")
                .matcher(json);
        return m.find() ? Boolean.parseBoolean(m.group(1)) : defaultVal;
    }

    public void saveModel() {
        try {
            File saveFile = new File("blocky_game/save.xmi");
            if (!saveFile.getParentFile().exists()) saveFile = new File("save.xmi");
            resource.setURI(URI.createFileURI(saveFile.getAbsolutePath()));
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

    public Resource getResource() {
        return resource;
    }

    // --- XMI Load & Export for WebView ---

    /**
     * Loads a Level from an XMI file and sets it as the current level.
     * Subsequent saveModel() will write to this file.
     *
     * @param file the .xmi file to load
     * @throws IOException if the file cannot be read or parsed
     * @throws IllegalArgumentException if the root element is not a Level or level has no map
     */
    public void loadFromFile(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist: " + (file == null ? "null" : file.getAbsolutePath()));
        }
        BlockyPackage.eINSTANCE.eClass();
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

        ResourceSet resSet = new ResourceSetImpl();
        URI uri = URI.createFileURI(file.getAbsolutePath());
        Resource newResource = resSet.createResource(uri);
        newResource.load(null);

        if (newResource.getContents().isEmpty()) {
            throw new IllegalArgumentException("XMI file has no root element: " + file.getAbsolutePath());
        }
        Object root = newResource.getContents().get(0);
        if (!(root instanceof Level)) {
            throw new IllegalArgumentException("XMI root is not a Level: " + (root != null ? root.getClass().getName() : "null"));
        }
        Level loadedLevel = (Level) root;
        if (loadedLevel.getMap() == null) {
            throw new IllegalArgumentException("Level has no map.");
        }
        this.resource = newResource;
        this.currentLevel = loadedLevel;
        System.out.println("[GameEngine] Loaded level id=" + loadedLevel.getId() + " from " + file.getAbsolutePath());
    }

    /**
     * Converts the level's solution block tree to Blockly XML string (without outer &lt;xml&gt;).
     * Returns empty string if solution is null.
     */
    public String blockToXml(Block block) {
        if (block == null) return "";
        StringBuilder sb = new StringBuilder();
        appendBlockXml(block, sb);
        return sb.toString();
    }

    /**
     * Returns full Blockly XML document for the level's solution (with &lt;xml&gt; wrapper).
     * If solution is null, returns &lt;xml&gt;&lt;/xml&gt;.
     */
    public String solutionToBlocklyXml(Level level) {
        if (level == null || level.getSolution() == null) {
            return "<xml></xml>";
        }
        return "<xml>" + blockToXml(level.getSolution()) + "</xml>";
    }

    private void appendBlockXml(Block block, StringBuilder sb) {
        if (block == null) return;
        if (block instanceof MoveForward) {
            sb.append("<block type=\"maze_moveForward\">");
            if (block.getNext() != null) {
                sb.append("<next>");
                appendBlockXml(block.getNext(), sb);
                sb.append("</next>");
            }
            sb.append("</block>");
        } else if (block instanceof Turn) {
            Turn t = (Turn) block;
            String dir = t.getDirection() == TurnDirection.RIGHT ? "turnRight" : "turnLeft";
            sb.append("<block type=\"maze_turn\"><field name=\"DIR\">").append(escapeXml(dir)).append("</field>");
            if (block.getNext() != null) {
                sb.append("<next>");
                appendBlockXml(block.getNext(), sb);
                sb.append("</next>");
            }
            sb.append("</block>");
        } else if (block instanceof RepeatUntilGoal) {
            RepeatUntilGoal r = (RepeatUntilGoal) block;
            sb.append("<block type=\"maze_forever\">");
            sb.append("<statement name=\"DO\">");
            if (r.getBody() != null) {
                appendBlockXml(r.getBody(), sb);
            }
            sb.append("</statement>");
            if (block.getNext() != null) {
                sb.append("<next>");
                appendBlockXml(block.getNext(), sb);
                sb.append("</next>");
            }
            sb.append("</block>");
        } else if (block instanceof IfStatement) {
            IfStatement i = (IfStatement) block;
            String dirField = conditionToBlocklyDir(i.getCondition());
            boolean hasElse = i.getElseBranch() != null;
            String blockType = hasElse ? "maze_ifElse" : "maze_if";
            sb.append("<block type=\"").append(blockType).append("\">");
            sb.append("<field name=\"DIR\">").append(escapeXml(dirField)).append("</field>");
            sb.append("<statement name=\"DO\">");
            if (i.getThenBranch() != null) {
                appendBlockXml(i.getThenBranch(), sb);
            }
            sb.append("</statement>");
            if (hasElse) {
                sb.append("<statement name=\"ELSE\">");
                appendBlockXml(i.getElseBranch(), sb);
                sb.append("</statement>");
            }
            if (block.getNext() != null) {
                sb.append("<next>");
                appendBlockXml(block.getNext(), sb);
                sb.append("</next>");
            }
            sb.append("</block>");
        } else {
            if (block.getNext() != null) {
                appendBlockXml(block.getNext(), sb);
            }
        }
    }

    private static String conditionToBlocklyDir(SensorDirection c) {
        if (c == null) return "isPathForward";
        switch (c) {
            case AHEAD: return "isPathForward";
            case LEFT:  return "isPathLeft";
            case RIGHT: return "isPathRight";
            default:    return "isPathForward";
        }
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    /**
     * Builds the 2D grid for the WebView: X[row][col], values 0=WALL, 1=EMPTY, 2=START, 3=GOAL.
     * Dimensions are [height][width] to match JS X[row][col].
     */
    public int[][] buildGridForWebView(GridMap map) {
        if (map == null) return new int[0][0];
        int w = map.getWidth();
        int h = map.getHeight();
        int[][] grid = new int[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                grid[y][x] = 0; // default WALL
            }
        }
        for (Cell c : map.getCells()) {
            int x = c.getX();
            int y = c.getY();
            if (y >= 0 && y < h && x >= 0 && x < w) {
                switch (c.getType()) {
                    case WALL:  grid[y][x] = 0; break;
                    case EMPTY: grid[y][x] = 1; break;
                    case START: grid[y][x] = 2; break;
                    case GOAL:  grid[y][x] = 3; break;
                    default:   grid[y][x] = 1; break;
                }
            }
        }
        return grid;
    }

    /**
     * Finds the START cell in the map (for nd and pegman position).
     */
    public Cell getStartCell(GridMap map) {
        if (map == null) return null;
        for (Cell c : map.getCells()) {
            if (c.getType() == CellType.START) return c;
        }
        return null;
    }

    /**
     * Finds the GOAL cell in the map (for od).
     */
    public Cell getGoalCell(GridMap map) {
        if (map == null) return null;
        for (Cell c : map.getCells()) {
            if (c.getType() == CellType.GOAL) return c;
        }
        return null;
    }

    /**
     * Blockly Maze T value: NORTH=0, EAST=1, SOUTH=2, WEST=3.
     */
    public int directionToT(Direction d) {
        if (d == null) return 1;
        switch (d) {
            case NORTH: return 0;
            case EAST:  return 1;
            case SOUTH: return 2;
            case WEST:  return 3;
            default:    return 1;
        }
    }
}
