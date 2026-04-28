package blocky_game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Best-effort loader for MoMoT outputs (objectives + textual summaries + model XMIs).
 *
 * Contract (current repo conventions):
 * - outputs live under blocky_momot/output* + "/" (e.g. output, output2)
 * - objectives in objectives.pf (whitespace-separated numbers per line)
 * - textual report in solutions.txt
 * - concrete solution models in output* + "/models/*.xmi"
 */
public final class MomotResultsService {

    public static final class SolutionEntry {
        public String outputDir;
        public String modelPath;
        public String objectiveLine; // e.g. "-1.0 15.0"
        public String summary;       // best-effort excerpt from solutions.txt
    }

    private MomotResultsService() {}

    public static List<SolutionEntry> loadAll() {
        File base = new File("blocky_momot");
        if (!base.exists() || !base.isDirectory()) {
            return Collections.emptyList();
        }

        File[] outs = base.listFiles(f -> f != null && f.isDirectory() && f.getName().startsWith("output"));
        if (outs == null || outs.length == 0) return Collections.emptyList();

        List<File> outputDirs = new ArrayList<>();
        Collections.addAll(outputDirs, outs);
        outputDirs.sort(Comparator.comparing(File::getName));

        List<SolutionEntry> all = new ArrayList<>();
        for (File outDir : outputDirs) {
            all.addAll(loadFromOutputDir(outDir));
        }
        return all;
    }

    public static List<SolutionEntry> loadFromOutputDir(File outDir) {
        if (outDir == null || !outDir.isDirectory()) return Collections.emptyList();

        List<String> objectiveLines = readLines(new File(outDir, "objectives.pf"));
        String solutionsTxt = readWhole(new File(outDir, "solutions.txt"));
        List<String> solutionSummaries = splitSolutionSummaries(solutionsTxt);

        // Join objectives -> summary by solution index (best-effort).
        Map<Integer, String> idxToObjective = new HashMap<>();
        for (int i = 0; i < objectiveLines.size(); i++) {
            String ln = objectiveLines.get(i).trim();
            if (!ln.isEmpty()) idxToObjective.put(i, ln);
        }
        Map<Integer, String> idxToSummary = new HashMap<>();
        for (int i = 0; i < solutionSummaries.size(); i++) {
            String s = solutionSummaries.get(i);
            if (s != null && !s.trim().isEmpty()) idxToSummary.put(i, s.trim());
        }

        // Model files.
        File modelsDir = new File(outDir, "models");
        File[] modelFiles = modelsDir.listFiles(f -> f != null && f.isFile() && f.getName().toLowerCase().endsWith(".xmi"));
        if (modelFiles == null) modelFiles = new File[0];

        // Try joining by filename convention: blocky_<obj0>_<obj1>.xmi (underscores instead of space).
        Map<String, Integer> objectiveToIndex = new HashMap<>();
        for (int i = 0; i < objectiveLines.size(); i++) {
            String ln = objectiveLines.get(i).trim();
            if (!ln.isEmpty()) objectiveToIndex.put(ln, i);
        }

        List<SolutionEntry> entries = new ArrayList<>();
        for (File mf : modelFiles) {
            SolutionEntry e = new SolutionEntry();
            e.outputDir = outDir.getPath();
            e.modelPath = mf.getPath();

            int idx = -1;
            String objGuess = objectiveFromModelFilename(mf.getName());
            if (objGuess != null && objectiveToIndex.containsKey(objGuess)) {
                idx = objectiveToIndex.get(objGuess);
            }

            if (idx >= 0) {
                e.objectiveLine = idxToObjective.get(idx);
                e.summary = idxToSummary.get(idx);
            } else {
                // Fall back: attach first objective/summary if only one exists.
                if (objectiveLines.size() == 1) e.objectiveLine = objectiveLines.get(0).trim();
                if (solutionSummaries.size() == 1) e.summary = solutionSummaries.get(0).trim();
            }

            entries.add(e);
        }

        // Stable order: objectiveLine then filename.
        entries.sort(Comparator
                .comparing((SolutionEntry s) -> s.objectiveLine == null ? "" : s.objectiveLine)
                .thenComparing(s -> s.modelPath == null ? "" : s.modelPath));

        return entries;
    }

    private static String objectiveFromModelFilename(String name) {
        if (name == null) return null;
        // Example: blocky_-1.0_15.0.xmi -> "-1.0 15.0"
        String n = name;
        if (n.toLowerCase().endsWith(".xmi")) n = n.substring(0, n.length() - 4);
        int idx = n.indexOf("blocky_");
        if (idx >= 0) n = n.substring(idx + "blocky_".length());
        String[] parts = n.split("_");
        if (parts.length < 2) return null;
        return parts[0] + " " + parts[1];
    }

    private static List<String> splitSolutionSummaries(String solutionsTxt) {
        if (solutionsTxt == null || solutionsTxt.trim().isEmpty()) return Collections.emptyList();

        // Very simple heuristic: split at "Solution i/j" headings and keep a small excerpt.
        String[] lines = solutionsTxt.split("\\r?\\n");
        List<String> out = new ArrayList<>();

        StringBuilder cur = null;
        boolean inSolution = false;
        for (String line : lines) {
            if (line != null && line.startsWith("Solution ")) {
                if (cur != null) out.add(cur.toString().trim());
                cur = new StringBuilder();
                inSolution = true;
                cur.append(line).append("\n");
                continue;
            }
            if (!inSolution) continue;
            if (cur == null) cur = new StringBuilder();

            // Keep the most informative parts only (avoid dumping huge files into the UI).
            if (line.startsWith("Number of objectives:")
                    || line.trim().startsWith("GoalReached:")
                    || line.trim().startsWith("SolutionLength:")
                    || line.startsWith("AggregatedFitness:")
                    || line.startsWith("Number of constraints:")
                    || line.startsWith("  AggregatedFitness:")
                    || line.startsWith("  GoalReached:")
                    || line.startsWith("  SolutionLength:")) {
                cur.append(line).append("\n");
            }
        }
        if (cur != null) out.add(cur.toString().trim());
        return out;
    }

    private static List<String> readLines(File file) {
        if (file == null || !file.exists() || !file.isFile()) return Collections.emptyList();
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String ln;
            while ((ln = br.readLine()) != null) lines.add(ln);
        } catch (Exception ignored) {
            // best-effort
        }
        return lines;
    }

    private static String readWhole(File file) {
        if (file == null || !file.exists() || !file.isFile()) return null;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                sb.append(ln).append("\n");
            }
        } catch (Exception ignored) {
            return null;
        }
        return sb.toString();
    }
}

