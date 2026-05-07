package blocky_game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * CLI batch runner to execute MoMoT on levels 1..10 with per-level tuning and
 * verify that each run produced at least one goal-reaching solution.
 *
 * Success condition per level (based on objectives.pf column order):
 * - GoalReached == -1.0 (maximization objective printed as negative)
 *
 * Current objective set (see blocky_custom): [GoalReached, Edits, ShortestPath]
 */
public final class MomotBatchRunner {
    private MomotBatchRunner() {}

    public static void main(String[] args) {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        boolean allOk = true;

        for (int level = 1; level <= 10; level++) {
            File alreadySolved = findAlreadySolvedOutput(level);
            if (alreadySolved != null) {
                System.out.println("=======================================================");
                System.out.println("[Batch] Level " + level + " SKIP (already solved): " + alreadySolved.getAbsolutePath());
                continue;
            }

            String input = resolveFirstExisting(
                    // new location (as you moved them)
                    "model/" + level + ".xmi",
                    // legacy locations (keep for compatibility)
                    "blocky_momot/model/input/" + level + ".xmi",
                    "../blocky_momot/model/input/" + level + ".xmi"
            );
            int maxBlocks = parseMaxBlocksOrDefault(new File(input), 5);

            int baseEvaluations = evaluationsForMaxBlocks(maxBlocks);
            int baseRuns = runsForMaxBlocks(maxBlocks);

            // Escalation strategy per attempt:
            // (maxEvaluations, nrRuns, populationSize, solutionLengthFactor)
            //
            // Default:
            // - attempt 0: base settings (factor=2)
            // - attempt 1: 2x evaluations (factor=2)
            // - attempt 2: 2x evaluations + 2x runs (factor=2)
            //
            // Hard levels (maxBlocks>=10):
            // - attempt 3: 4x evaluations + 2x runs, pop=200, factor=3
            // - attempt 4: 6x evaluations + 3x runs, pop=250, factor=4
            int[][] attempts = (maxBlocks >= 10)
                    ? new int[][] {
                        new int[] { baseEvaluations, baseRuns, 100, 2 },
                        new int[] { baseEvaluations * 2, baseRuns, 100, 2 },
                        new int[] { baseEvaluations * 2, baseRuns * 2, 100, 2 },
                        new int[] { baseEvaluations * 4, baseRuns * 2, 200, 3 },
                        new int[] { baseEvaluations * 6, baseRuns * 3, 250, 4 }
                    }
                    : new int[][] {
                        new int[] { baseEvaluations, baseRuns, 100, 2 },
                        new int[] { baseEvaluations * 2, baseRuns, 100, 2 },
                        new int[] { baseEvaluations * 2, baseRuns * 2, 100, 2 }
                    };

            boolean ok = false;
            String lastOutDir = null;
            for (int attempt = 0; attempt < attempts.length; attempt++) {
                int maxEvaluations = attempts[attempt][0];
                int nrRuns = attempts[attempt][1];
                int populationSize = attempts[attempt][2];
                int solutionLengthFactor = attempts[attempt][3];

                System.setProperty("blocky.populationSize", Integer.toString(populationSize));
                System.setProperty("blocky.maxEvaluations", Integer.toString(maxEvaluations));
                System.setProperty("blocky.nrRuns", Integer.toString(nrRuns));
                System.setProperty("blocky.solutionLengthFactor", Integer.toString(solutionLengthFactor));

                String outBase = "blocky_momot/output_levels_" + ts + "_lvl" + level + "_try" + attempt;

                System.out.println("=======================================================");
                System.out.println("[Batch] Level " + level + " input=" + new File(input).getAbsolutePath());
                System.out.println("[Batch] maxBlocks=" + maxBlocks
                        + " populationSize=" + populationSize
                        + " maxEvaluations=" + maxEvaluations
                        + " nrRuns=" + nrRuns
                        + " solutionLengthFactor=" + solutionLengthFactor
                        + " attempt=" + attempt);

                String outDir = MomotRunService.runSync(new MomotRunService.RunSpec(input, outBase), System.out::println, null);
                lastOutDir = outDir;
                if (outDir == null) {
                    System.out.println("[Batch] FAILED: no output directory produced.");
                    continue;
                }

                File objectives = new File(outDir, "objectives.pf");
                ok = hasGoalReachedSolution(objectives);
                System.out.println("[Batch] " + (ok ? "OK" : "FAILED") + " output=" + new File(outDir).getAbsolutePath());
                if (ok) break;
            }

            if (!ok) {
                allOk = false;
                if (lastOutDir != null) {
                    System.out.println("[Batch] Level " + level + " last output: " + new File(lastOutDir).getAbsolutePath());
                }
            }
        }

        System.out.println("=======================================================");
        System.out.println(allOk ? "[Batch] ALL LEVELS OK" : "[Batch] SOME LEVELS FAILED");
        if (!allOk) {
            System.exit(2);
        }
    }

    private static String resolveFirstExisting(String... candidates) {
        if (candidates == null || candidates.length == 0) return "model/1.xmi";
        for (String c : candidates) {
            if (c == null || c.isBlank()) continue;
            try {
                File f = new File(c);
                if (f.exists() && f.isFile()) return c;
            } catch (Exception ignored) {
            }
        }
        // return first even if missing (keeps error message deterministic)
        return candidates[0];
    }

    private static File findAlreadySolvedOutput(int level) {
        // We run from blocky_game, and outputs are written under blocky_game/blocky_momot/...
        File base = new File("blocky_momot");
        if (!base.exists() || !base.isDirectory()) return null;

        File[] outs = base.listFiles(f -> {
            if (f == null || !f.isDirectory()) return false;
            String n = f.getName();
            if (n == null) return false;
            // matches:
            // - output_levels_<ts>_lvlX
            // - output_levels_<ts>_lvlX_tryY
            return n.contains("_lvl" + level) && n.startsWith("output_levels_");
        });
        if (outs == null || outs.length == 0) return null;

        // Prefer newest folders first.
        java.util.Arrays.sort(outs, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        for (File outDir : outs) {
            File objectives = new File(outDir, "objectives.pf");
            if (hasGoalReachedSolution(objectives)) {
                return outDir;
            }
        }
        return null;
    }

    private static int evaluationsForMaxBlocks(int maxBlocks) {
        if (maxBlocks <= 2) return 10_000;
        if (maxBlocks <= 5) return 20_000;
        if (maxBlocks <= 7) return 30_000;
        return 50_000;
    }

    private static int runsForMaxBlocks(int maxBlocks) {
        return maxBlocks >= 7 ? 20 : 10;
    }

    private static int parseMaxBlocksOrDefault(File xmi, int fallback) {
        if (xmi == null || !xmi.exists() || !xmi.isFile()) return fallback;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(xmi), StandardCharsets.UTF_8))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                int idx = ln.indexOf("maxBlocks=\"");
                if (idx < 0) continue;
                int start = idx + "maxBlocks=\"".length();
                int end = ln.indexOf('"', start);
                if (end <= start) continue;
                String num = ln.substring(start, end).trim();
                int v = Integer.parseInt(num);
                return v > 0 ? v : fallback;
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private static boolean hasGoalReachedSolution(File objectivesPf) {
        if (objectivesPf == null || !objectivesPf.exists() || !objectivesPf.isFile()) return false;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(objectivesPf), StandardCharsets.UTF_8))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                ln = ln.trim();
                if (ln.isEmpty()) continue;
                String[] parts = ln.split("\\s+");
                if (parts.length < 1) continue;

                double goalReached = Double.parseDouble(parts[0]);
                boolean reached = goalReached <= -0.999999;
                if (reached) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}

