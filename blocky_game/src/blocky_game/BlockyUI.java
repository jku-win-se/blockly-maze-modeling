package blocky_game;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import blocky.Cell;
import blocky.Level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Web-based interface that synchronizes the Blockly workspace with the EMF
 * model.
 */
public class BlockyUI extends Application {

    private GameEngine engine;
    private WebView webView;
    @SuppressWarnings("FieldCanBeLocal")
    private JSBridge jsBridge;
    /** When true, after the next page load we apply currentLevel to the WebView (map, blocks, metadata). */
    private volatile boolean pendingApplyLevel;
    /** Suppress sync from JS to Java while we are injecting loaded state into the WebView. */
    private volatile boolean suppressSync;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("[BlockyUI] Application starting...");

        try {
            engine = new GameEngine();
            engine.initializeGame();

            webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            // Redirect JS console to Java System.out
            webEngine.setOnAlert(event -> System.out.println("[JS Alert] " + event.getData()));

            // Load the local maze.html with an explicit level so the WebView shows that level on load
            int initialLevel = 1;
            if (engine.getCurrentLevel() != null && engine.getCurrentLevel().getId() >= 1 && engine.getCurrentLevel().getId() <= 10) {
                initialLevel = engine.getCurrentLevel().getId();
            }
            webEngine.load(getMazeBaseUrl() + "?lang=en&level=" + initialLevel);

            // Setup the bridge
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    jsBridge = new JSBridge();
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaBridge", jsBridge);
                    injectSyncScript(webEngine);
                    // Maze's init runs in window "load" event, which fires after Worker.SUCCEEDED.
                    // Apply runs via JS polling until svgMaze + BlocklyInterface exist, then injects (no fixed delay).
                    if (pendingApplyLevel) {
                        try {
                            applyLevelToWebView(engine.getCurrentLevel(), webEngine);
                        } finally {
                            pendingApplyLevel = false;
                            suppressSync = false;
                        }
                    }
                    // Nav "Model" pill: delay so h1 exists
                    PauseTransition navDelay = new PauseTransition(Duration.millis(800));
                    navDelay.setOnFinished(e2 -> injectLevelNavModelElement(webEngine));
                    navDelay.play();
                }
            });

            BorderPane root = new BorderPane(webView);
            Scene scene = new Scene(root, 1200, 800);

            primaryStage.setTitle("Blockly Games : Maze (Web Sync to XMI)");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void injectSyncScript(WebEngine webEngine) {
        // Use Blockly's own XML serialization — stable against minification.
        // Blockly.Xml.workspaceToDom(ws) produces well-structured XML with
        // <block type>, <field name>, <statement name>, and <next> elements.
        String script = "(function() {\n" +
                "  var lastXml = '';\n" +
                "  function log(msg) {\n" +
                "    try {\n" +
                "      if (window.javaBridge) window.javaBridge.logJS(msg);\n" +
                "      else if (window.parent && window.parent.javaBridge) window.parent.javaBridge.logJS(msg);\n" +
                "    } catch(e) {}\n" +
                "  }\n" +
                "  window.onerror = function(m, u, l) { log('JS Error: ' + m + ' at ' + u + ':' + l); };\n" +
                "  function getWS() {\n" +
                "    try {\n" +
                "      if (window.BlocklyInterface && window.BlocklyInterface.getWorkspace) return window.BlocklyInterface.getWorkspace();\n"
                +
                "      if (window.Blockly && window.Blockly.getMainWorkspace) return window.Blockly.getMainWorkspace();\n"
                +
                "    } catch(e) {}\n" +
                "    return null;\n" +
                "  }\n" +
                "  function getXml(ws) {\n" +
                "    try {\n" +
                "      if (window.h && window.h.K && typeof window.h.K.qn === 'function' && typeof window.h.K.Mc === 'function') {\n"
                +
                "        return window.h.K.Mc(window.h.K.qn());\n" +
                "      }\n" +
                "      var dom = null;\n" +
                "      if (window.Blockly && window.Blockly.Xml && typeof window.Blockly.Xml.workspaceToDom === 'function') {\n"
                +
                "        dom = window.Blockly.Xml.workspaceToDom(ws);\n" +
                "      } else if (window.h && window.h.K && typeof window.h.K.lf === 'function') {\n" +
                "        dom = window.h.K.lf(ws);\n" +
                "      } else if (window.h && window.h.K && typeof window.h.K.workspaceToDom === 'function') {\n" +
                "        dom = window.h.K.workspaceToDom(ws);\n" +
                "      }\n" +
                "      if (!dom) { log('Blockly.Xml not found'); return null; }\n" +
                "      return new XMLSerializer().serializeToString(dom);\n" +
                "    } catch(e) { log('XML error: ' + e); return null; }\n" +
                "  }\n" +
                "  function sync(ws) {\n" +
                "    var xml = getXml(ws);\n" +
                "    if (xml && xml !== lastXml) {\n" +
                "      lastXml = xml;\n" +
                "      log('Syncing XML: ' + xml.substring(0, 120));\n" +
                "      var bridge = window.javaBridge || (window.parent && window.parent.javaBridge);\n" +
                "      if (bridge) bridge.syncModel(xml);\n" +
                "    }\n" +
                "  }\n" +
                "  var iters = 0;\n" +
                "  var interval = setInterval(function() {\n" +
                "    var ws = getWS();\n" +
                "    if (ws) {\n" +
                "      log('Workspace active. Attaching listeners.');\n" +
                "      clearInterval(interval);\n" +
                "      if (typeof ws.addChangeListener === 'function') ws.addChangeListener(function(e) { sync(ws); });\n"
                +
                "      else if (ws.zc && ws.zc.push) ws.zc.push(function(e) { sync(ws); });\n" +
                "      setInterval(function() { sync(ws); }, 1000);\n" +
                "      sync(ws);\n" +
                "      setTimeout(function() {\n" +
                "          var bridge = window.javaBridge || (window.parent && window.parent.javaBridge);\n" +
                "          if (!bridge) return;\n" +
                "          if (typeof window.X !== 'undefined') {\n" +
                "            bridge.syncMap(JSON.stringify(window.X));\n" +
                "          }\n" +
                "          try {\n" +
                "            var lvl  = (typeof window.K  !== 'undefined') ? window.K  : 1;\n" +
                "            var mxb  = (typeof window.Od !== 'undefined' && isFinite(window.Od)) ? window.Od : -1;\n" +
                "            var tb   = document.getElementById('toolbox');\n" +
                "            var tbHtml = tb ? tb.innerHTML : '';\n" +
                "            var hasLoops = tbHtml.indexOf('maze_forever') !== -1;\n" +
                "            var hasConds = tbHtml.indexOf('maze_if') !== -1;\n" +
                "            var meta = JSON.stringify({ level: lvl, maxBlocks: mxb, startDirection: 1,\n" +
                "                                        allowLoops: hasLoops, allowConditionals: hasConds });\n" +
                "            bridge.syncLevelMeta(meta);\n" +
                "          } catch(e) { log('syncLevelMeta error: ' + e); }\n" +
                "      }, 500);\n" +
                "\n" +

                "    } else if (iters % 5 == 0) {\n" +
                "      log('Waiting for workspace... (' + iters + 's)');\n" +
                "    }\n" +
                "    iters++;\n" +
                "  }, 1000);\n" +
                "  var runHooked = false;\n" +
                "  setInterval(function() {\n" +
                "    if (runHooked) return;\n" +
                "    var runBtn = document.getElementById('runButton');\n" +
                "    if (runBtn) {\n" +
                "      runHooked = true;\n" +
                "      log('Watching runButton via MutationObserver.');\n" +
                "      var observer = new MutationObserver(function(mutations) {\n" +
                "        for (var i = 0; i < mutations.length; i++) {\n" +
                "          if (mutations[i].attributeName === 'style' && runBtn.style.display === 'none') {\n" +
                "            log('Run button hidden — syncing state, saving XMI, then running simulation.');\n" +
                "            var bridge = window.javaBridge || (window.parent && window.parent.javaBridge);\n" +
                "            if (bridge) {\n" +
                "              var ws = getWS(); if (ws) sync(ws);\n" +
                "              if (typeof window.X !== 'undefined') bridge.syncMap(JSON.stringify(window.X));\n" +
                "              try {\n" +
                "                var lvl = (typeof window.K !== 'undefined') ? window.K : 1;\n" +
                "                var mxb = (typeof window.Od !== 'undefined' && isFinite(window.Od)) ? window.Od : -1;\n" +
                "                var tb = document.getElementById('toolbox'); var tbHtml = tb ? tb.innerHTML : '';\n" +
                "                var meta = JSON.stringify({ level: lvl, maxBlocks: mxb, startDirection: (typeof window.T !== 'undefined') ? window.T : 1, allowLoops: tbHtml.indexOf('maze_forever') !== -1, allowConditionals: tbHtml.indexOf('maze_if') !== -1 });\n" +
                "                bridge.syncLevelMeta(meta);\n" +
                "              } catch(e) { log('syncLevelMeta: ' + e); }\n" +
                "              bridge.runSimulation();\n" +
                "            }\n" +
                "            break;\n" +
                "          }\n" +
                "        }\n" +
                "      });\n" +
                "      observer.observe(runBtn, { attributes: true, attributeFilter: ['style'] });\n" +
                "    }\n" +
                "  }, 500);\n" +
                "})();\n";

        webEngine.executeScript(script);
    }

    public class JSBridge {
        public void logJS(String msg) {
            System.out.println("[WebView JS] " + msg);
        }

        /** Saves the current model to XMI. Called only when Run Program is clicked. */
        public void saveModelNow() {
            engine.saveModel();
        }

        public void runSimulation() {
            System.out.println("[JSBridge] 'Run' detected. Starting Java simulation...");
            engine.simulateUserProgram();
        }

        public void syncMap(String mapJson) {
            System.out.println("[JSBridge] Received map JSON from JS: " + mapJson);
            engine.setMapFromJson(mapJson);
        }

        public void syncLevelMeta(String metaJson) {
            System.out.println("[JSBridge] Received level metadata: " + metaJson);
            engine.syncLevelMeta(metaJson);
        }

        /** Called from WebView when the user clicks the "Model" pill; loads the single Model XMI from hardcoded path. */
        public void loadModel() {
            Platform.runLater(() -> loadModelImpl());
        }

        public void syncModel(String xml) {
            if (suppressSync) return;
            try {
                System.out.println("[JSBridge] Syncing workspace XML:\n" + xml);
                List<Map<String, Object>> data = parseBlocklyXml(xml);
                engine.rebuildProgram(data);
                // Auto-run simulation removed; it restricts to the runButton click now.
                // if (engine.getCurrentLevel().getSolution() != null) {
                // engine.simulateUserProgram();
                // }

                System.out.println("[JSBridge] Sync complete. Top-level blocks: " + data.size());
            } catch (Exception e) {
                System.err.println("[JSBridge] Sync Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // --- Blockly XML parser ---
    // Parses the XML produced by Blockly.Xml.workspaceToDom(workspace).
    // Format: <xml><block type="..."><field name="DIR">isPathLeft</field>
    // <statement name="DO"><block type="..."/></statement>
    // <next><block type="..."/></next></block></xml>

    private List<Map<String, Object>> parseBlocklyXml(String xml) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        if (xml == null || xml.trim().isEmpty())
            return result;

        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.Element root = doc.getDocumentElement();

        // Top-level <block> elements directly inside <xml>
        org.w3c.dom.NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node n = children.item(i);
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && "block".equalsIgnoreCase(n.getNodeName())) {
                Map<String, Object> block = parseBlockElement((org.w3c.dom.Element) n);
                if (block != null)
                    result.add(block);
            }
        }
        return result;
    }

    private Map<String, Object> parseBlockElement(org.w3c.dom.Element el) {
        if (el == null)
            return null;
        Map<String, Object> map = new HashMap<>();
        map.put("type", el.getAttribute("type"));
        map.put("id", el.getAttribute("id"));

        org.w3c.dom.NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node n = children.item(i);
            if (n.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE)
                continue;
            org.w3c.dom.Element child = (org.w3c.dom.Element) n;
            String tag = child.getNodeName().toLowerCase();

            System.out.println("[XMLParser]   block=" + el.getAttribute("type") + " child tag='" + tag + "'");
            switch (tag) {
                case "field":
                    String fieldName = child.getAttribute("name");
                    String fieldVal = child.getTextContent().trim();
                    System.out.println("[XMLParser]     field: name='" + fieldName + "' value='" + fieldVal + "'");
                    map.put(fieldName, fieldVal);
                    break;
                case "statement":
                    // e.g. <statement name="DO"><block .../></statement>
                    String stmtName = child.getAttribute("name");
                    org.w3c.dom.Element stmtBlock = firstBlockChild(child);
                    if (stmtBlock != null) {
                        String key = "DO".equals(stmtName) ? "body" : stmtName.equals("ELSE") ? "elseBranch" : stmtName;
                        map.put(key, parseBlockElement(stmtBlock));
                    }
                    break;
                case "next":
                    // <next><block .../></next> — chained blocks in same sequence
                    org.w3c.dom.Element nextBlock = firstBlockChild(child);
                    if (nextBlock != null) {
                        map.put("next", parseBlockElement(nextBlock));
                    }
                    break;
                default:
                    break;
            }
        }
        return map;
    }

    private org.w3c.dom.Element firstBlockChild(org.w3c.dom.Element parent) {
        org.w3c.dom.NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node n = children.item(i);
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && "block".equalsIgnoreCase(n.getNodeName())) {
                return (org.w3c.dom.Element) n;
            }
        }
        return null;
    }

    /** Loads the single Model XMI from hardcoded path and applies it to the WebView (levels 1–10 stay predefined in JS). */
    private void loadModelImpl() {
        File xmiFile = getModelXmiFile();
        if (!xmiFile.exists()) {
            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Model file not found: " + xmiFile.getPath()).showAndWait());
            return;
        }
        try {
            engine.loadFromFile(xmiFile);
        } catch (IOException e) {
            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Failed to load model: " + e.getMessage()).showAndWait());
            return;
        } catch (IllegalArgumentException e) {
            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Invalid model: " + e.getMessage()).showAndWait());
            return;
        }
        Level level = engine.getCurrentLevel();
        int levelId = level != null ? Math.max(1, Math.min(10, level.getId())) : 1;
        pendingApplyLevel = true;
        suppressSync = true;
        webView.getEngine().load(getMazeBaseUrl() + "?lang=en&level=" + levelId);
    }

    /** Single hardcoded XMI for "Model" (levels 1–10 are predefined in the WebView). */
    /** Path for Model load: load.xmi (blocky_game/ or current dir). */
    private static File getModelXmiFile() {
        File f = new File("blocky_game/load.xmi");
        if (f.exists()) return f;
        f = new File("load.xmi");
        if (f.exists()) return f;
        return new File("blocky_game/load.xmi");
    }

    private static String getMazeBaseUrl() {
        File mazeFile = new File("blocky_game/src/blocky_game/blockly-games-web/maze.html");
        if (!mazeFile.exists()) mazeFile = new File("src/blocky_game/blockly-games-web/maze.html");
        if (!mazeFile.exists()) mazeFile = new File("blockly-games-web/maze.html");
        String url = mazeFile.toURI().toString();
        if (url.contains("?")) url = url.substring(0, url.indexOf('?'));
        return url;
    }

    /** Injects a "Model" option in the level nav; only this one loads from XMI (levels 1–10 are predefined in the WebView). */
    private void injectLevelNavModelElement(WebEngine webEngine) {
        webEngine.executeScript(
            "(function() {" +
            "  var h1 = document.querySelector('body table tr td h1');" +
            "  if (!h1 || document.getElementById('levelModel')) return;" +
            "  h1.appendChild(document.createTextNode(' '));" +
            "  var span = document.createElement('span');" +
            "  span.className = 'level_number level_done';" +
            "  span.id = 'levelModel';" +
            "  span.textContent = 'Model';" +
            "  span.style.cursor = 'pointer';" +
            "  span.title = 'Load level from saved XMI model';" +
            "  span.addEventListener('click', function() {" +
            "    if (window.javaBridge && window.javaBridge.loadModel) window.javaBridge.loadModel();" +
            "  });" +
            "  h1.appendChild(span);" +
            "})();"
        );
    }

    /**
     * Injects the loaded level state into the WebView: map grid, nd/od, metadata (K, Od, T, Q, S),
     * Blockly workspace XML, and resets pegman. Call with suppressSync already set and clear it after.
     */
    private void applyLevelToWebView(Level level, WebEngine webEngine) {
        if (level == null || level.getMap() == null) return;
        suppressSync = true;
        try {
            int[][] grid = engine.buildGridForWebView(level.getMap());
            Cell startCell = engine.getStartCell(level.getMap());
            Cell goalCell = engine.getGoalCell(level.getMap());
            int levelId = Math.max(1, Math.min(10, level.getId()));
            int maxBlocks = level.getMaxBlocks() <= 0 ? -1 : level.getMaxBlocks();
            int t = engine.directionToT(level.getStartOrientation());

            // Build JSON array for window.X: X[row][col], row-major
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int row = 0; row < grid.length; row++) {
                sb.append("[");
                for (int col = 0; col < grid[row].length; col++) {
                    if (col > 0) sb.append(",");
                    sb.append(grid[row][col]);
                }
                sb.append("]");
                if (row < grid.length - 1) sb.append(",");
            }
            sb.append("]");
            String gridJson = sb.toString();

            // 1) Store level data in __inject* so the poller can apply it after maze init (which overwrites X, nd, od).
            webEngine.executeScript("window.__injectGridJson = " + gridJson + ";");
            if (grid.length > 0 && grid[0].length > 0) {
                int rd = grid[0].length;
                int qd = grid.length;
                webEngine.executeScript("window.__injectQd = " + qd + "; window.__injectRd = " + rd + ";");
                webEngine.executeScript("window.__injectSd = " + (50 * rd) + "; window.__injectTd = " + (50 * qd) + ";");
            }
            if (startCell != null) {
                webEngine.executeScript("window.__injectNd = {x: " + startCell.getX() + ", y: " + startCell.getY() + "};");
                webEngine.executeScript("window.__injectQ = " + startCell.getX() + "; window.__injectS = " + startCell.getY() + ";");
            }
            if (goalCell != null) {
                webEngine.executeScript("window.__injectOd = {x: " + goalCell.getX() + ", y: " + goalCell.getY() + "};");
            }
            webEngine.executeScript("window.__injectK = " + levelId + ";");
            webEngine.executeScript("window.__injectOdVal = " + (maxBlocks < 0 ? "Infinity" : String.valueOf(maxBlocks)) + ";");
            webEngine.executeScript("window.__injectT = " + t + ";");

            String xml = engine.solutionToBlocklyXml(level);
            String escapedForJson = xml.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
            webEngine.executeScript("window.__loadXml = \"" + escapedForJson + "\";");

            // 2) Poll until maze DOM and Blockly are ready, then apply our data (overwrite maze defaults), redraw, load blocks, reset pegman
            webEngine.executeScript(
                "(function(){ " +
                "var attempts = 0, maxAttempts = 60, interval = 100; " +
                "var id = setInterval(function() { " +
                "  if (!document.getElementById('svgMaze') || !window.BlocklyInterface) { " +
                "    attempts++; if (attempts >= maxAttempts) clearInterval(id); " +
                "    return; " +
                "  } " +
                "  clearInterval(id); " +
                "  if (window.__injectGridJson !== undefined) { " +
                "    window.X = window.__injectGridJson; " +
                "    if (window.__injectQd !== undefined) { window.Qd = window.__injectQd; window.Rd = window.__injectRd; window.Sd = window.__injectSd; window.Td = window.__injectTd; } " +
                "    if (window.__injectNd !== undefined) { window.nd = window.__injectNd; window.Q = window.__injectQ; window.S = window.__injectS; } " +
                "    if (window.__injectOd !== undefined) window.od = window.__injectOd; " +
                "    if (window.__injectK !== undefined) window.K = window.__injectK; " +
                "    if (window.__injectOdVal !== undefined) window.Od = window.__injectOdVal; " +
                "    if (window.__injectT !== undefined) window.T = window.__injectT; " +
                "  } " +
                "  var c = document.getElementById('svgMaze'); " +
                "  if (c) { while (c.firstChild) c.removeChild(c.firstChild); } " +
                "  if (typeof Wd === 'function') Wd(); " +
                "  if (!document.getElementById('look') && c) { " +
                "    var ns = 'http://www.w3.org/2000/svg'; " +
                "    var g = document.createElementNS(ns, 'g'); g.id = 'look'; " +
                "    var p1 = document.createElementNS(ns, 'path'); p1.setAttribute('d', 'M 0,-15 a 15 15 0 0 1 15 15'); " +
                "    var p2 = document.createElementNS(ns, 'path'); p2.setAttribute('d', 'M 0,-35 a 35 35 0 0 1 35 35'); " +
                "    var p3 = document.createElementNS(ns, 'path'); p3.setAttribute('d', 'M 0,-55 a 55 55 0 0 1 55 55'); " +
                "    g.appendChild(p1); g.appendChild(p2); g.appendChild(p3); c.appendChild(g); " +
                "  } " +
                "  if (window.BlocklyInterface.Kv && window.__loadXml !== undefined) { " +
                "    try { window.BlocklyInterface.Kv(window.__loadXml); delete window.__loadXml; } catch(e) { if (window.javaBridge) window.javaBridge.logJS('Kv: ' + e); } " +
                "  } " +
                "  if (typeof $d === 'function' && document.getElementById('finish')) { try { $d(false); } catch(e) { if (window.javaBridge) window.javaBridge.logJS('$d: ' + e); } } " +
                "  setTimeout(function() { try { var btn = document.getElementById('runButton'); if (btn) btn.click(); } catch(e) {} }, 300); " +
                "}, interval); " +
                "})();"
            );
        } finally {
            suppressSync = false;
        }
    }
}