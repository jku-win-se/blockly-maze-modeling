package blocky_game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Paper-style benchmark: for each maze level, find the minimum {@code solutionLength}
 * (allowed transformation count) under a fixed MOMoT configuration.
 *
 * <p>Default search budget (tuned for reliable convergence):
 * population size 150, 100 iterations (15,000 evaluations), ten independent runs with
 * seed {@code i} equal to the run index. A length is accepted when at least one run
 * reaches the goal. Override via {@code -Dblocky.populationSize}, {@code -Dblocky.iterations},
 * {@code -Dblocky.nrRuns}.
 *
 * <p>Run from repo root:
 * {@code mvn -pl blocky_game compile exec:java -Dexec.mainClass=blocky_game.MomotSimpleMinSolutionLengthRunner}
 */
public final class MomotSimpleMinSolutionLengthRunner {

    static final int PAPER_POPULATION_SIZE = 150;
    static final int PAPER_ITERATIONS = 100;
    static final int PAPER_MAX_EVALUATIONS = PAPER_POPULATION_SIZE * PAPER_ITERATIONS;
    static final int PAPER_NR_RUNS = 10;

    private static final int[] OPTIMAL_BLOCK_COUNT = { 2, 5, 2, 5, 5, 4, 4, 5, 4, 7 };

    private MomotSimpleMinSolutionLengthRunner() {}

    public static void main(String[] args) throws IOException {
        int fromLevel = parseIntProperty("blocky.fromLevel", 1);
        int toLevel = parseIntProperty("blocky.toLevel", 10);
        int popSize = parseIntProperty("blocky.populationSize", PAPER_POPULATION_SIZE);
        int iterations = parseIntProperty("blocky.iterations", PAPER_ITERATIONS);
        int maxEval = parseIntProperty("blocky.maxEvaluations", popSize * iterations);
        int nrRuns = parseIntProperty("blocky.nrRuns", PAPER_NR_RUNS);

        String sessionId = System.getProperty("blocky.benchmarkSession");
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_simple";
        }

        System.out.println("[SimpleMinSolutionLength] Session: " + sessionId);
        System.out.println("[SimpleMinSolutionLength] Levels: " + fromLevel + ".." + toLevel);
        System.out.println("[SimpleMinSolutionLength] populationSize=" + popSize
                + " iterations=" + iterations
                + " maxEvaluations=" + maxEval
                + " nrRuns=" + nrRuns);

        MomotRunService.warmupRunnerClass();

        List<LevelResult> results = new ArrayList<>();
        for (int level = fromLevel; level <= toLevel; level++) {
            LevelResult row = processLevel(level, sessionId, popSize, maxEval, nrRuns);
            if (row != null) {
                results.add(row);
            }
        }

