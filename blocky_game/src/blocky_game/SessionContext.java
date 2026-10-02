package blocky_game;

import blocky.Level;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Encapsulates per-guest session state, including an independent GameEngine,
 * session workspace directory for MOMoT runs, and status tracking.
 */
public class SessionContext {

    private final String sessionId;
    private final GameEngine engine;
    private final File sessionDir;
    private volatile long lastAccessedTime;

    private volatile boolean isMomotRunning;
    private volatile String momotStatus = "Idle";
    private volatile Thread momotThread;
    private final List<String> momotLogBuffer = new CopyOnWriteArrayList<>();
    private volatile String momotCurrentOutputDir;
    /** Level id that {@link #momotCurrentOutputDir} belongs to. -1 means no active results. */
    private volatile int momotCurrentLevelId = -1;
    /**
     * Bumped when a search starts or the level changes so a finishing search from a
     * previous level cannot overwrite this session's output directory or status.
     */
    private final AtomicLong searchGeneration = new AtomicLong();

    public SessionContext(String sessionId) {
        this.sessionId = Objects.requireNonNull(sessionId);
        this.engine = new GameEngine();
        this.engine.initializeGame();
        this.lastAccessedTime = System.currentTimeMillis();

        File baseDir = new File("momot_sessions");
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        this.sessionDir = new File(baseDir, "session_" + sessionId);
        if (!this.sessionDir.exists()) {
            this.sessionDir.mkdirs();
        }
    }

    public void touch() {
        this.lastAccessedTime = System.currentTimeMillis();
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public GameEngine getEngine() {
        touch();
        return engine;
    }

    public File getSessionDir() {
        return sessionDir;
    }

    public synchronized void syncModel(String xml) {
        touch();
        if (xml == null || xml.indexOf("<block") < 0) return;
        try {
            List<Map<String, Object>> data = BlocklyXmlParser.parseBlocklyXml(xml);
            engine.rebuildProgram(data);
        } catch (Exception e) {
            System.err.println("[SessionContext " + sessionId + "] syncModel error: " + e.getMessage());
        }
    }

    public synchronized void syncMap(String mapJson) {
        touch();
        if (mapJson == null || mapJson.trim().isEmpty()) return;
        try {
            engine.setMapFromJson(mapJson);
            clearMomotState();
        } catch (Exception e) {
            System.err.println("[SessionContext " + sessionId + "] syncMap error: " + e.getMessage());
        }
    }

    public synchronized void syncLevelMeta(String metaJson) {
        touch();
        if (metaJson == null || metaJson.trim().isEmpty()) return;
        try {
            int oldLevelId = (engine.getCurrentLevel() != null) ? engine.getCurrentLevel().getId() : -1;
            engine.syncLevelMeta(metaJson);
            int newLevelId = (engine.getCurrentLevel() != null) ? engine.getCurrentLevel().getId() : -1;
            if (oldLevelId != newLevelId) {
                clearMomotState();
            }
        } catch (Exception e) {
            System.err.println("[SessionContext " + sessionId + "] syncLevelMeta error: " + e.getMessage());
        }
    }

    public synchronized void clearMomotState() {
        // Invalidate in-flight callbacks before interrupting so a late finish cannot
        // restore the previous level's output directory.
        searchGeneration.incrementAndGet();
        if (isMomotRunning) {
            stopMomotRun();
        }
        this.momotCurrentOutputDir = null;
        this.momotCurrentLevelId = -1;
        this.momotLogBuffer.clear();
        this.momotStatus = "Idle";
    }

    public synchronized void runMomotWithParams(int seed, int pop, int eval, int runs, int solLen) {
        touch();
        if (isMomotRunning) {
            momotLogBuffer.add("[Session] MoMoT run already in progress.");
            return;
        }

        final long runGen = searchGeneration.incrementAndGet();
        final int levelId = currentLevelId();

        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date()) + "_" + runGen;
        File runDir = new File(sessionDir, "run_" + ts);
        if (!runDir.exists() && !runDir.mkdirs()) {
            momotLogBuffer.add("[Error] Failed to create MoMoT run directory: " + runDir.getAbsolutePath());
            return;
        }
        // Keep the input beside the output folder. MomotRunService deletes the output
        // directory at the start of an isolated run, so the input must not live inside it.
        File inputXmi = new File(runDir, "input.xmi");
        File outDir = new File(runDir, "output");
        try {
            engine.saveToFile(inputXmi);
        } catch (Exception e) {
            System.err.println("[SessionContext " + sessionId + "] saveToFile failed: " + e.getMessage());
            momotLogBuffer.add("[Error] Failed to save input model for MoMoT run: " + e.getMessage());
            return;
        }
        if (!inputXmi.exists()) {
            momotLogBuffer.add("[Error] Failed to save input model for MoMoT run.");
            return;
        }

        this.momotCurrentOutputDir = outDir.getAbsolutePath();
        this.momotCurrentLevelId = levelId;

        MomotRunService.RunSpec spec = new MomotRunService.RunSpec(
            inputXmi.getAbsolutePath(),
            outDir.getAbsolutePath(),
            pop, eval, runs, solLen, false, seed, sessionId
        );

        isMomotRunning = true;
        momotStatus = "Waiting";
        momotLogBuffer.clear();
        momotLogBuffer.add("[MoMoT] Starting search for session " + sessionId + "...");

        this.momotThread = MomotRunService.runAsync(spec, log -> {
            if (runGen != searchGeneration.get()) return;
            momotLogBuffer.add(log);
        }, status -> {
            if (runGen != searchGeneration.get()) return;
            this.momotStatus = status;
        }, () -> {
            if (runGen != searchGeneration.get()) return;
            isMomotRunning = false;
            if (!"Stopped".equals(momotStatus)) {
                momotStatus = "Finished";
                momotLogBuffer.add("[MoMoT] Search completed.");
            }
            momotThread = null;
        }, dir -> {
            if (runGen != searchGeneration.get()) return;
            if (currentLevelId() != levelId) return;
            momotCurrentOutputDir = dir;
        });
    }

