package blocky_game;

import javafx.animation.PauseTransition;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import netscape.javascript.JSObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Snapshots from the Blocky UI: maze SVG export and full-window PNG capture.
 */
public class BlockySnapshotService {
    private final WebView webView;

    /** Render scale for optional maze-only PNG export (viewBox pixels × this factor). */
    private static final double PNG_RENDER_SCALE = 4.0;

    /** Target file for the next WebView->PNG snapshot callback. */
    private volatile File pendingPngFile;

    public BlockySnapshotService(WebView webView) {
        this.webView = webView;
    }

    /** Called by WebView after the PNG snapshot callback returns a base64 dataUrl. */
    public void receivePngDataUrl(String dataUrl) {
        File target = pendingPngFile;
        pendingPngFile = null;
        if (target == null) {
            System.err.println("[BlockySnapshotService] PNG snapshot callback received, but pendingPngFile is null.");
            return;
        }
        if (dataUrl == null || !dataUrl.startsWith("data:image/png;base64,")) {
            System.err.println("[BlockySnapshotService] PNG snapshot failed (dataUrl missing/invalid): "
                    + (dataUrl == null ? "null" : dataUrl.substring(0, Math.min(80, dataUrl.length()))));
            return;
        }

        try {
            String base64 = dataUrl.substring("data:image/png;base64,".length());
            byte[] bytes = Base64.getDecoder().decode(base64);
            File parent = target.getParentFile();
            if (parent != null) parent.mkdirs();
            Files.write(target.toPath(), bytes);
            System.out.println("[BlockySnapshotService] PNG snapshot saved to: " + target.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[BlockySnapshotService] PNG snapshot write failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Saves an SVG snapshot from the WebView's svgMaze element (embedded images => data URIs). */
    public void saveWebViewSvgSnapshot() {
        if (webView == null || webView.getEngine() == null) {
            System.err.println("[BlockySnapshotService] SVG snapshot skipped: WebView not ready.");
            return;
        }

        // Match GameEngine.saveModel() path logic exactly.
        File xmi = resolveXmiFile();
        File svgFile = svgSiblingOfXmi(xmi);
        System.out.println("[BlockySnapshotService] Attempting SVG snapshot to: " + svgFile.getAbsolutePath());

        try {
            String svg = extractSvgFromWebView();
            if (svg == null || svg.isEmpty()) {
                return;
            }

            svg = embedSvgReferencedImages(svg);

            if (svgFile.getParentFile() != null) svgFile.getParentFile().mkdirs();
            Files.write(svgFile.toPath(), svg.getBytes(StandardCharsets.UTF_8));
            System.out.println("[BlockySnapshotService] SVG snapshot saved to: " + svgFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[BlockySnapshotService] SVG snapshot failed: " + e.getMessage());
        }
    }

    /**
     * Saves a true 7680×4320 PNG of the scene root via JavaFX snapshot (no screen capture).
     * Content is letterboxed/pillarboxed to preserve aspect ratio.
     */
    public JavaFxSnapshotExporter.ExportResult save8kPngSnapshot(javafx.scene.Scene scene) {
        return save8kPngSnapshot(scene, null, JavaFxSnapshotExporter.ScalingMode.PRESERVE_ASPECT_RATIO);
    }

    /**
     * Saves a true 7680×4320 PNG of the scene root.
     *
     * @param outputDirectory optional directory; defaults to the parent of the current XMI save file
     */
    public JavaFxSnapshotExporter.ExportResult save8kPngSnapshot(
            javafx.scene.Scene scene,
            Path outputDirectory,
            JavaFxSnapshotExporter.ScalingMode mode) {
        if (scene == null || scene.getRoot() == null) {
            String message = "8K PNG snapshot skipped: scene or root is null.";
            System.err.println("[BlockySnapshotService] " + message);
            return new JavaFxSnapshotExporter.ExportResult(false, null, 0, 0, 0L, message);
        }

        Path directory = outputDirectory != null ? outputDirectory : resolveDefaultExportDirectory();
        Path outputPath = JavaFxSnapshotExporter.defaultTimestampedPath(directory);
        System.out.println("[BlockySnapshotService] Attempting 8K PNG snapshot to: "
                + outputPath.toAbsolutePath());
        return JavaFxSnapshotExporter.exportScene(scene, outputPath, mode);
    }

    private Path resolveDefaultExportDirectory() {
        File xmiParent = resolveXmiFile().getParentFile();
        return xmiParent != null ? xmiParent.toPath() : Path.of(".");
    }

    /**
     * Saves a true 7680×4320 PNG after a short layout settle delay.
     */
    public void save8kPngSnapshot(Stage stage, Consumer<JavaFxSnapshotExporter.ExportResult> afterCapture) {
        Path outputPath = JavaFxSnapshotExporter.defaultTimestampedPath(resolveDefaultExportDirectory());
        save8kPngSnapshot(stage, outputPath, JavaFxSnapshotExporter.ScalingMode.PRESERVE_ASPECT_RATIO, afterCapture);
    }

    /**
     * Saves a true 7680×4320 PNG to {@code outputPath} after a short layout settle delay.
     */
    public void save8kPngSnapshot(
            Stage stage,
            Path outputPath,
            JavaFxSnapshotExporter.ScalingMode mode,
            Consumer<JavaFxSnapshotExporter.ExportResult> afterCapture) {
        if (stage == null || stage.getScene() == null || stage.getScene().getRoot() == null) {
            System.err.println("[BlockySnapshotService] 8K PNG snapshot skipped: stage not ready.");
            JavaFxSnapshotExporter.ExportResult failure = new JavaFxSnapshotExporter.ExportResult(
                    false, null, 0, 0, 0L, "Stage or scene not ready.");
            if (afterCapture != null) {
                afterCapture.accept(failure);
            }
            return;
        }
        if (outputPath == null) {
            JavaFxSnapshotExporter.ExportResult failure = new JavaFxSnapshotExporter.ExportResult(
                    false, null, 0, 0, 0L, "Output path is null.");
            if (afterCapture != null) {
                afterCapture.accept(failure);
            }
            return;
        }

        PauseTransition delay = new PauseTransition(Duration.millis(50));
        delay.setOnFinished(e -> {
            JavaFxSnapshotExporter.ExportResult result = new JavaFxSnapshotExporter.ExportResult(
                    false, outputPath, 0, 0, 0L, "Export did not run.");
            try {
                System.out.println("[BlockySnapshotService] Attempting 8K PNG snapshot to: "
                        + outputPath.toAbsolutePath());
                result = JavaFxSnapshotExporter.exportScene(stage.getScene(), outputPath, mode);
                if (!result.success()) {
                    System.err.println("[BlockySnapshotService] 8K PNG snapshot failed: " + result.message());
                }
            } catch (Exception ex) {
                System.err.println("[BlockySnapshotService] 8K PNG snapshot failed: " + ex.getMessage());
                ex.printStackTrace();
                result = new JavaFxSnapshotExporter.ExportResult(
                        false, outputPath, 0, 0, 0L, ex.getMessage());
            } finally {
                if (afterCapture != null) {
                    afterCapture.accept(result);
                }
            }
        });
        delay.play();
    }

    /**
     * Saves a high-DPI PNG of the entire JavaFX window without resizing the on-screen window.
     * Delegates to the true 8K JavaFX snapshot exporter (no OS screen capture).
     */
    public void saveFullWindowPngSnapshot(Stage stage, Runnable afterCapture) {
        save8kPngSnapshot(stage, result -> {
            if (afterCapture != null) {
                afterCapture.run();
            }
        });
    }

    /**
     * Maze-only PNG (full SVG viewBox). Prefer {@link #save8kPngSnapshot(Stage, Runnable)}
     * for UI screenshots.
     */
    public void saveWebViewPngSnapshot() {
        if (webView == null || webView.getEngine() == null) {
            System.err.println("[BlockySnapshotService] PNG snapshot skipped: WebView not ready.");
            return;
        }

        File pngFile = pngSiblingOfXmi(resolveXmiFile());
        pendingPngFile = pngFile;
        System.out.println("[BlockySnapshotService] Attempting full-maze PNG snapshot to: "
                + pngFile.getAbsolutePath());

        try {
            String svg = extractSvgFromWebView();
            if (svg == null || svg.isEmpty()) {
                pendingPngFile = null;
                return;
            }
            svg = embedSvgReferencedImages(svg);

            String b64 = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
            JSObject window = (JSObject) webView.getEngine().executeScript("window");
            window.setMember("_snapshotSvgB64", b64);

            webView.getEngine().executeScript(
                    "(function(scale){\n" +
                            "  try {\n" +
                            "    var svgText = atob(window._snapshotSvgB64 || '');\n" +
                            "    var bridge = window.javaBridge || (window.parent && window.parent.javaBridge);\n" +
                            "    if (!svgText) { if (bridge && bridge.receivePngDataUrl) bridge.receivePngDataUrl(null); return; }\n" +
                            "    var doc = new DOMParser().parseFromString(svgText, 'image/svg+xml');\n" +
                            "    var svgEl = doc.documentElement;\n" +
                            "    var vb = svgEl.viewBox && svgEl.viewBox.baseVal;\n" +
                            "    var w = (vb && vb.width > 0) ? vb.width : (parseFloat(svgEl.getAttribute('width')) || 400);\n" +
                            "    var h = (vb && vb.height > 0) ? vb.height : (parseFloat(svgEl.getAttribute('height')) || 400);\n" +
                            "    var blob = new Blob([svgText], {type: 'image/svg+xml;charset=utf-8'});\n" +
                            "    var url = URL.createObjectURL(blob);\n" +
                            "    var img = new Image();\n" +
                            "    img.onload = function(){\n" +
                            "      try {\n" +
                            "        var canvas = document.createElement('canvas');\n" +
                            "        canvas.width = Math.max(1, Math.round(w * scale));\n" +
                            "        canvas.height = Math.max(1, Math.round(h * scale));\n" +
                            "        var ctx = canvas.getContext('2d');\n" +
                            "        ctx.setTransform(scale, 0, 0, scale, 0, 0);\n" +
                            "        ctx.drawImage(img, 0, 0, w, h);\n" +
                            "        if (bridge && bridge.receivePngDataUrl) bridge.receivePngDataUrl(canvas.toDataURL('image/png'));\n" +
                            "      } catch(e) {\n" +
                            "        if (bridge && bridge.receivePngDataUrl) bridge.receivePngDataUrl(null);\n" +
                            "      } finally {\n" +
                            "        try { URL.revokeObjectURL(url); } catch(e2) {}\n" +
                            "      }\n" +
                            "    };\n" +
                            "    img.onerror = function(){\n" +
                            "      if (bridge && bridge.receivePngDataUrl) bridge.receivePngDataUrl(null);\n" +
                            "      try { URL.revokeObjectURL(url); } catch(e2) {}\n" +
                            "    };\n" +
                            "    img.src = url;\n" +
                            "  } catch(e) {\n" +
                            "    try {\n" +
                            "      var bridge2 = window.javaBridge || (window.parent && window.parent.javaBridge);\n" +
                            "      if (bridge2 && bridge2.receivePngDataUrl) bridge2.receivePngDataUrl(null);\n" +
                            "    } catch(e2) {}\n" +
                            "  }\n" +
                            "})(" + PNG_RENDER_SCALE + ");"
            );
        } catch (Exception e) {
            System.err.println("[BlockySnapshotService] PNG snapshot failed: " + e.getMessage());
            pendingPngFile = null;
        }
    }

    /**
     * Viewport-only WebView PNG capture (legacy). Prefer {@link #save8kPngSnapshot(Stage, Runnable)}.
     */
    public void saveWebViewPngSnapshotByFx() {
        if (webView == null) {
            System.err.println("[BlockySnapshotService] WebView PNG snapshot skipped: webView is null.");
            return;
        }

        System.err.println("[BlockySnapshotService] saveWebViewPngSnapshotByFx is deprecated; "
                + "use save8kPngSnapshot(Stage, Runnable) instead.");
    }

    private String extractSvgFromWebView() throws Exception {
        Object result = webView.getEngine().executeScript(
                "(function(){\n" +
                        "  try {\n" +
                        "    var s = document.getElementById('svgMaze') || document.querySelector('svg#svgMaze');\n" +
                        "    if (!s) return null;\n" +
                        "    var clone = s.cloneNode(true);\n" +
                        "    if (!clone.getAttribute('xmlns')) clone.setAttribute('xmlns', 'http://www.w3.org/2000/svg');\n" +
                        "    if (!clone.getAttribute('xmlns:xlink')) clone.setAttribute('xmlns:xlink', 'http://www.w3.org/1999/xlink');\n" +
                        "    return new XMLSerializer().serializeToString(clone);\n" +
                        "  } catch(e) { return null; }\n" +
                        "})()"
        );

        if (!(result instanceof String)) {
            System.err.println("[BlockySnapshotService] SVG extraction failed: JS did not return a String (got "
                    + (result == null ? "null" : result.getClass().getName()) + ").");
            return null;
        }

        String svg = ((String) result).trim();
        if (svg.isEmpty()) {
            System.err.println("[BlockySnapshotService] SVG extraction failed: empty SVG string.");
            return null;
        }
        return svg;
    }

    private static File resolveXmiFile() {
        File xmi = new File("blocky_game/save.xmi");
        if (xmi.getParentFile() == null || !xmi.getParentFile().exists()) {
            xmi = new File("save.xmi");
        }
        return xmi;
    }

    private String embedSvgReferencedImages(String svg) {
        File mazeHtml = resolveMazeHtmlFile();
        if (mazeHtml == null) return svg;

        File webRoot = mazeHtml.getParentFile(); // blockly-games-web/
        if (webRoot == null) return svg;

        Pattern p = Pattern.compile("(xlink:href|href)\\s*=\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(svg);

        java.util.Map<String, String> embedded = new java.util.HashMap<>();
        java.util.Set<String> missing = new java.util.HashSet<>();

        while (m.find()) {
            String href = m.group(2);
            if (href == null) continue;
            if (!href.startsWith("maze/")) continue;
            if (missing.contains(href)) continue;
            if (embedded.containsKey(href)) continue;

            File imgFile = new File(webRoot, href);
            if (!imgFile.exists()) {
                missing.add(href);
                continue;
            }
            try {
                byte[] bytes = Files.readAllBytes(imgFile.toPath());
                String b64 = Base64.getEncoder().encodeToString(bytes);
                String mime = guessMimeType(imgFile);
                embedded.put(href, "data:" + mime + ";base64," + b64);
            } catch (Exception e) {
                missing.add(href);
            }
        }

        if (embedded.isEmpty()) return svg;

        // Replace xlink:href and href occurrences with data URIs.
        for (java.util.Map.Entry<String, String> e : embedded.entrySet()) {
            String href = e.getKey();
            String dataUri = e.getValue();
            svg = svg.replace("xlink:href=\"" + href + "\"", "xlink:href=\"" + dataUri + "\"");
            svg = svg.replace("href=\"" + href + "\"", "href=\"" + dataUri + "\"");
        }
        return svg;
    }

    private static File resolveMazeHtmlFile() {
        File f = new File("blocky_game/src/blocky_game/blockly-games-web/maze.html");
        if (f.exists()) return f;
        f = new File("src/blocky_game/blockly-games-web/maze.html");
        if (f.exists()) return f;
        f = new File("blocky-games-web/maze.html");
        if (f.exists()) return f;
        return null;
    }

    private static String guessMimeType(File imgFile) {
        String name = imgFile.getName().toLowerCase();
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".gif")) return "image/gif";
        if (name.endsWith(".svg")) return "image/svg+xml";
        // Default.
        return "application/octet-stream";
    }

    private static File svgSiblingOfXmi(File xmiFile) {
        if (xmiFile == null) return new File("blocky_game/save.svg");
        String name = xmiFile.getName();
        String svgName = name.toLowerCase().endsWith(".xmi")
                ? name.substring(0, name.length() - 4) + ".svg"
                : name + ".svg";
        File parent = xmiFile.getParentFile();
        return parent != null ? new File(parent, svgName) : new File(svgName);
    }

    private static File pngSiblingOfXmi(File xmiFile) {
        if (xmiFile == null) return new File("blocky_game/save.png");
        String name = xmiFile.getName();
        String pngName = name.toLowerCase().endsWith(".xmi")
                ? name.substring(0, name.length() - 4) + ".png"
                : name + ".png";
        File parent = xmiFile.getParentFile();
        return parent != null ? new File(parent, pngName) : new File(pngName);
    }
}

