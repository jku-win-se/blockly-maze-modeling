package blocky_game;

import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

/**
 * Encapsulates all PNG/SVG snapshot functionality from the WebView (including
 * PNG base64 callback persistence and SVG embedding of referenced images).
 */
public class BlockySnapshotService {
    private final WebView webView;

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
        File xmi = new File("blocky_game/save.xmi");
        if (xmi.getParentFile() == null || !xmi.getParentFile().exists()) {
            xmi = new File("save.xmi");
        }

        File svgFile = svgSiblingOfXmi(xmi);
        System.out.println("[BlockySnapshotService] Attempting SVG snapshot to: " + svgFile.getAbsolutePath());

        try {
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
                System.err.println("[BlockySnapshotService] SVG snapshot failed: JS did not return a String (got "
                        + (result == null ? "null" : result.getClass().getName()) + ").");
                return;
            }

            String svg = ((String) result).trim();
            if (svg.isEmpty()) {
                System.err.println("[BlockySnapshotService] SVG snapshot failed: empty SVG string.");
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
     * Saves a high-resolution PNG preview by temporarily increasing WebView zoom and capturing
     * via AWT Robot from the computed WebView screen rectangle.
     */
    public void saveWebViewPngSnapshotByFx() {
        if (webView == null) {
            System.err.println("[BlockySnapshotService] WebView PNG snapshot skipped: webView is null.");
            return;
        }

        // Match GameEngine.saveModel() path logic exactly.
        File xmi = new File("blocky_game/save.xmi");
        if (xmi.getParentFile() == null || !xmi.getParentFile().exists()) {
            xmi = new File("save.xmi");
        }
        File pngFile = pngSiblingOfXmi(xmi);

        // PNG zoom is limited by the underlying raster tiles; to avoid "pixelated" screenshots
        // we capture at a higher WebView zoom (supersampling) and then restore zoom.
        final double zoomFactor = 20.0; // higher supersampling for crisper PNG preview
        final double originalZoom = webView.getZoom();
        webView.setZoom(originalZoom * zoomFactor);

        PauseTransition delay = new PauseTransition(Duration.millis(250));
        delay.setOnFinished(e -> {
            try {
                System.out.println("[BlockySnapshotService] Attempting WebView PNG snapshot to: " + pngFile.getAbsolutePath());
                if (pngFile.getParentFile() != null) pngFile.getParentFile().mkdirs();

                // Compute WebView screen rectangle robustly (Windows DPI-safe).
                Point2D topLeft = webView.localToScreen(0, 0);
                Point2D bottomRight = webView.localToScreen(webView.getWidth(), webView.getHeight());

                if (topLeft == null || bottomRight == null) {
                    System.err.println("[BlockySnapshotService] WebView PNG snapshot failed: localToScreen returned null points.");
                    return;
                }

                double tlX = Math.min(topLeft.getX(), bottomRight.getX());
                double tlY = Math.min(topLeft.getY(), bottomRight.getY());
                double brX = Math.max(topLeft.getX(), bottomRight.getX());
                double brY = Math.max(topLeft.getY(), bottomRight.getY());
                double wFx = Math.max(1, brX - tlX);
                double hFx = Math.max(1, brY - tlY);

                Screen targetScreen = Screen.getScreensForRectangle(tlX, tlY, wFx, hFx)
                        .stream()
                        .findFirst()
                        .orElse(Screen.getPrimary());

                double scaleX = targetScreen.getOutputScaleX();
                double scaleY = targetScreen.getOutputScaleY();

                Rectangle2D screenBoundsFx = targetScreen.getBounds();
                int screenMinXDev = (int) Math.round(screenBoundsFx.getMinX() * scaleX);
                int screenMinYDev = (int) Math.round(screenBoundsFx.getMinY() * scaleY);
                int screenWDev = (int) Math.round(screenBoundsFx.getWidth() * scaleX);
                int screenHDev = (int) Math.round(screenBoundsFx.getHeight() * scaleY);

                int xDev = (int) Math.round(tlX * scaleX);
                int yDev = (int) Math.round(tlY * scaleY);
                int wDev = (int) Math.round(wFx * scaleX);
                int hDev = (int) Math.round(hFx * scaleY);

                // If scaling was applied incorrectly, the capture rect will exceed monitor bounds.
                // In that case, fall back to scale=1 which matches Robot's coordinate system on this setup.
                if (wDev > screenWDev * 1.5 || hDev > screenHDev * 1.5) {
                    scaleX = 1.0;
                    scaleY = 1.0;
                    xDev = (int) Math.round(tlX * scaleX);
                    yDev = (int) Math.round(tlY * scaleY);
                    wDev = (int) Math.round(wFx * scaleX);
                    hDev = (int) Math.round(hFx * scaleY);
                }

                // Clamp to monitor bounds to avoid capturing the entire screen.
                int maxWDev = Math.max(1, screenWDev - (xDev - screenMinXDev));
                int maxHDev = Math.max(1, screenHDev - (yDev - screenMinYDev));
                xDev = Math.max(screenMinXDev, Math.min(xDev, screenMinXDev + screenWDev - 1));
                yDev = Math.max(screenMinYDev, Math.min(yDev, screenMinYDev + screenHDev - 1));
                wDev = Math.max(1, Math.min(wDev, maxWDev));
                hDev = Math.max(1, Math.min(hDev, maxHDev));

                System.out.println("[BlockySnapshotService] WebView screen rect (JavaFX): TL=("
                        + tlX + "," + tlY + "), BR=("
                        + brX + "," + brY + ")"
                        + ", screenScale=(" + scaleX + "," + scaleY + ")"
                        + ", captureRect=(" + xDev + "," + yDev + "," + wDev + "x" + hDev + ")");

                if (wDev < 50 || hDev < 50) {
                    System.err.println("[BlockySnapshotService] WebView PNG snapshot rect too small; refusing to capture.");
                    return;
                }

                Rectangle rect = new Rectangle(xDev, yDev, wDev, hDev);
                Robot robot = new Robot();
                BufferedImage capture = robot.createScreenCapture(rect);
                ImageIO.write(capture, "png", pngFile);

                System.out.println("[BlockySnapshotService] WebView PNG snapshot saved to: " + pngFile.getAbsolutePath());
            } catch (AWTException ex) {
                System.err.println("[BlockySnapshotService] WebView PNG snapshot failed (Robot): " + ex.getMessage());
                ex.printStackTrace();
            } catch (Exception ex) {
                System.err.println("[BlockySnapshotService] WebView PNG snapshot failed: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    webView.setZoom(originalZoom);
                } catch (Exception ignored) {
                }
            }
        });
        delay.play();
    }

    /**
     * Alternative PNG capture path using SVG->canvas->dataURL inside WebView JS and then
     * receiving the base64 dataUrl back in {@link #receivePngDataUrl(String)}.
     */
    public void saveWebViewPngSnapshot() {
        if (webView == null || webView.getEngine() == null) {
            System.err.println("[BlockySnapshotService] PNG snapshot skipped: WebView not ready.");
            return;
        }

        // Match GameEngine.saveModel() path logic exactly.
        File xmi = new File("blocky_game/save.xmi");
        if (xmi.getParentFile() == null || !xmi.getParentFile().exists()) {
            xmi = new File("save.xmi");
        }
        File pngFile = pngSiblingOfXmi(xmi);
        pendingPngFile = pngFile;
        System.out.println("[BlockySnapshotService] Attempting PNG snapshot to: " + pngFile.getAbsolutePath());

        try {
            webView.getEngine().executeScript(
                    "(function(){\n" +
                            "  try {\n" +
                            "    var svg = document.getElementById('svgMaze') || document.querySelector('svg#svgMaze');\n" +
                            "    var bridge = window.javaBridge || (window.parent && window.parent.javaBridge);\n" +
                            "    if (!svg) { if (bridge && bridge.receivePngDataUrl) bridge.receivePngDataUrl(null); return; }\n" +
                            "    var svgText = new XMLSerializer().serializeToString(svg);\n" +
                            "    if (!svgText.match(/xmlns=/)) svgText = svgText.replace(/^<svg/, '<svg xmlns=\"http://www.w3.org/2000/svg\"');\n" +
                            "    var blob = new Blob([svgText], {type: 'image/svg+xml;charset=utf-8'});\n" +
                            "    var url = URL.createObjectURL(blob);\n" +
                            "    var img = new Image();\n" +
                            "    img.onload = function(){\n" +
                            "      try {\n" +
                            "        var w = svg.clientWidth || parseFloat(svg.getAttribute('width')) || 400;\n" +
                            "        var h = svg.clientHeight || parseFloat(svg.getAttribute('height')) || 400;\n" +
                            "        var canvas = document.createElement('canvas');\n" +
                            "        canvas.width = w; canvas.height = h;\n" +
                            "        var ctx = canvas.getContext('2d');\n" +
                            "        ctx.clearRect(0,0,w,h);\n" +
                            "        ctx.drawImage(img, 0, 0, w, h);\n" +
                            "        var dataUrl = canvas.toDataURL('image/png');\n" +
                            "        if (bridge && bridge.receivePngDataUrl) bridge.receivePngDataUrl(dataUrl);\n" +
                            "      } catch(e) {\n" +
                            "        if (bridge && bridge.receivePngDataUrl) bridge.receivePngDataUrl(null);\n" +
                            "      } finally {\n" +
                            "        try { URL.revokeObjectURL(url); } catch(e2) {}\n" +
                            "      }\n" +
                            "    };\n" +
                            "    img.onerror = function(){ if (bridge && bridge.receivePngDataUrl) bridge.receivePngDataUrl(null); try { URL.revokeObjectURL(url); } catch(e2) {} };\n" +
                            "    img.src = url;\n" +
                            "  } catch(e) {\n" +
                            "    try { var bridge2 = window.javaBridge || (window.parent && window.parent.javaBridge); if (bridge2 && bridge2.receivePngDataUrl) bridge2.receivePngDataUrl(null); } catch(e2) {}\n" +
                            "  }\n" +
                            "})();"
            );
        } catch (Exception e) {
            System.err.println("[BlockySnapshotService] PNG snapshot JS execution failed: " + e.getMessage());
            pendingPngFile = null;
        }
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

