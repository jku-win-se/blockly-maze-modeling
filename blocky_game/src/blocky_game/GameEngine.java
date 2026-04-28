package blocky_game;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import blocky.*;
import blocky.BlockyPackage;

public class GameEngine {

    private Game currentGame;
    private Level currentLevel;
    private Resource resource;

    // Immediate feedback overlays (old vs new execution traces).
    // Computed when loading an XMI model.
    private int[][] pastPath = new int[0][0];
    private int[][] newPath = new int[0][0];

    // --- Debugger session state (Java-driven stepping) ---
    private ExecutionTrace debugTrace;
    private List<String> debugLogLines;
    private int debugIndex; // index into debugTrace.getStates()
    private boolean debugPaused;
    private boolean debugDirtySolution;
    private int debugStartX;
    private int debugStartY;
    private Direction debugStartDir;
    private int debugCurrentX;
    private int debugCurrentY;
    private Direction debugCurrentDir;
    private boolean debugSessionActive;

    private Cell findCellByXY(int x, int y) {
        if (currentLevel == null || currentLevel.getMap() == null || currentLevel.getMap().getCells() == null) return null;
        for (Cell c : currentLevel.getMap().getCells()) {
            if (c != null && c.getX() == x && c.getY() == y) return c;
        }
        return null;
    }

    private static void ensureLevelHasNonNullSolution(Level level) {
        if (level == null) return;
        if (level.getSolution() != null) return;
        Body b = BlockyFactory.eINSTANCE.createBody();
        Container c = BlockyFactory.eINSTANCE.createContainer();
        // Keep statement unset (null) — represents an empty program.
        b.setFirstContainer(c);
        level.setSolution(b);
    }

    private static void ensureGameHasNonNullSolutions(Game game) {
        if (game == null || game.getLevels() == null) return;
        for (Level lvl : game.getLevels()) {
            ensureLevelHasNonNullSolution(lvl);
        }
    }

    private void clearDirectManipulationGoal() {
        if (currentLevel == null || currentLevel.getMap() == null) return;
        for (Cell c : currentLevel.getMap().getCells()) {
            if (c != null && c.getType() == CellType.DMG) {
                c.setType(CellType.EMPTY);
            }
        }
    }

    /**
     * Teleport pegman to a specific map cell for direct manipulation.
     * This is allowed only onto EMPTY or GOAL cells.
     *
     * Also updates the engine's notion of the current situation by creating a single-state trace
     * at the target position and direction.
     *
     * @param x cell x
     * @param y cell y
     * @param t direction code (0=N,1=E,2=S,3=W)
     */
    public void teleportPegman(int x, int y, int t) {
        if (currentLevel == null || currentLevel.getMap() == null) return;

        Cell target = findCellByXY(x, y);
        if (target == null) {
            System.err.println("[GameEngine] teleportPegman rejected: no cell at x=" + x + " y=" + y);
            return;
        }
        if (!(target.getType() == CellType.EMPTY || target.getType() == CellType.GOAL)) {
            System.err.println("[GameEngine] teleportPegman rejected: target type=" + target.getType() + " at x=" + x + " y=" + y);
            return;
        }

        Direction dir = blocky_game.DebuggingService.tToDirection(t);

        // Mark DM goal: the clicked cell becomes a DMG marker unless it's already the real GOAL.
        // DMG is an intermediate target for synthesis and UI display; GOAL remains the semantic level goal.
        clearDirectManipulationGoal();
        if (target.getType() == CellType.EMPTY) {
            target.setType(CellType.DMG);
        }

        // Update debugger snapshot (so Resume/Step continues from here if paused).
        debugCurrentX = x;
        debugCurrentY = y;
        debugCurrentDir = dir;

        // If no debug session ever started, also treat this as the current start snapshot.
        if (debugTrace == null) {
            debugStartX = x;
            debugStartY = y;
            debugStartDir = dir;
        }

        // Replace traces with a minimal "current situation" trace at the teleported state.
        currentLevel.getTraces().clear();
        ExecutionTrace trace = BlockyFactory.eINSTANCE.createExecutionTrace();
        currentLevel.getTraces().add(trace);
        GameState initialState = BlockyFactory.eINSTANCE.createGameState();
        initialState.setStep(0);
        initialState.setPosition(target);
        initialState.setOrientation(dir);
        initialState.setStatus(GameStatus.RUNNING);
        trace.getStates().add(initialState);

        // If a debug session is active, recompute trace from the new snapshot when paused/next step.
        if (debugSessionActive) {
            debugDirtySolution = true;
        }

        // Persist a direct manipulation request model for downstream tools (MoMoT).
        saveDirectManipulationRequestXmi();
        saveMomotInputRequestXmi();
    }

