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
        Throwable root = t;
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
        sb.append("\n");
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        sb.append(sw);
        return sb.toString();
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

    public static void runAsync(RunSpec spec, Consumer<String> logLine, Runnable onDone) {
        runAsync(spec, logLine, onDone, null);
    }

    /**
     * Runs MoMoT and (best-effort) reports the final moved output directory path.
     * The output directory may differ from {@link RunSpec#outputBase} if a suffix is applied.
     */
    public static void runAsync(RunSpec spec, Consumer<String> logLine, Runnable onDone, Consumer<String> onOutputDirReady) {
        Thread t = new Thread(() -> {
            try {
                if (logLine != null) logLine.accept("[MoMoT] Starting…");

                // NOTE:
                // `blocky_momot/src-gen/blocky.java` is generated, so we must NOT rely on custom edits there.
                // We instead set the generated runner's static `input` field via reflection and call its APIs.

                // The generated runner's static initializer reads its default model file
                // "model/input/level5.xmi" relative to the current working directory.
                // Ensure this file exists to prevent ExceptionInInitializerError.
                try {
                    Path staged = Path.of("model", "input", "level5.xmi").normalize();
                    Files.createDirectories(staged.getParent());
                    Path src = Path.of(spec.inputXmi).toAbsolutePath().normalize();
                    if (Files.exists(src)) {
                        Files.copy(src, staged, StandardCopyOption.REPLACE_EXISTING);
                        if (logLine != null) logLine.accept("[MoMoT] Staged input for generated runner: " + staged.toString());
                    } else {
                        if (logLine != null) logLine.accept("[MoMoT] Warning: input XMI not found to stage: " + src);
                    }
                } catch (Exception e) {
                    if (logLine != null) logLine.accept("[MoMoT] Warning: failed to stage model/input/level5.xmi: " + e.getMessage());
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
                    // Allow initialization now that the expected default input exists.
                    runnerClass = Class.forName("blocky", true, cl);
                } catch (ClassNotFoundException cnf) {
                    if (logLine != null) {
                        logLine.accept("[MoMoT] Cannot start: generated runner class 'blocky' not found on classpath.");
                        logLine.accept("[MoMoT] This usually happens when running blocky_game via Maven (no MoMoT/PDE bundles).");
                        logLine.accept("[MoMoT] Run blocky_game from Eclipse with the modeling target platform active,");
                        logLine.accept("[MoMoT] and ensure project 'blocky_momot' is built so 'blocky' exists in bin/");
                    }
                    return;
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
                String phase = "init";
                try {
                    // Call initialization() explicitly (register packages, baseline distance cache, etc.).
                    phase = "initialization";
                    try {
                        runnerClass.getMethod("initialization").invoke(null);
                    } catch (Exception ignored) {
                    }

                    // Compute solution length dynamically: BlockyProgramMetrics.inferSolutionLength(input) * 2
                    phase = "infer_solution_length";
                    int solLen = 10;
                    try {
                        Class<?> metrics = Class.forName("blocky_momot.BlockyProgramMetrics");
                        String absInput = new File(spec.inputXmi).getAbsoluteFile().getPath();
                        Object v = metrics.getMethod("inferSolutionLength", String.class).invoke(null, absInput);
                        if (v instanceof Number) solLen = Math.max(1, ((Number) v).intValue() * 2);
                    } catch (Exception ignored) {
                    }

                    phase = "perform_search";
                    Object inst = runnerClass.getDeclaredConstructor().newInstance();
                    String absInput = new File(spec.inputXmi).getAbsoluteFile().getPath();
                    runnerClass.getMethod("performSearch", String.class, int.class).invoke(inst, absInput, solLen);
                } finally {
                    System.setOut(oldOut);
                    System.setErr(oldErr);
                }

                // The generated runner writes to `blocky_momot/output` (relative to its working directory).
                // The generated runner typically writes to `output/` relative to the current working directory.
                // We move that folder into a unique `blocky_momot/output_*` directory so the UI can show multiple runs.
                String movePhase = "move_outputs";
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
                        if (logLine != null) logLine.accept("[MoMoT] Moved outputs: " + produced + " -> " + target);
                        try {
                            if (onOutputDirReady != null) onOutputDirReady.accept(target.toString());
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
                    // Also emit the full captured output once (useful for copy/paste), but keep it bounded.
                    if (out != null && out.length() > 0) {
                        String tail = out.length() > 20000 ? out.substring(out.length() - 20000) : out;
                        logLine.accept(tail);
                    }
                    logLine.accept("[MoMoT] Finished.");
                }
            } catch (Throwable t2) {
                if (logLine != null) {
                    String details = throwableToString(t2);
                    logLine.accept("[MoMoT] Failed:\n" + details);
                }
            } finally {
                if (onDone != null) {
                    Platform.runLater(onDone);
                }
            }
        }, "MomotRunService");
        t.setDaemon(true);
        t.start();
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

