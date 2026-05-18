package blocky_game;

import javafx.application.Platform;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Best-effort MoMoT runner for the Blocky workspace.
 *
 * This is designed to work when MoMoT + blocky_momot are on the runtime classpath
 * (typically when running from Eclipse with the modeling target platform).
 */
public final class MomotRunService {
    private MomotRunService() {}

    private static final class LineForwardingOutputStream extends OutputStream {
        private final Consumer<String> onLine;
        private final ByteArrayOutputStream all;
        private final StringBuilder line = new StringBuilder();

        LineForwardingOutputStream(Consumer<String> onLine, ByteArrayOutputStream all) {
            this.onLine = onLine;
            this.all = all;
        }

        @Override
        public synchronized void write(int b) {
            all.write(b);
            char ch = (char) (b & 0xFF);
            if (ch == '\r') return;
            if (ch == '\n') {
                flushLine();
                return;
            }
            line.append(ch);
            // Prevent pathological long lines from blowing memory.
            if (line.length() > 4000) {
                flushLine();
            }
        }

        @Override
        public synchronized void flush() {
            flushLine();
        }

        private void flushLine() {
            if (onLine == null) return;
            if (line.length() == 0) return;
            String s = line.toString();
            line.setLength(0);
            try {
                onLine.accept(s);
            } catch (Exception ignored) {
            }
        }
    }

