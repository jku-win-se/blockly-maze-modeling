package blocky_game;

import javafx.application.Platform;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Robust MoMoT runner for Docker/Maven environments.
 */
public final class MomotRunService {

    private MomotRunService() {}

    public static class RunSpec {
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

    public static String runSync(RunSpec spec, Consumer<String> logLine, Consumer<String> onOutputDirReady) {
        return runInternal(spec, logLine, onOutputDirReady);
    }

    public static void stopCurrentRun() {
        Thread t = currentMomotThread;
        if (t != null && t.isAlive()) {
            t.interrupt();
        }
    }

    public static void runAsync(RunSpec spec, Consumer<String> logLine, Runnable onDone, Consumer<String> onOutputDirReady) {
        stopCurrentRun();
        Thread t = new Thread(() -> {
            try {
                runInternal(spec, logLine, onOutputDirReady);
            } catch (Throwable t2) {
                if (logLine != null) logLine.accept("[MoMoT] Failed:\n" + throwableToString(t2));
            } finally {
                currentMomotThread = null;
                if (onDone != null) Platform.runLater(onDone);
            }
        }, "MomotRunService");
        currentMomotThread = t;
        t.setDaemon(true);
        t.start();
    }

    private static void findJars(File dir, List<URL> urls) {
        if (!dir.exists() || !dir.isDirectory()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) findJars(f, urls);
            else if (f.getName().endsWith(".jar")) {
                try { urls.add(f.toURI().toURL()); } catch (Exception ignored) {}
            }
        }
    }