    /**
     * Saves a deterministic XMI snapshot representing the current situation after direct manipulation.
     * This does not change the engine's primary {@link #resource} URI.
     */
    public void saveDirectManipulationRequestXmi() {
        try {
            File out = new File("blocky_game/direct_manipulation_request.xmi");
            if (!out.getParentFile().exists()) out = new File("direct_manipulation_request.xmi");

            BlockyPackage.eINSTANCE.eClass();
            Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

            ResourceSet resSet = new ResourceSetImpl();
            Resource outRes = resSet.createResource(URI.createFileURI(out.getAbsolutePath()));

            Game snapshot = currentGame != null ? EcoreUtil.copy(currentGame) : null;
            ensureGameHasNonNullSolutions(snapshot);
            if (snapshot != null) {
                outRes.getContents().add(snapshot);
                outRes.save(null);
                System.out.println("[GameEngine] Direct manipulation request saved to: " + outRes.getURI().toFileString());
            }
        } catch (Exception e) {
            System.err.println("[GameEngine] Direct manipulation request save failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Saves the DM request model to the default MoMoT input location.
     * This is intended to be used as the search.model.file for automated MoMoT runs.
     */
    public void saveMomotInputRequestXmi() {
        try {
            File out = new File("blocky_momot/model/input/direct_manipulation_request.xmi");
            if (!out.getParentFile().exists()) {
                out = new File("../blocky_momot/model/input/direct_manipulation_request.xmi");
            }
            if (!out.getParentFile().exists()) {
                // best-effort: fall back to repo root
                out = new File("direct_manipulation_request_momot.xmi");
            } else {
                out.getParentFile().mkdirs();
            }

            BlockyPackage.eINSTANCE.eClass();
            Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

            ResourceSet resSet = new ResourceSetImpl();
            Resource outRes = resSet.createResource(URI.createFileURI(out.getAbsolutePath()));

            Game snapshot = currentGame != null ? EcoreUtil.copy(currentGame) : null;
            ensureGameHasNonNullSolutions(snapshot);
            if (snapshot != null) {
                outRes.getContents().add(snapshot);
                outRes.save(null);
                System.out.println("[GameEngine] MoMoT DM request saved to: " + outRes.getURI().toFileString());
            }
        } catch (Exception e) {
            System.err.println("[GameEngine] MoMoT DM request save failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

        // Root object is now Game, which contains one or more Levels.
        currentGame = BlockyFactory.eINSTANCE.createGame();
        currentLevel = BlockyFactory.eINSTANCE.createLevel();
        currentGame.getLevels().add(currentLevel);
        resource.getContents().add(currentGame);
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

    // --- Program / statement management (Body + linked Statements) ---

    public void rebuildProgram(java.util.List<Map<String, Object>> blockData) {
        System.out.println("[GameEngine] Rebuilding model solution...");
        // If the user is editing while paused in the debugger, we need to recompute on the next
        // Resume/Step using the updated solution blocks.
        if (debugTrace != null && debugPaused) {
            debugDirtySolution = true;
        }

        currentLevel.getTraces().clear(); // Clear old traces—they reference old solution blocks
        currentLevel.setSolution(null);
        if (blockData == null || blockData.isEmpty()) {
            System.out.println("[GameEngine] Program cleared.");
            return;
        }

        currentLevel.setSolution(buildSolutionBody(blockData));
        System.out.println("[GameEngine] Solution rebuilt. Main sequence length: " + blockData.size());
    }

    private static Body emptyBody() {
        return BlockyFactory.eINSTANCE.createBody();
    }

    private static Body wrapContainerChain(Container head) {
        Body b = BlockyFactory.eINSTANCE.createBody();
        b.setFirstContainer(head);
        return b;
    }

    private Container createContainerChainFromData(Map<String, Object> data) {
        if (data == null) return null;

        Statement stmt = createStatementFromData(data);
        if (stmt == null) return null;

        Container c = BlockyFactory.eINSTANCE.createContainer();
        c.setStatement(stmt);

        Map<String, Object> nextData = (Map<String, Object>) data.get("next");
        if (nextData != null) {
            c.setNext(createContainerChainFromData(nextData));
        }
        return c;
    }

    private Statement createStatementFromData(Map<String, Object> data) {
        String type = (String) data.get("type");
        Statement stmt = null;

        if ("maze_moveForward".equals(type) || "move_forward".equals(type)) {
            AtomicStatement a = BlockyFactory.eINSTANCE.createAtomicStatement();
            a.setKind(AtomicStatementKind.MOVE_FORWARD);
            stmt = a;
        } else if ("maze_turn".equals(type) || "turn_left".equals(type) || "turn_right".equals(type)) {
            String dir = (String) data.get("DIR");
            System.out.println("[GameEngine]   Turn block: type=" + type + ", DIR field=" + dir + ", all keys=" + data.keySet());
            if (dir == null) {
                dir = type;
            }
            boolean right = dir.toLowerCase().contains("right");
            AtomicStatement a = BlockyFactory.eINSTANCE.createAtomicStatement();
            a.setKind(right ? AtomicStatementKind.TURN_RIGHT : AtomicStatementKind.TURN_LEFT);
            stmt = a;
            System.out.println("[GameEngine]   -> resolved dir=" + dir + " -> " + a.getKind());
        } else if ("maze_forever".equals(type) || "repeat_until_goal".equals(type)) {
            Loop r = BlockyFactory.eINSTANCE.createLoop();
            Map<String, Object> bodyData = (Map<String, Object>) data.get("body");
            r.setBody(bodyData != null ? wrapContainerChain(createContainerChainFromData(bodyData)) : emptyBody());
            stmt = r;
        } else if (type != null && (type.startsWith("maze_if") || type.startsWith("if_path"))) {
            IfStmt i = BlockyFactory.eINSTANCE.createIfStmt();
            String dir = (String) data.get("DIR");
            ConditionKind ck = dir != null ? parseCondition(dir) : parseCondition(type);
            i.setCondition(ck);
            Map<String, Object> thenData = (Map<String, Object>) data.get("body");
            if (thenData == null) {
                thenData = (Map<String, Object>) data.get("then");
            }
            i.setThenBody(wrapContainerChain(thenData != null ? createContainerChainFromData(thenData) : null));
            Map<String, Object> elseData = (Map<String, Object>) data.get("elseBranch");
            if (elseData == null) {
                elseData = (Map<String, Object>) data.get("else");
            }
            if (elseData != null) {
                i.setElseBody(wrapContainerChain(createContainerChainFromData(elseData)));
            }
            stmt = i;
        }

        return stmt;
    }

    private Body buildSolutionBody(java.util.List<Map<String, Object>> dataList) {
        // Blockly typically serializes the whole program as ONE top-level <block>
        // with a linked <next> chain. Our XML parser preserves that as nested "next" maps,
        // so we must convert the nested chain into linked Containers.
        Body body = BlockyFactory.eINSTANCE.createBody();
        if (dataList == null || dataList.isEmpty()) {
            body.setFirstContainer(null);
            return body;
        }

        Container first = null;
        Container last = null;

        // If multiple top-level blocks exist, we concatenate their chains in order.
        for (Map<String, Object> data : dataList) {
            Container chainHead = createContainerChainFromData(data);
            if (chainHead == null) continue;

            if (first == null) {
                first = chainHead;
                last = chainHead;
            } else {
                last.setNext(chainHead);
            }

            // Advance last to the end of the appended chain.
            Container cur = chainHead;
            while (cur.getNext() != null) {
                cur = cur.getNext();
            }
            last = cur;
        }

        body.setFirstContainer(first);
        return body;
    }

    private static ConditionKind parseCondition(String type) {
        String lower = type.toLowerCase();
        if (lower.contains("forward") || lower.contains("ahead"))
            return ConditionKind.CHECK_FORWARD;
        if (lower.contains("left"))
            return ConditionKind.CHECK_LEFT;
        if (lower.contains("right"))
            return ConditionKind.CHECK_RIGHT;
        return ConditionKind.CHECK_FORWARD;
    }

    // --- Simulation ---

    public void simulateUserProgram() {
        // Keep existing behavior (stdout + save) for callers that don't need logs.
        simulateUserProgramWithLogs();
    }

    /**
     * Runs the current solution and returns a high-level, step-by-step log.
     * Also saves the model (including execution trace) at the end (same as simulateUserProgram()).
     */
    public List<String> simulateUserProgramWithLogs() {
        List<String> logs = new ArrayList<>();
        System.out.println("\n[GameEngine] Starting simulation...");
        logs.add("Simulation start");
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
        logs.add("Start: " + getPosStr(startNode) + " dir=" + initialState.getOrientation());

        executeBodyWithLogs(currentLevel.getSolution(), initialState, trace, logs);
        saveModel();
        System.out.println("[GameEngine] Simulation finished. Model (with execution trace) saved.\n");
        GameState last = trace.getStates().isEmpty() ? null : trace.getStates().get(trace.getStates().size() - 1);
        if (last != null) {
            if (last.getStatus() == GameStatus.WON) logs.add("Result: GOAL");
            else if (last.getStatus() == GameStatus.CRASHED) logs.add("Result: CRASH");
            else logs.add("Result: " + last.getStatus());
        }
        return logs;
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

    private GameState executeBody(Body body, GameState state, ExecutionTrace trace) {
        if (body == null) {
            return state;
        }
        return executeContainerChain(body.getFirstContainer(), state, trace);
    }

    private GameState executeBodyWithLogs(Body body, GameState state, ExecutionTrace trace, List<String> logs) {
        if (body == null) {
            return state;
        }
        return executeContainerChainWithLogs(body.getFirstContainer(), state, trace, logs);
    }

    private GameState executeContainerChain(Container first, GameState state, ExecutionTrace trace) {
        Container current = first;
        GameState last = state;
        while (current != null && last.getStatus() == GameStatus.RUNNING) {
            last = executeSingle(current.getStatement(), last, trace);
            current = current.getNext();
        }
        return last;
    }

    private GameState executeContainerChainWithLogs(Container first, GameState state, ExecutionTrace trace, List<String> logs) {
        Container current = first;
        GameState last = state;
        while (current != null && last.getStatus() == GameStatus.RUNNING) {
            last = executeSingleWithLogs(current.getStatement(), last, trace, logs);
            current = current.getNext();
        }
        return last;
    }

    private static SensorDirection conditionKindToSensor(ConditionKind ck) {
        if (ck == ConditionKind.CHECK_LEFT) {
            return SensorDirection.LEFT;
        }
        if (ck == ConditionKind.CHECK_RIGHT) {
            return SensorDirection.RIGHT;
        }
        return SensorDirection.AHEAD;
    }

    private boolean checkCondition(GameState state, ConditionKind ck) {
        return checkSensor(state, conditionKindToSensor(ck));
    }

    private GameState executeSingle(Statement stmt, GameState prev, ExecutionTrace trace) {
        GameState next = BlockyFactory.eINSTANCE.createGameState();
        next.setStep(prev.getStep() + 1);
        next.setOrientation(prev.getOrientation());
        next.setPosition(prev.getPosition());
        next.setStatus(GameStatus.RUNNING);
        next.setExecutingStatement(stmt);
        next.setPrevious(prev);
        trace.getStates().add(next);

        String typeName = stmt.getClass().getSimpleName().replace("Impl", "");
        System.out.print("[GameEngine] Step " + next.getStep() + ": " + typeName + " -> ");

        if (stmt instanceof AtomicStatement) {
            AtomicStatement a = (AtomicStatement) stmt;
            switch (a.getKind()) {
            case MOVE_FORWARD: {
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
                break;
            }
            case TURN_LEFT:
                next.setOrientation(getRelativeDir(next.getOrientation(), SensorDirection.LEFT));
                System.out.println("TurnLeft. New Dir: " + next.getOrientation());
                break;
            case TURN_RIGHT:
                next.setOrientation(getRelativeDir(next.getOrientation(), SensorDirection.RIGHT));
                System.out.println("TurnRight. New Dir: " + next.getOrientation());
                break;
            default:
                break;
            }
        } else if (stmt instanceof Loop) {
            System.out.println("Loop Start");
            Loop r = (Loop) stmt;
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
                loop = executeBody(r.getBody(), loop, trace);
                if (loop.getStep() == previousStep) {
                    loop.setStatus(GameStatus.CRASHED);
                    System.out.println("[GameEngine] Empty loop body or zero progress in loop! Crashing.");
                    break;
                }
            }
            return loop;
        } else if (stmt instanceof IfStmt) {
            IfStmt i = (IfStmt) stmt;
            boolean cond = checkCondition(next, i.getCondition());
            System.out.println("If (" + conditionKindToSensor(i.getCondition()) + ") is " + cond);
            if (cond) {
                return executeBody(i.getThenBody(), next, trace);
            }
            if (i.getElseBody() != null) {
                return executeBody(i.getElseBody(), next, trace);
            }
        }
        return next;
    }

    private GameState executeSingleWithLogs(Statement stmt, GameState prev, ExecutionTrace trace, List<String> logs) {
        GameState next = BlockyFactory.eINSTANCE.createGameState();
        next.setStep(prev.getStep() + 1);
        next.setOrientation(prev.getOrientation());
        next.setPosition(prev.getPosition());
        next.setStatus(GameStatus.RUNNING);
        next.setExecutingStatement(stmt);
        next.setPrevious(prev);
        trace.getStates().add(next);

        String typeName = stmt != null ? stmt.getClass().getSimpleName().replace("Impl", "") : "null";

        if (stmt instanceof AtomicStatement) {
            AtomicStatement a = (AtomicStatement) stmt;
            switch (a.getKind()) {
            case MOVE_FORWARD: {
                Cell target = getAdjacent(next.getPosition(), next.getOrientation());
                if (target == null || target.getType() == CellType.WALL) {
                    next.setStatus(GameStatus.CRASHED);
                    if (logs != null) logs.add("Step " + next.getStep() + ": MoveForward -> CRASH at " + getPosStr(next.getPosition()));
                } else {
                    next.setPosition(target);
                    if (target.getType() == CellType.GOAL) {
                        next.setStatus(GameStatus.WON);
                        if (logs != null) logs.add("Step " + next.getStep() + ": MoveForward -> " + getPosStr(target) + " GOAL");
                    } else {
                        if (logs != null) logs.add("Step " + next.getStep() + ": MoveForward -> " + getPosStr(target));
                    }
                }
                break;
            }
            case TURN_LEFT: {
                Direction before = next.getOrientation();
                next.setOrientation(getRelativeDir(next.getOrientation(), SensorDirection.LEFT));
                if (logs != null) logs.add("Step " + next.getStep() + ": TurnLeft -> " + before + "→" + next.getOrientation());
                break;
            }
            case TURN_RIGHT: {
                Direction before = next.getOrientation();
                next.setOrientation(getRelativeDir(next.getOrientation(), SensorDirection.RIGHT));
                if (logs != null) logs.add("Step " + next.getStep() + ": TurnRight -> " + before + "→" + next.getOrientation());
                break;
            }
            default:
                if (logs != null) logs.add("Step " + next.getStep() + ": " + typeName);
                break;
            }
        } else if (stmt instanceof Loop) {
            if (logs != null) logs.add("Step " + next.getStep() + ": Loop");
            Loop r = (Loop) stmt;
            GameState loop = next;
            GridMap map = currentLevel.getMap();
            int maxSteps = map.getWidth() * map.getHeight() * 2;
            while (loop.getStatus() == GameStatus.RUNNING && loop.getPosition().getType() != CellType.GOAL) {
                if (loop.getStep() > maxSteps) {
                    loop.setStatus(GameStatus.CRASHED);
                    if (logs != null) logs.add("Result: INFINITE_LOOP (bound " + maxSteps + " exceeded)");
                    break;
                }
                int previousStep = loop.getStep();
                loop = executeBodyWithLogs(r.getBody(), loop, trace, logs);
                if (loop.getStep() == previousStep) {
                    loop.setStatus(GameStatus.CRASHED);
                    if (logs != null) logs.add("Result: CRASH (empty loop body / no progress)");
                    break;
                }
            }
            return loop;
        } else if (stmt instanceof IfStmt) {
            IfStmt i = (IfStmt) stmt;
            boolean cond = checkCondition(next, i.getCondition());
            if (logs != null) {
                String branch = cond ? "then" : (i.getElseBody() != null ? "else" : "skip");
                logs.add("Step " + next.getStep() + ": If " + conditionKindToSensor(i.getCondition()) + " -> " + cond + " (" + branch + ")");
            }
            if (cond) {
                return executeBodyWithLogs(i.getThenBody(), next, trace, logs);
            }
            if (i.getElseBody() != null) {
                return executeBodyWithLogs(i.getElseBody(), next, trace, logs);
            }
        } else {
            if (logs != null) logs.add("Step " + next.getStep() + ": " + typeName);
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

            // Blockly's startDirection is frequently unreliable (often defaulting to EAST).
            // Prefer an explicitly stored model value; otherwise derive it from the map layout.
            Direction dir = null;
            if (currentLevel != null && currentLevel.eIsSet(BlockyPackage.Literals.LEVEL__START_ORIENTATION)) {
                dir = currentLevel.getStartOrientation();
            } else if (currentLevel != null && currentLevel.getMap() != null) {
                Cell startCell = getStartCell(currentLevel.getMap());
                dir = determineStartOrientation(startCell);
            }
            if (dir == null) {
                switch (startDirCode) {
                    case 0:  dir = Direction.NORTH; break;
                    case 1:  dir = Direction.EAST;  break;
                    case 2:  dir = Direction.SOUTH; break;
                    case 3:  dir = Direction.WEST;  break;
                    default: dir = Direction.EAST;
                }
            }
            if (!debugSessionActive) {
                currentLevel.setStartOrientation(dir);
            } else {
                System.out.println("[GameEngine] Ignoring startDirection update during active debug session.");
            }

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
            // Ensure XMI always contains a non-null solution container to avoid downstream tooling issues.
            ensureLevelHasNonNullSolution(currentLevel);
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

    public Game getCurrentGame() {
        return currentGame;
    }

    public Resource getResource() {
        return resource;
    }

    // --- Immediate Feedback getters (old vs new path) ---
    public int[][] getPastPath() {
        return pastPath != null ? pastPath : new int[0][0];
    }

    public int[][] getNewPath() {
        return newPath != null ? newPath : new int[0][0];
    }

    // --- XMI Load & Export for WebView ---

    /**
     * Loads a model from an XMI file and sets the current game/level.
     * Subsequent saveModel() will write to this file.
     *
     * Accepts both:
     * - Game root (new model)
     * - Level root (legacy) — will be wrapped into a Game root in-memory
     *
     * @param file the .xmi file to load
     * @throws IOException if the file cannot be read or parsed
     * @throws IllegalArgumentException if the root element is not compatible or level has no map
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

        Game loadedGame;
        Level loadedLevel;

        if (root instanceof Game) {
            loadedGame = (Game) root;
            if (loadedGame.getLevels().isEmpty()) {
                throw new IllegalArgumentException("Game has no levels.");
            }
            loadedLevel = loadedGame.getLevels().get(0);
        } else if (root instanceof Level) {
            loadedLevel = (Level) root;
            loadedGame = BlockyFactory.eINSTANCE.createGame();
            loadedGame.getLevels().add(loadedLevel);
            newResource.getContents().clear();
            newResource.getContents().add(loadedGame);
        } else {
            throw new IllegalArgumentException("XMI root is neither Game nor Level: "
                    + (root != null ? root.getClass().getName() : "null"));
        }

        if (loadedLevel.getMap() == null) {
            throw new IllegalArgumentException("Level has no map.");
        }
        this.resource = newResource;
        this.currentGame = loadedGame;
        this.currentLevel = loadedLevel;

        // If the XMI doesn't explicitly store a start orientation, derive a stable one from the map.
        // This prevents stale/default WebView metadata (often EAST) from "winning" after a model load.
        if (this.currentLevel != null
                && !this.currentLevel.eIsSet(BlockyPackage.Literals.LEVEL__START_ORIENTATION)
                && this.currentLevel.getMap() != null) {
            Cell startCell = getStartCell(this.currentLevel.getMap());
            Direction derived = determineStartOrientation(startCell);
            this.currentLevel.setStartOrientation(derived);
        }
        try {
            // Compute old (stored) vs new (re-simulated) paths for immediate feedback.
            ImmediateFeedbackService.Paths paths = ImmediateFeedbackService.computePaths(loadedLevel);
            this.pastPath = paths.pastPath;
            this.newPath = paths.newPath;
        } catch (Exception e) {
            System.err.println("[GameEngine] ImmediateFeedback compute failed: " + e.getMessage());
            this.pastPath = new int[0][0];
            this.newPath = new int[0][0];
        }

        System.out.println("[GameEngine] Loaded level id=" + loadedLevel.getId() + " from " + file.getAbsolutePath());
    }

    // --- Debugger Controls (Java-driven stepping) ---

    /**
     * Start a debug session from a specific WebView pegman state.
     * This computes a full trace and resets debugIndex to 0.
     *
     * @param startX cell x (WebView "Q")
     * @param startY cell y (WebView "S")
     * @param startT direction code (WebView "T": 0=N,1=E,2=S,3=W)
     * @return JSON frame describing current debug state for the UI
     */
    public String debugStart(int startX, int startY, int startT) {
        if (currentLevel == null) {
            return "{\"index\":0,\"total\":0}";
        }
        debugStartX = startX;
        debugStartY = startY;
        debugStartDir = blocky_game.DebuggingService.tToDirection(startT);
        debugCurrentX = startX;
        debugCurrentY = startY;
        debugCurrentDir = debugStartDir;
        debugPaused = true;
        debugDirtySolution = false;
        debugSessionActive = true;

        blocky_game.DebuggingService.DebugTraceResult result =
                blocky_game.DebuggingService.computeTraceFromState(currentLevel, startX, startY, debugStartDir);
        debugTrace = result.trace;
        debugLogLines = result.logLines;
        debugIndex = 0;

        // Return frame for UI rendering.
        return debugFrameJson();
    }

    public String debugTogglePause() {
        if (debugTrace == null) {
            // Not started yet; UI should call debugStart first.
            return debugFrameJson();
        }
        debugPaused = !debugPaused;
        if (!debugPaused && debugDirtySolution) {
            // Recompute from the current state snapshot with updated solution blocks.
            recomputeDebugTraceFromCurrentState();
            debugIndex = 0;
            debugDirtySolution = false;
        }
        return debugFrameJson();
    }

    public String debugStepOnce() {
        if (currentLevel == null) return "{\"index\":0,\"total\":0}";
        if (debugTrace == null) {
            // Can't step without a trace; UI should have called debugStart.
            return debugFrameJson();
        }

        if (debugDirtySolution) {
            recomputeDebugTraceFromCurrentState();
            debugIndex = 0;
            debugDirtySolution = false;
        }

        int lastIndex = debugTrace.getStates().size() - 1;
        if (debugIndex < lastIndex) {
            debugIndex++;
        }
        debugPaused = true;
        syncCurrentStateSnapshotFromTraceIndex();
        return debugFrameJson();
    }

    public String debugStop() {
        if (currentLevel == null) return "{\"index\":0,\"total\":0}";
        if (debugTrace == null) return debugFrameJson();

        debugPaused = true;
        debugCurrentX = debugStartX;
        debugCurrentY = debugStartY;
        debugCurrentDir = debugStartDir;
        debugIndex = 0;

        // If solution changed while paused, recompute immediately so overlays/pegman stay consistent.
        if (debugDirtySolution) {
            recomputeDebugTraceFromCurrentState();
            debugDirtySolution = false;
        }
        debugSessionActive = false;

        return debugFrameJson();
    }

    /**
     * Jump directly to the final debug state and pause there.
     * Useful for quickly reaching GOAL/CRASH/INFINITE_LOOP outcome.
     */
    public String debugSkipToEnd() {
        if (debugTrace == null || debugTrace.getStates() == null || debugTrace.getStates().isEmpty()) {
            return debugFrameJson();
        }
        debugIndex = debugTrace.getStates().size() - 1;
        debugPaused = true;
        syncCurrentStateSnapshotFromTraceIndex();
        return debugFrameJson();
    }

    /**
     * Called periodically while running (Resume pressed).
     * If paused or at end, returns the current frame.
     */
    public String debugTick() {
        if (debugTrace == null) return debugFrameJson();
        if (debugPaused) return debugFrameJson();

        // Advance one state.
        int lastIndex = debugTrace.getStates().size() - 1;
        if (debugIndex < lastIndex) {
            debugIndex++;
            syncCurrentStateSnapshotFromTraceIndex();
        } else {
            // End reached; pause.
            debugPaused = true;
        }

        if (debugDirtySolution) {
            // If user edited blocks while we were running, pause + recompute from current snapshot.
            recomputeDebugTraceFromCurrentState();
            debugIndex = 0;
            debugDirtySolution = false;
            debugPaused = true;
        }

        return debugFrameJson();
    }

    private void recomputeDebugTraceFromCurrentState() {
        if (currentLevel == null) return;
        Direction dir = debugCurrentDir != null ? debugCurrentDir : Direction.EAST;
        blocky_game.DebuggingService.DebugTraceResult result =
                blocky_game.DebuggingService.computeTraceFromState(currentLevel, debugCurrentX, debugCurrentY, dir);
        debugTrace = result.trace;
        debugLogLines = result.logLines;
        debugIndex = 0;
    }

    private void syncCurrentStateSnapshotFromTraceIndex() {
        if (debugTrace == null || debugTrace.getStates() == null || debugTrace.getStates().isEmpty()) return;
        int safeIndex = Math.max(0, Math.min(debugIndex, debugTrace.getStates().size() - 1));
        GameState s = debugTrace.getStates().get(safeIndex);
        Cell pos = s != null ? s.getPosition() : null;
        if (pos != null) {
            debugCurrentX = pos.getX();
            debugCurrentY = pos.getY();
        }
        debugCurrentDir = s != null ? s.getOrientation() : debugCurrentDir;
    }

    private String debugFrameJson() {
        if (currentLevel == null || debugTrace == null || debugTrace.getStates() == null) {
            return "{\"index\":0,\"total\":0,\"q\":0,\"s\":0,\"t\":0,\"prefix\":[]}";
        }
        List<GameState> states = debugTrace.getStates();
        int total = states.size();
        int safeIndex = Math.max(0, Math.min(debugIndex, total - 1));
        GameState s = states.get(safeIndex);
        Cell pos = s != null ? s.getPosition() : null;
        Direction dir = s != null ? s.getOrientation() : debugCurrentDir;
        int q = pos != null ? pos.getX() : debugCurrentX;
        int r = pos != null ? pos.getY() : debugCurrentY;
        int t = blocky_game.DebuggingService.directionToT(dir);

        // Prefix path up to current index (in trace-state order).
        int prefixLen = safeIndex + 1;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < prefixLen; i++) {
            GameState ps = states.get(i);
            Cell ppos = ps != null ? ps.getPosition() : null;
            int px = ppos != null ? ppos.getX() : 0;
            int py = ppos != null ? ppos.getY() : 0;
            if (i > 0) sb.append(",");
            sb.append("[").append(px).append(",").append(py).append("]");
        }
        sb.append("]");

        String result = "RUNNING";
        if (safeIndex >= total - 1) {
            GameStatus st = s != null ? s.getStatus() : null;
            if (st == GameStatus.WON) {
                result = "GOAL";
            } else if (st == GameStatus.CRASHED) {
                // Heuristic: crashes near/above loop bound are typically infinite loops.
                int loopBound = 0;
                if (currentLevel != null && currentLevel.getMap() != null) {
                    loopBound = currentLevel.getMap().getWidth() * currentLevel.getMap().getHeight() * 2;
                }
                result = (s != null && loopBound > 0 && s.getStep() >= loopBound) ? "INFINITE_LOOP" : "CRASH";
            }
        }

        String logLine = null;
        if (debugLogLines != null && safeIndex >= 0 && safeIndex < debugLogLines.size()) {
            logLine = debugLogLines.get(safeIndex);
        }
        if (logLine == null) {
            logLine = "Step " + (s != null ? s.getStep() : safeIndex) + ": (no log)";
        }
        logLine = escapeJsonString(logLine);

        return "{\"index\":" + safeIndex
                + ",\"total\":" + total
                + ",\"q\":" + q
                + ",\"s\":" + r
                + ",\"t\":" + t
                + ",\"prefix\":" + sb.toString()
                + ",\"paused\":" + debugPaused
                + ",\"result\":\"" + result + "\""
                + ",\"logLine\":\"" + logLine + "\""
                + "}";
    }

    private static String escapeJsonString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    /**
     * Converts a linked list of {@link Container}s to Blockly XML (without outer &lt;xml&gt;).
     */
    public String statementChainToXml(Container first) {
        if (first == null) return "";
        StringBuilder sb = new StringBuilder();
        appendStatementXml(first, sb);
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
        return "<xml>" + statementChainToXml(level.getSolution().getFirstContainer()) + "</xml>";
    }

    private void appendStatementXml(Container c, StringBuilder sb) {
        if (c == null) return;
        Statement stmt = c.getStatement();
        if (stmt == null) return;

        if (stmt instanceof AtomicStatement) {
            AtomicStatement a = (AtomicStatement) stmt;
            if (a.getKind() == AtomicStatementKind.MOVE_FORWARD) {
                sb.append("<block type=\"maze_moveForward\">");
            } else if (a.getKind() == AtomicStatementKind.TURN_LEFT) {
                sb.append("<block type=\"maze_turn\"><field name=\"DIR\">turnLeft</field>");
            } else if (a.getKind() == AtomicStatementKind.TURN_RIGHT) {
                sb.append("<block type=\"maze_turn\"><field name=\"DIR\">turnRight</field>");
            }
            if (a.getKind() == AtomicStatementKind.MOVE_FORWARD || a.getKind() == AtomicStatementKind.TURN_LEFT
                    || a.getKind() == AtomicStatementKind.TURN_RIGHT) {
                if (c.getNext() != null) {
                    sb.append("<next>");
                    appendStatementXml(c.getNext(), sb);
                    sb.append("</next>");
                }
                sb.append("</block>");
            }
        } else if (stmt instanceof Loop) {
            Loop r = (Loop) stmt;
            sb.append("<block type=\"maze_forever\">");
            sb.append("<statement name=\"DO\">");
            if (r.getBody() != null) {
                appendStatementXml(r.getBody().getFirstContainer(), sb);
            }
            sb.append("</statement>");
            if (c.getNext() != null) {
                sb.append("<next>");
                appendStatementXml(c.getNext(), sb);
                sb.append("</next>");
            }
            sb.append("</block>");
        } else if (stmt instanceof IfStmt) {
            IfStmt i = (IfStmt) stmt;
            String dirField = conditionToBlocklyDir(i.getCondition());
            boolean hasElse = i.getElseBody() != null;
            String blockType = hasElse ? "maze_ifElse" : "maze_if";
            sb.append("<block type=\"").append(blockType).append("\">");
            sb.append("<field name=\"DIR\">").append(escapeXml(dirField)).append("</field>");
            sb.append("<statement name=\"DO\">");
            if (i.getThenBody() != null) {
                appendStatementXml(i.getThenBody().getFirstContainer(), sb);
            }
            sb.append("</statement>");
            if (hasElse) {
                sb.append("<statement name=\"ELSE\">");
                appendStatementXml(i.getElseBody().getFirstContainer(), sb);
                sb.append("</statement>");
            }
            if (c.getNext() != null) {
                sb.append("<next>");
                appendStatementXml(c.getNext(), sb);
                sb.append("</next>");
            }
            sb.append("</block>");
        } else {
            if (c.getNext() != null) {
                appendStatementXml(c.getNext(), sb);
            }
        }
    }

    private static String conditionToBlocklyDir(ConditionKind ck) {
        if (ck == ConditionKind.CHECK_LEFT) return "isPathLeft";
        if (ck == ConditionKind.CHECK_RIGHT) return "isPathRight";
        return "isPathForward";
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
                    case DMG:   grid[y][x] = 1; break; // treat as EMPTY; visual marker uses injected od
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
     * Finds the Direct Manipulation Goal (DMG) cell in the map (if any).
     * DMG is an intermediate target used for synthesis, distinct from the level's GOAL.
     */
    public Cell getDmgCell(GridMap map) {
        if (map == null) return null;
        for (Cell c : map.getCells()) {
            if (c.getType() == CellType.DMG) return c;
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