        results.sort(Comparator.comparingInt(r -> r.level));
        writeOutputs(sessionId, results, popSize, iterations, maxEval, nrRuns);
    }

    private static LevelResult processLevel(int level, String sessionId, int popSize, int maxEval, int nrRuns) {
        System.out.println("=======================================================");
        System.out.println("[SimpleMinSolutionLength] Level " + level + " START");
        System.out.println("=======================================================");

        String input = resolveFirstExisting(
                "blocky_momot/model/input/" + level + ".xmi",
                "../blocky_momot/model/input/" + level + ".xmi",
                "model/" + level + ".xmi");

        File inputFile = new File(input);
        if (!inputFile.exists()) {
            System.err.println("[SimpleMinSolutionLength] FAILED: input not found for level " + level);
            return null;
        }

        String absInput = inputFile.getAbsolutePath();
        int maxBlocks = parseMaxBlocksOrDefault(inputFile, 5);
        int optimalBlocks = optimalBlockCountForLevel(level);
        int maxLenCap = Math.max(optimalBlocks + 25, maxBlocks * 4);

        Integer minLength = null;
        int lengthsTried = 0;
        int lastSuccesses = 0;

        for (int attemptLen = 1; attemptLen <= maxLenCap; attemptLen++) {
            lengthsTried++;
            System.out.println("[SimpleMinSolutionLength] Level " + level
                    + " trying solutionLength=" + attemptLen + " (" + nrRuns + " runs)...");

            int successes = runTrial(absInput, attemptLen, level, sessionId, lengthsTried, popSize, maxEval, nrRuns);
            lastSuccesses = successes;

            System.out.println("[SimpleMinSolutionLength] Level " + level + " length=" + attemptLen
                    + " successes=" + successes + "/" + nrRuns);

            if (successes > 0) {
                minLength = attemptLen;
                System.out.println("[SimpleMinSolutionLength] Level " + level
                        + " SOLVED at solutionLength=" + attemptLen);
                break;
            }
        }

        String status = minLength != null ? "SOLVED" : "UNSOLVED";
        Integer momotBlockCount = null;
        String winningOutputDir = null;
        if (minLength != null) {
            File outDir = MomotSimpleBenchmarkMetrics.resolveWinningOutputDir(
                    sessionId, level, minLength, lengthsTried);
            if (outDir != null) {
                winningOutputDir = outDir.getAbsolutePath();
                momotBlockCount = MomotSimpleBenchmarkMetrics.minBlockCountInSuccessfulModels(outDir);
            }
        }

        System.out.println("[SimpleMinSolutionLength] Level " + level + " FINISHED: status=" + status
                + " minSolutionLength=" + (minLength != null ? minLength : "")
                + " momotBlockCount=" + (momotBlockCount != null ? momotBlockCount : ""));

        return new LevelResult(level, absInput, optimalBlocks, minLength, lastSuccesses, nrRuns,
                lengthsTried, status, momotBlockCount, winningOutputDir);
    }

    private static int runTrial(String inputXmi, int solutionLength, int level, String sessionId,
            int attemptId, int popSize, int maxEval, int nrRuns) {
        String outBase = "blocky_momot/output_simple_" + sessionId + "_lvl" + level
                + "_len" + solutionLength + "_try" + attemptId;

        MomotRunService.RunSpec spec = new MomotRunService.RunSpec(
                inputXmi, outBase, popSize, maxEval, nrRuns, solutionLength);

        String outDir = MomotRunService.runSync(spec, line -> System.out.println("[L" + level + "] " + line), null);
        if (outDir == null) {
            return 0;
        }

        File dir = new File(outDir);
        File[] perRun = dir.listFiles((d, name) -> name.startsWith("objectives_seed_") && name.endsWith(".pf"));
        if (perRun != null && perRun.length > 0) {
            int successes = 0;
            for (File seedFile : perRun) {
                if (hasGoalReachedSolution(seedFile)) {
                    successes++;
                }
            }
            return successes;
        }

        return hasGoalReachedSolution(new File(outDir, "objectives.pf")) ? 1 : 0;
    }

    private static boolean hasGoalReachedSolution(File objectivesPf) {
        if (objectivesPf == null || !objectivesPf.exists() || !objectivesPf.isFile()) {
            return false;
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(objectivesPf), StandardCharsets.UTF_8))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                ln = ln.trim();
                if (ln.isEmpty()) {
                    continue;
                }
                String[] parts = ln.split("\\s+");
                if (parts.length < 1) {
                    continue;
                }
                double goalReached = Double.parseDouble(parts[0]);
                if (goalReached <= -0.999999) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static void writeOutputs(String sessionId, List<LevelResult> results,
            int popSize, int iterations, int maxEval, int nrRuns) throws IOException {
        File analysisDir = resolveAnalysisDir();
        File summaryCsv = new File(analysisDir, "min_solution_length_simple_" + sessionId + ".csv");

        try (FileWriter summary = new FileWriter(summaryCsv, StandardCharsets.UTF_8)) {
            summary.write("level,inputXmi,optimalBlockCount,minSolutionLength,momotBlockCount,successesAtMin,nrRuns,"
                    + "lengthsTried,status,populationSize,iterationsPerRun,maxEvaluations,winningOutputDir\n");
            for (LevelResult r : results) {
                summary.write(r.toCsvRow(popSize, iterations, maxEval) + "\n");
            }
        }

        System.out.println("[SimpleMinSolutionLength] Exported summary to: " + summaryCsv.getAbsolutePath());
    }

    private static File resolveAnalysisDir() {
        File[] candidates = {
            new File("../blocky_momot/analysis"),
            new File("blocky_momot/analysis"),
            new File("blocky_game/blocky_momot/analysis")
        };
        for (File dir : candidates) {
            if (dir.exists() || dir.mkdirs()) {
                return dir.getAbsoluteFile();
            }
        }
        return candidates[0].getAbsoluteFile();
    }

    private static int parseIntProperty(String key, int fallback) {
        try {
            String raw = System.getProperty(key);
            if (raw != null && !raw.isBlank()) {
                return Integer.parseInt(raw.trim());
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private static String resolveFirstExisting(String... candidates) {
        if (candidates == null || candidates.length == 0) {
            return "model/1.xmi";
        }
        for (String c : candidates) {
            if (c == null || c.isBlank()) {
                continue;
            }
            File f = new File(c);
            if (f.exists() && f.isFile()) {
                return c;
            }
        }
        return candidates[0];
    }

    private static int parseMaxBlocksOrDefault(File xmi, int fallback) {
        if (xmi == null || !xmi.exists() || !xmi.isFile()) {
            return fallback;
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(xmi), StandardCharsets.UTF_8))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                int idx = ln.indexOf("maxBlocks=\"");
                if (idx < 0) {
                    continue;
                }
                int start = idx + "maxBlocks=\"".length();
                int end = ln.indexOf('"', start);
                if (end <= start) {
                    continue;
                }
                int v = Integer.parseInt(ln.substring(start, end).trim());
                return v > 0 ? v : fallback;
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private static int optimalBlockCountForLevel(int level) {
        if (level >= 1 && level <= OPTIMAL_BLOCK_COUNT.length) {
            return OPTIMAL_BLOCK_COUNT[level - 1];
        }
        return 5;
    }

    static final class LevelResult {
        final int level;
        final String inputXmi;
        final int optimalBlockCount;
        final Integer minSolutionLength;
        final int successesAtMin;
        final int nrRuns;
        final int lengthsTried;
        final String status;
        final Integer momotBlockCount;
        final String winningOutputDir;

        LevelResult(int level, String inputXmi, int optimalBlockCount, Integer minSolutionLength,
                int successesAtMin, int nrRuns, int lengthsTried, String status,
                Integer momotBlockCount, String winningOutputDir) {
            this.level = level;
            this.inputXmi = inputXmi;
            this.optimalBlockCount = optimalBlockCount;
            this.minSolutionLength = minSolutionLength;
            this.successesAtMin = successesAtMin;
            this.nrRuns = nrRuns;
            this.lengthsTried = lengthsTried;
            this.status = status;
            this.momotBlockCount = momotBlockCount;
            this.winningOutputDir = winningOutputDir;
        }

        String toCsvRow(int popSize, int iterations, int maxEval) {
            String min = minSolutionLength != null ? String.valueOf(minSolutionLength) : "";
            String blocks = momotBlockCount != null ? String.valueOf(momotBlockCount) : "";
            return level + "," + escapeCsvField(inputXmi) + "," + optimalBlockCount + ","
                    + min + "," + blocks + "," + successesAtMin + "," + nrRuns + ","
                    + lengthsTried + "," + status + ","
                    + popSize + "," + iterations + "," + maxEval + ","
                    + escapeCsvField(winningOutputDir != null ? winningOutputDir : "");
        }

        private static String escapeCsvField(String field) {
            if (field == null) {
                return "";
            }
            if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                return "\"" + field.replace("\"", "\"\"") + "\"";
            }
            return field;
        }
    }
}
