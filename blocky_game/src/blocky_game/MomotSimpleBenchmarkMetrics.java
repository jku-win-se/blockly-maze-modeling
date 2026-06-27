package blocky_game;

import java.io.File;

import blocky_momot.BlockyProgramMetrics;

/**
 * Derives Blockly block counts from MOMoT output directories produced by
 * {@link MomotSimpleMinSolutionLengthRunner}.
 */
public final class MomotSimpleBenchmarkMetrics {

    private MomotSimpleBenchmarkMetrics() {}

    /**
     * Minimum statement count among goal-reaching models saved under {@code outDir/models}.
     * Returns {@code null} when no successful model is found.
     */
    public static Integer minBlockCountInSuccessfulModels(File outDir) {
        if (outDir == null || !outDir.isDirectory()) {
            return null;
        }

        File modelsDir = new File(outDir, "models");
        File[] modelFiles = modelsDir.listFiles(
                f -> f != null && f.isFile() && f.getName().toLowerCase().endsWith(".xmi"));
        if (modelFiles == null || modelFiles.length == 0) {
            return null;
        }

        Integer min = null;
        for (File modelFile : modelFiles) {
            if (!isGoalReachedModelFilename(modelFile.getName())) {
                continue;
            }
            try {
                int blocks = BlockyProgramMetrics.countStatementsInXmi(modelFile.getAbsolutePath());
                min = min == null ? blocks : Math.min(min, blocks);
            } catch (Exception ignored) {
            }
        }
        return min;
    }

    static boolean isGoalReachedModelFilename(String name) {
        String objective = objectiveFromModelFilename(name);
        if (objective == null) {
            return false;
        }
        String[] parts = objective.trim().split("\\s+");
        if (parts.length == 0) {
            return false;
        }
        try {
            return Double.parseDouble(parts[0]) <= -0.999999;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static String objectiveFromModelFilename(String name) {
        if (name == null) {
            return null;
        }
        String n = name;
        if (n.toLowerCase().endsWith(".xmi")) {
            n = n.substring(0, n.length() - 4);
        }
        String[] parts = n.split("_");
        StringBuilder objective = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) {
                continue;
            }
            char first = p.charAt(0);
            if (!(Character.isDigit(first) || (first == '-' && p.length() > 1) || first == '.')) {
                continue;
            }
            if (objective.length() > 0) {
                objective.append(' ');
            }
            objective.append(p);
        }
        return objective.length() == 0 ? null : objective.toString();
    }

    public static File resolveWinningOutputDir(String sessionId, int level, int minSolutionLength, int lengthsTried) {
        String dirName = "output_simple_" + sessionId + "_lvl" + level
                + "_len" + minSolutionLength + "_try" + lengthsTried;
        File[] candidates = {
            new File("blocky_momot", dirName),
            new File("../blocky_momot", dirName),
            new File("blocky_game/blocky_momot", dirName)
        };
        for (File candidate : candidates) {
            if (candidate.isDirectory()) {
                return candidate.getAbsoluteFile();
            }
        }
        return null;
    }
}
