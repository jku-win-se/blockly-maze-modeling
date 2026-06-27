package blocky_game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds {@code momotBlockCount} to an existing simple benchmark CSV by reading
 * the winning MOMoT output directories on disk.
 */
public final class MomotSimpleBenchmarkEnricher {

    private MomotSimpleBenchmarkEnricher() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: MomotSimpleBenchmarkEnricher <sessionId>");
            System.exit(1);
        }
        String sessionId = args[0];
        File csv = resolveCsv(sessionId);
        if (!csv.isFile()) {
            throw new IllegalStateException("CSV not found for session: " + sessionId);
        }

        List<Map<String, String>> rows = readCsv(csv);
        for (Map<String, String> row : rows) {
            if (!"SOLVED".equals(row.get("status"))) {
                continue;
            }
            int level = Integer.parseInt(row.get("level"));
            int minLen = Integer.parseInt(row.get("minSolutionLength"));
            int tried = Integer.parseInt(row.get("lengthsTried"));
            File outDir = MomotSimpleBenchmarkMetrics.resolveWinningOutputDir(sessionId, level, minLen, tried);
            if (outDir != null) {
                row.put("winningOutputDir", outDir.getAbsolutePath());
                Integer blocks = MomotSimpleBenchmarkMetrics.minBlockCountInSuccessfulModels(outDir);
                if (blocks != null) {
                    row.put("momotBlockCount", String.valueOf(blocks));
                }
            }
            System.out.println("[Enricher] Level " + level + " momotBlockCount="
                    + row.getOrDefault("momotBlockCount", "?"));
        }

        writeCsv(csv, rows);
        System.out.println("[Enricher] Updated " + csv.getAbsolutePath());
    }

    private static File resolveCsv(String sessionId) {
        File[] candidates = {
            new File("../blocky_momot/analysis/min_solution_length_simple_" + sessionId + ".csv"),
            new File("blocky_momot/analysis/min_solution_length_simple_" + sessionId + ".csv"),
            new File("../analysis/min_solution_length_simple_" + sessionId + ".csv")
        };
        for (File f : candidates) {
            if (f.isFile()) {
                return f.getAbsoluteFile();
            }
        }
        return candidates[0];
    }

    private static List<Map<String, String>> readCsv(File csv) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csv, StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                return rows;
            }
            String[] headers = headerLine.split(",", -1);
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1);
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i], values[i]);
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private static void writeCsv(File csv, List<Map<String, String>> rows) throws Exception {
        if (rows.isEmpty()) {
            return;
        }
        List<String> headers = List.of(
                "level", "inputXmi", "optimalBlockCount", "minSolutionLength", "momotBlockCount",
                "successesAtMin", "nrRuns", "lengthsTried", "status",
                "populationSize", "iterationsPerRun", "maxEvaluations", "winningOutputDir");

        try (FileWriter fw = new FileWriter(csv, StandardCharsets.UTF_8)) {
            fw.write(String.join(",", headers) + "\n");
            for (Map<String, String> row : rows) {
                List<String> values = new ArrayList<>();
                for (String h : headers) {
                    values.add(row.getOrDefault(h, ""));
                }
                fw.write(String.join(",", values) + "\n");
            }
        }
    }
}