    private static void deleteDirectoryRecursive(Path dir) {
        if (dir == null) return;
        try {
            if (!Files.exists(dir)) return;
            if (!Files.isDirectory(dir)) return;
            try (Stream<Path> walk = Files.walk(dir)) {
                walk.sorted((a, b) -> b.getNameCount() - a.getNameCount()) // delete children first
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (Exception ignored) {
                        }
                    });
            }
        } catch (Exception ignored) {
        }
    }

    private static String throwableToString(Throwable t) {
        if (t == null) return "null";
        
        // Unwrap common reflection/proxy wrappers to find the meaningful cause
        Throwable root = t;
        while (root != null) {
            if (root.getMessage() != null && root.getMessage().contains("MoMoT search interrupted")) {
                return "MoMoT search stopped by user or level change.";
            }
            if (root.getCause() == null || root.getCause() == root) break;
            root = root.getCause();
        }
        
        root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(t.getClass().getName());
        if (t.getMessage() != null) sb.append(": ").append(t.getMessage());
        if (root != t) {
            sb.append("\nCaused by: ").append(root.getClass().getName());
            if (root.getMessage() != null) sb.append(": ").append(root.getMessage());
        }
        appendJdkModuleAccessHint(sb, root);
        sb.append("\n");
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        sb.append(sw);
        return sb.toString();
    }

    private static void appendJdkModuleAccessHint(StringBuilder sb, Throwable root) {
        if (sb == null || root == null) return;
        String cn = root.getClass().getName();
        String msg = root.getMessage();
        if (cn == null) cn = "";
        if (msg == null) msg = "";

        // JDK 16+ strongly encapsulates JDK internals; MOEAFramework instrumentation may require opens.
        if (cn.contains("InaccessibleObjectException")
                && msg.contains("java.util.AbstractList.modCount")
                && msg.contains("java.base")
                && msg.contains("opens java.util")) {
            sb.append("\n\n")
              .append("---- JVM module access fix ----\n")
              .append("Your JVM blocks reflective access needed by MOEAFramework instrumentation.\n")
              .append("Add this VM argument to your Eclipse run configuration:\n")
              .append("  --add-opens=java.base/java.util=ALL-UNNAMED\n")
              .append("If you still see similar errors, also try:\n")
              .append("  --add-opens=java.base/java.lang=ALL-UNNAMED\n");
        }
    }

    public static final class RunSpec {
        public final String inputXmi;
        public final String outputBase;

        public RunSpec(String inputXmi, String outputBase) {
            this.inputXmi = Objects.requireNonNull(inputXmi);
            this.outputBase = Objects.requireNonNull(outputBase);
        }
    }

    public static RunSpec defaultDirectManipulationSpec() {
        String input = firstExisting(
                "blocky_momot/model/input/direct_manipulation_request.xmi",
                "../blocky_momot/model/input/direct_manipulation_request.xmi",
                "direct_manipulation_request_momot.xmi",
                "blocky_game/direct_manipulation_request.xmi",
                "../blocky_game/direct_manipulation_request.xmi",
                "direct_manipulation_request.xmi"
        );

        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String out = firstWritableParent(
                "blocky_momot/output_dm_" + ts,
                "../blocky_momot/output_dm_" + ts,
                "output_dm_" + ts
        );

        return new RunSpec(input, out);
    }

    private static volatile Thread currentMomotThread;

    /**
     * Synchronous MoMoT run (no background thread).
     *
     * @return the final moved output directory, or {@code null} if unavailable
     */
    public static String runSync(RunSpec spec, Consumer<String> logLine, Consumer<String> onOutputDirReady) {
        return runInternal(spec, logLine, onOutputDirReady);
    }

    public static void stopCurrentRun() {
        Thread t = currentMomotThread;
        if (t != null && t.isAlive()) {
            System.out.println("[MomotRunService] Stopping current MoMoT run (interrupting thread: " + t.getName() + ")...");
            t.interrupt();
        }
    }

    public static void runAsync(RunSpec spec, Consumer<String> logLine, Runnable onDone) {
        runAsync(spec, logLine, onDone, null);
    }

    /**
     * Runs MoMoT and (best-effort) reports the final moved output directory path.
     * The output directory may differ from {@link RunSpec#outputBase} if a suffix is applied.
     */
    public static void runAsync(RunSpec spec, Consumer<String> logLine, Runnable onDone, Consumer<String> onOutputDirReady) {
        stopCurrentRun(); // Ensure only one run at a time
        Thread t = new Thread(() -> {
            try {
                runInternal(spec, logLine, onOutputDirReady);
            } catch (Throwable t2) {
                if (logLine != null) {
                    String details = throwableToString(t2);
                    logLine.accept("[MoMoT] Failed:\n" + details);
                }
            } finally {
                currentMomotThread = null;
                if (onDone != null) {
                    Platform.runLater(onDone);
                }
            }
        }, "MomotRunService");
        currentMomotThread = t;
        t.setDaemon(true);
        t.start();
    }

    private static String runInternal(RunSpec spec, Consumer<String> logLine, Consumer<String> onOutputDirReady) {
        if (logLine != null) logLine.accept("[MoMoT] Starting…");

        // NOTE:
        // `blocky_momot/src-gen/blocky.java` is generated, so we must NOT rely on custom edits there.
        // We instead set the generated runner's static `input` field via reflection and call its APIs.

        // The generated runner reads its default input model path from a system property.
        // Set it *before* loading the class so its static initializer uses the right value,
        // regardless of the current working directory (blocky_game vs blocky_momot).
        try {
            String absInput = resolveExistingFile(spec.inputXmi).getAbsoluteFile().getPath();
            System.setProperty("blocky.input", absInput);
            if (logLine != null) logLine.accept("[MoMoT] Set system property blocky.input = " + absInput);
        } catch (Exception e) {
            if (logLine != null) logLine.accept("[MoMoT] Warning: failed to set blocky.input: " + e.getMessage());
        }

        if (Thread.interrupted()) {
            if (logLine != null) logLine.accept("[MoMoT] Interrupted during initialization.");
            return null;
        }

        if (logLine != null) {
            logLine.accept("[MoMoT] Properties: blocky.populationSize=" + System.getProperty("blocky.populationSize")
                    + " blocky.maxEvaluations=" + System.getProperty("blocky.maxEvaluations")
                    + " blocky.nrRuns=" + System.getProperty("blocky.nrRuns")
                    + " blocky.solutionLength=" + System.getProperty("blocky.solutionLength")
                    + " blocky.solutionLengthFactor=" + System.getProperty("blocky.solutionLengthFactor")
                    + " blocky.seed=" + System.getProperty("blocky.seed")
                    + " blocky.henshin=" + System.getProperty("blocky.henshin"));
        }

        // Clean any previous outputs so each run starts fresh.
        // The runner may write to `output/` (cwd) or `blocky_momot/output/` depending on launch context.
        deleteDirectoryRecursive(Path.of("output"));
        deleteDirectoryRecursive(Path.of("blocky_momot", "output"));
        deleteDirectoryRecursive(Path.of("..", "output").normalize());
        deleteDirectoryRecursive(Path.of("..", "blocky_momot", "output").normalize());

        // Ensure the generated runner is available on the runtime classpath.
        // This typically requires running from Eclipse with the MoMoT target platform active (PDE).
                final Class<?> runnerClass;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) cl = MomotRunService.class.getClassLoader();
            // Initialize the runner after setting blocky.input.
            // Prefer the non-generated copy so regeneration won't break custom behavior.
            Class<?> c;
            try {
                c = Class.forName("blocky_custom", true, cl);
            } catch (ClassNotFoundException e) {
                try {
                    c = Class.forName("blocky", true, cl);
                } catch (ClassNotFoundException e2) {
                    throw e2;
                }
            }
            runnerClass = c;
        } catch (ClassNotFoundException cnf) {
            if (logLine != null) {
                logLine.accept("[MoMoT] Cannot start: runner class 'blocky_custom' or 'blocky' not found on classpath.");
                logLine.accept("[MoMoT] This usually happens when running blocky_game via Maven (no MoMoT/PDE bundles).");
                logLine.accept("[MoMoT] Run blocky_game from Eclipse with the modeling target platform active,");
                logLine.accept("[MoMoT] and ensure project 'blocky_momot' is built so 'blocky_custom' or 'blocky' exists in bin/");
            }
            return null;
        }

        // Capture stdout/stderr from the in-process runner so we can surface progress in the UI.
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream teeOut = new PrintStream(new LineForwardingOutputStream(logLine, buf), true, StandardCharsets.UTF_8);
        PrintStream teeErr = new PrintStream(new LineForwardingOutputStream((s) -> {
            if (logLine != null) logLine.accept("[stderr] " + s);
        }, buf), true, StandardCharsets.UTF_8);
        System.setOut(teeOut);
        System.setErr(teeErr);
        try {
                    // Call initialization() explicitly (register packages, baseline distance cache, etc.).
            try {
                        // blocky_custom.initialization(String absInputModelPath)
                        String absInput = resolveExistingFile(spec.inputXmi).getAbsoluteFile().getPath();
                        try {
                            runnerClass.getMethod("initialization", String.class).invoke(null, absInput);
                        } catch (NoSuchMethodException ns) {
                            // Fallback for older generated runner signature
                            runnerClass.getMethod("initialization").invoke(null);
                        }
            } catch (Exception ignored) {
            }

            // Compute solution length: use explicit property if set, else infer dynamically
            int solLen = 10;
            try {
                String explicitSolLen = System.getProperty("blocky.solutionLength");
                if (explicitSolLen != null && !explicitSolLen.isBlank()) {
                    solLen = Integer.parseInt(explicitSolLen.trim());
                } else {
                    Class<?> metrics = Class.forName("blocky_momot.BlockyProgramMetrics");
                    String absInput = resolveExistingFile(spec.inputXmi).getAbsoluteFile().getPath();
                    Object v = metrics.getMethod("inferSolutionLength", String.class).invoke(null, absInput);
                    int factor = 2;
                    try {
                        factor = Integer.parseInt(System.getProperty("blocky.solutionLengthFactor", "2"));
                    } catch (Exception ignored) {
                    }
                    if (factor < 1) factor = 1;
                    if (v instanceof Number) solLen = Math.max(1, ((Number) v).intValue() * factor);
                }
            } catch (Exception ignored) {
            }

                    Object inst = runnerClass.getDeclaredConstructor().newInstance();
                    applyExperimentOverrides(inst, logLine);
                    
                    if (Thread.interrupted()) {
                        if (logLine != null) logLine.accept("[MoMoT] Interrupted before starting search.");
                        return null;
                    }

                    String absInput = resolveExistingFile(spec.inputXmi).getAbsoluteFile().getPath();
                    runnerClass.getMethod("performSearch", String.class, int.class).invoke(inst, absInput, solLen);
        } catch (Throwable t) {
            if (t instanceof InterruptedException || (t.getCause() instanceof InterruptedException)) {
                if (logLine != null) logLine.accept("[MoMoT] Execution stopped (interrupted).");
            } else if (logLine != null) {
                logLine.accept("[MoMoT] Failed:\n" + throwableToString(t));
            }
            return null;
        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
        }

        // The generated runner typically writes to `output/` relative to the current working directory.
        // We move that folder into a unique `blocky_momot/output_*` directory so the UI can show multiple runs.
        String outputDir = null;
        try {
            Path produced = null;
            Path[] candidates = new Path[] {
                    Path.of("output"),
                    Path.of("blocky_momot", "output"),
                    Path.of("..", "output").normalize(),
                    Path.of("..", "blocky_momot", "output").normalize()
            };
            for (Path c : candidates) {
                try {
                    if (c != null && Files.exists(c) && Files.isDirectory(c)) {
                        produced = c;
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
            Path target = Path.of(spec.outputBase).normalize();
            if (produced != null && Files.exists(produced)) {
                if (Files.exists(target)) {
                    // avoid failures; keep existing target and pick a suffix
                    target = Path.of(spec.outputBase + "_2").normalize();
                }
                Files.createDirectories(target.getParent() != null ? target.getParent() : Path.of("."));
                Files.move(produced, target, StandardCopyOption.REPLACE_EXISTING);
                outputDir = target.toString();
                if (logLine != null) logLine.accept("[MoMoT] Moved outputs: " + produced + " -> " + target);
                try {
                    if (onOutputDirReady != null) onOutputDirReady.accept(outputDir);
                } catch (Exception ignored) {
                }
            } else {
                if (logLine != null) logLine.accept("[MoMoT] No output directory found to move (expected 'output/' or 'blocky_momot/output').");
            }
        } catch (Exception e) {
            if (logLine != null) logLine.accept("[MoMoT] Output move failed: " + e.getMessage());
        }

        if (logLine != null) {
            String out = buf.toString(StandardCharsets.UTF_8);
            if (out != null && !out.isEmpty()) {
                String tail = out.length() > 20000 ? out.substring(out.length() - 20000) : out;
                logLine.accept(tail);
            }
            logLine.accept("[MoMoT] Finished.");
        }
        return outputDir;
    }

    private static void applyExperimentOverrides(Object runnerInstance, Consumer<String> logLine) {
        // Overrides are now handled by the blocky_custom mediator class 
        // which extends 'blocky' and reads system properties directly.
        if (logLine != null && runnerInstance != null && runnerInstance.getClass().getSimpleName().equals("blocky_custom")) {
            logLine.accept("[MoMoT] Using blocky_custom mediator for dynamic parameter injection.");
        }
    }

    private static int parseIntOrDefault(String s, int def) {
        if (s == null) return def;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * Resolves an input XMI path robustly across different launch working directories.
     * If {@code path} is relative and does not exist, tries a few common parent prefixes.
     */
    private static File resolveExistingFile(String path) {
        if (path == null || path.isBlank()) return new File("model/1.xmi");
        File f = new File(path);
        if (f.isAbsolute()) return f;
        if (f.exists()) return f;
        // common when launching from blocky_game vs workspace root
        File f1 = new File("..", path);
        if (f1.exists()) return f1;
        File f2 = new File("..", ".." + File.separator + path);
        if (f2.exists()) return f2;
        return f;
    }

    private static String firstExisting(String... paths) {
        for (String p : paths) {
            if (p == null) continue;
            File f = new File(p);
            if (f.exists() && f.isFile()) return p;
        }
        // Return first as default (even if missing) to keep error messages consistent.
        return (paths != null && paths.length > 0) ? paths[0] : "blocky_momot/model/input/direct_manipulation_request.xmi";
    }

    private static String firstWritableParent(String... dirPaths) {
        for (String p : dirPaths) {
            try {
                File d = new File(p);
                if (!d.exists() && !d.mkdirs()) continue;
                if (d.exists() && d.isDirectory()) return p;
            } catch (Exception ignored) {
            }
        }
        return (dirPaths != null && dirPaths.length > 0) ? dirPaths[0] : "output_dm";
    }
}

