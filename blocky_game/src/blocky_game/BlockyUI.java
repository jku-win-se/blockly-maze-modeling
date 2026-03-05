package blocky_game;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.File;
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

            // Load the local maze.html
            File file = new File("blocky_game/src/blocky_game/blockly-games-web/maze.html");
            if (!file.exists()) {
                file = new File("src/blocky_game/blockly-games-web/maze.html");
            }
            if (!file.exists()) {
                file = new File("blockly-games-web/maze.html");
            }

            if (file.exists()) {
                webEngine.load(file.toURI().toString());
            }

            // Setup the bridge
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    jsBridge = new JSBridge();
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaBridge", jsBridge);
                    injectSyncScript(webEngine);
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
                "            log('Run button hidden — triggering Java simulation.');\n" +
                "            var bridge = window.javaBridge || (window.parent && window.parent.javaBridge);\n" +
                "            if (bridge) bridge.runSimulation();\n" +
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

        public void syncModel(String xml) {
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
}