    private static String runInternal(RunSpec spec, Consumer<String> logLine, Consumer<String> onOutputDirReady) {
        if (logLine != null) logLine.accept("[MoMoT] Starting search logic...");

        try {
            File input = resolveExistingFile(spec.inputXmi);
            System.setProperty("blocky.input", input.getAbsolutePath());
        } catch (Exception e) {}

        deleteDirectoryRecursive(Path.of("output"));
        deleteDirectoryRecursive(Path.of("blocky_momot", "output"));

        Class<?> runnerClass = null;
        ClassLoader finalCl = MomotRunService.class.getClassLoader();
        
        try {
            runnerClass = Class.forName("blocky_momot_runner.blocky_custom", true, finalCl);
        } catch (Throwable e) {
            try {
                runnerClass = Class.forName("blocky_momot_runner.blocky", true, finalCl);
            } catch (Throwable e2) {
                try {
                    List<URL> urls = new ArrayList<>();
                    File[] targets = { 
                        new File("/app/blocky_momot/target/classes"), 
                        new File("/app/blocky_game/target/classes"),
                        new File("blocky_momot/target/classes"), 
                        new File("blocky_game/target/classes") 
                    };
                    for (File f : targets) if (f.exists()) urls.add(f.toURI().toURL());
                    findJars(new File("/app/blocky_game/target/all_deps"), urls);
                    findJars(new File("/app/libs"), urls);
                    findJars(new File("blocky_game/target/all_deps"), urls);
                    findJars(new File("libs"), urls);
                    URLClassLoader urlCl = new URLClassLoader(urls.toArray(new URL[0]), finalCl);
                    try { runnerClass = urlCl.loadClass("blocky_momot_runner.blocky_custom"); }
                    catch (Throwable e3) { runnerClass = urlCl.loadClass("blocky_momot_runner.blocky"); }
                    finalCl = urlCl;
                } catch (Throwable t) {
                    if (logLine != null) logLine.accept("[MoMoT] Loader Error: " + t.toString());
                    return null;
                }
            }
        }

        if (runnerClass == null) return null;

        registerPackages(finalCl, logLine);

        ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(finalCl);

        PrintStream oldOut = System.out, oldErr = System.err;
        MirroringOutputStream mirrorOut = new MirroringOutputStream(logLine, oldOut);
        MirroringOutputStream mirrorErr = new MirroringOutputStream(s -> { if (logLine != null) logLine.accept("[stderr] " + s); }, oldErr);
        
        System.setOut(new PrintStream(mirrorOut, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(mirrorErr, true, StandardCharsets.UTF_8));
        
        try {
            String absInput = resolveExistingFile(spec.inputXmi).getAbsolutePath();
            try {
                runnerClass.getMethod("initialization", String.class).invoke(null, absInput);
            } catch (Throwable e) {
                try { runnerClass.getMethod("initialization").invoke(null); } catch (Throwable ignored) {}
            }

            int solLen = 10;
            try {
                Class<?> m = Class.forName("blocky_momot.BlockyProgramMetrics", true, finalCl);
                Object v = m.getMethod("inferSolutionLength", String.class).invoke(null, absInput);
                if (v instanceof Number) solLen = Math.max(1, ((Number) v).intValue() * 2);
            } catch (Throwable ignored) {}

            Object inst = runnerClass.getDeclaredConstructor().newInstance();
            
            if (!Thread.interrupted()) {
                // Hardened method discovery
                java.lang.reflect.Method m = null;
                Class<?> curr = inst.getClass();
                while (curr != null && m == null) {
                    for (java.lang.reflect.Method candidate : curr.getDeclaredMethods()) {
                        if (candidate.getName().equals("performSearch") && candidate.getParameterCount() == 2) {
                            m = candidate;
                            break;
                        }
                    }
                    curr = curr.getSuperclass();
                }
                if (m != null) {
                    m.setAccessible(true);
                    m.invoke(inst, absInput, solLen);
                }
            }
        } catch (Throwable t) {
            if (logLine != null) logLine.accept("[MoMoT] Exec Failed: " + t.toString());
        } finally { 
            System.out.flush();
            System.err.flush();
            Thread.currentThread().setContextClassLoader(originalTCCL);
            System.setOut(oldOut); 
            System.setErr(oldErr); 
        }

        return finalizeOutput(spec, onOutputDirReady);
    }

    private static String finalizeOutput(RunSpec spec, Consumer<String> onOutputDirReady) {
        try {
            File currentDir = new File(System.getProperty("user.dir"));
            Path produced = new File(currentDir, "output").toPath();
            if (Files.exists(produced)) {
                Path target = Path.of(spec.outputBase).normalize();
                if (!target.isAbsolute()) target = new File(currentDir, spec.outputBase).toPath();
                Files.createDirectories(target.getParent());
                if (Files.exists(target)) deleteDirectoryRecursive(target);
                Files.move(produced, target, StandardCopyOption.REPLACE_EXISTING);
                if (onOutputDirReady != null) onOutputDirReady.accept(target.toString());
                return target.toString();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static Field findField(Class<?> clazz, String name) {
        Class<?> curr = clazz;
        while (curr != null) {
            try { return curr.getDeclaredField(name); }
            catch (NoSuchFieldException e) { curr = curr.getSuperclass(); }
        }
        return null;
    }

    private static void registerPackages(ClassLoader cl, Consumer<String> log) {
        try {
            Class<?> regClass = Class.forName("org.eclipse.emf.ecore.EPackage$Registry", true, cl);
            Class<?> ecorePkgClass = Class.forName("org.eclipse.emf.ecore.EcorePackage", true, cl);
            Class<?> blockyPkgClass = Class.forName("blocky.BlockyPackage", true, cl);
            Object registry = regClass.getField("INSTANCE").get(null);
            Object ecoreInst = ecorePkgClass.getField("eINSTANCE").get(null);
            String ecoreUri = (String) ecorePkgClass.getField("eNS_URI").get(null);
            Object blockyInst = blockyPkgClass.getField("eINSTANCE").get(null);
            String blockyUri = (String) blockyPkgClass.getField("eNS_URI").get(null);
            
            java.util.Map map = (java.util.Map) registry;
            map.put(ecoreUri, ecoreInst);
            map.put(blockyUri, blockyInst);
            map.put(blockyUri + "#", blockyInst);
            
            try {
                regClass.getMethod("put", String.class, Object.class).invoke(registry, blockyUri, blockyInst);
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }

    private static File resolveExistingFile(String path) {
        if (path == null || path.isBlank()) return new File("model/1.xmi");
        File f = new File(path);
        if (f.isAbsolute() || f.exists()) return f;
        File f1 = new File("..", path);
        if (f1.exists()) return f1;
        File f2 = new File("/app", path);
        if (f2.exists()) return f2;
        return f;
    }

    private static String firstExisting(String... paths) {
        for (String p : paths) if (p != null && new File(p).exists()) return p;
        return paths[0];
    }

    private static String firstWritableParent(String... dirPaths) {
        for (String p : dirPaths) {
            File d = new File(p);
            if ((d.exists() || d.mkdirs()) && d.isDirectory()) return p;
        }
        return dirPaths[0];
    }

    private static void deleteDirectoryRecursive(Path path) {
        if (!Files.exists(path)) return;
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted((p1, p2) -> p2.compareTo(p1)).map(Path::toFile).forEach(File::delete);
        } catch (Exception ignored) {}
    }

    private static String throwableToString(Throwable t) {
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }

    private static class MirroringOutputStream extends OutputStream {
        private static final ThreadLocal<Boolean> IN_CALLBACK = ThreadLocal.withInitial(() -> false);
        private final Consumer<String> logLine;
        private final PrintStream fallback;
        private final StringBuilder lineBuf = new StringBuilder();

        public MirroringOutputStream(Consumer<String> l, PrintStream f) {
            this.logLine = l;
            this.fallback = f;
        }

        @Override
        public void write(int b) throws java.io.IOException {
            if (fallback != null) fallback.write(b);
            if (b == '\n') {
                String s = lineBuf.toString();
                lineBuf.setLength(0);
                if (IN_CALLBACK.get()) return;
                IN_CALLBACK.set(true);
                try { if (logLine != null) logLine.accept(s); } finally { IN_CALLBACK.set(false); }
            } else if (b != '\r') {
                lineBuf.append((char) b);
            }
        }
        
        @Override public void write(byte[] b, int off, int len) throws java.io.IOException {
            if (fallback != null) fallback.write(b, off, len);
            for (int i = 0; i < len; i++) {
                byte curr = b[off + i];
                if (curr == '\n') {
                    String s = lineBuf.toString();
                    lineBuf.setLength(0);
                    if (!IN_CALLBACK.get()) {
                        IN_CALLBACK.set(true);
                        try { if (logLine != null) logLine.accept(s); } finally { IN_CALLBACK.set(false); }
                    }
                } else if (curr != '\r') {
                    lineBuf.append((char) curr);
                }
            }
        }
    }
}