    public synchronized void stopMomotRun() {
        touch();
        if (isMomotRunning) {
            MomotRunService.stopMomotSearch(this.sessionId);
            Thread t = this.momotThread;
            if (t != null && t.isAlive()) {
                MomotRunService.stopRun(t);
            }
            this.momotThread = null;
            isMomotRunning = false;
            momotStatus = "Stopped";
            momotLogBuffer.add("[MoMoT] Search stopped by user.");
        }
    }

    public synchronized List<MomotResultsService.SolutionEntry> listMomotSolutions() {
        touch();
        if (momotCurrentOutputDir == null || momotCurrentLevelId != currentLevelId()) {
            return Collections.emptyList();
        }
        File outDir = new File(momotCurrentOutputDir);
        if (outDir.exists() && outDir.isDirectory()) {
            return MomotResultsService.loadFromOutputDir(outDir);
        }
        return Collections.emptyList();
    }

    private int currentLevelId() {
        Level level = engine.getCurrentLevel();
        return level != null ? level.getId() : -1;
    }

    public synchronized boolean loadMomotSolution(String modelPath) {
        touch();
        if (modelPath == null || modelPath.trim().isEmpty()) return false;
        try {
            File f = new File(modelPath.trim());
            if (!f.exists()) return false;
            engine.loadFromFile(f);
            return true;
        } catch (Exception e) {
            System.err.println("[SessionContext " + sessionId + "] loadMomotSolution failed: " + e.getMessage());
            return false;
        }
    }

    public boolean isMomotRunning() {
        return isMomotRunning;
    }

    public String getMomotStatus() {
        return momotStatus;
    }

    public List<String> getMomotLogs() {
        return new ArrayList<>(momotLogBuffer);
    }

    public String getMomotCurrentOutputDir() {
        return momotCurrentOutputDir;
    }
}
