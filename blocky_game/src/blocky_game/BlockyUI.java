package blocky_game;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
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
    private BlockySnapshotService snapshotService;
    @SuppressWarnings("FieldCanBeLocal")
    private JSBridge jsBridge;
    /** When true, after the next page load we apply currentLevel to the WebView (map, blocks, metadata). */
    private volatile boolean pendingApplyLevel;
    /** Suppress sync from JS to Java while we are injecting loaded state into the WebView. */
    private volatile boolean suppressSync;
    /** True while loading/injecting a model, until JS confirms injection complete. */
    private volatile boolean awaitingInjectComplete;
    /** Monotonically increasing generation id for each successful page load. */
    private final java.util.concurrent.atomic.AtomicInteger pageGen = new java.util.concurrent.atomic.AtomicInteger(0);
    /** Current generation id for the currently loaded page. */
    private volatile int currentPageGen;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("[BlockyUI] Application starting...");

        try {
            engine = new GameEngine();
            engine.initializeGame();

            webView = new WebView();
            snapshotService = new BlockySnapshotService(webView);
            WebEngine webEngine = webView.getEngine();

            // Manual snapshot control overlay (top-right corner).
            Button snapshotButton = new Button("Snapshot");
            snapshotButton.setOnAction(e2 -> {
                if (awaitingInjectComplete) {
                    System.err.println("[BlockyUI] Snapshot skipped: WebView injection not complete yet.");
                    return;
                }
                // Snapshots are best-effort; failures are logged inside BlockySnapshotService.
                snapshotService.saveWebViewSvgSnapshot();
                snapshotService.saveWebViewPngSnapshotByFx();
            });
            StackPane.setAlignment(snapshotButton, Pos.BOTTOM_LEFT);
            StackPane.setMargin(snapshotButton, new Insets(10));

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
                    try {
                        // Generation guard for fast level switching: stale JS must not call into a new engine state.
                        currentPageGen = pageGen.incrementAndGet();
                        webEngine.executeScript("window.__javaPageGen = " + currentPageGen + ";");
                        injectSyncScript(webEngine);
                    } catch (Exception e) {
                        System.err.println("[BlockyUI] injectSyncScript failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                    // Debugger buttons must be available for both:
                    // - default WebView levels (no XMI loaded)
                    // - loaded XMI models (applyLevelToWebView)
                    try {
                        injectDebugControls(webEngine);
                    } catch (Exception e) {
                        System.err.println("[BlockyUI] injectDebugControls failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                    // Maze's init runs in window "load" event, which fires after Worker.SUCCEEDED.
                    // Apply runs via JS polling until svgMaze + BlocklyInterface exist, then injects (no fixed delay).
                    if (pendingApplyLevel) {
                        try {
                            applyLevelToWebView(engine.getCurrentLevel(), webEngine);
                        } catch (Exception e) {
                            System.err.println("[BlockyUI] applyLevelToWebView failed: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            pendingApplyLevel = false;
                            // Keep suppressSync until WebView confirms injection completed.
                        }
                    }
                    // Nav "Model" pill: delay so h1 exists
                    PauseTransition navDelay = new PauseTransition(Duration.millis(800));
                    navDelay.setOnFinished(e2 -> injectLevelNavModelElement(webEngine));
                    navDelay.play();
                }
            });

            StackPane root = new StackPane(webView, snapshotButton);
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
                "            var sd = (typeof window.__stableStartT === 'number') ? window.__stableStartT : ((typeof window.T !== 'undefined') ? window.T : 1);\n" +
                "            var meta = JSON.stringify({ level: lvl, maxBlocks: mxb, startDirection: sd,\n" +
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
                "            if (window.__dbgActive || window.__dbgSessionStarted) {\n" +
                "              log('Run observer ignored while debug session is active.');\n" +
                "              continue;\n" +
                "            }\n" +
                "            log('Run button hidden — syncing state, saving XMI, then running simulation.');\n" +
                "            var bridge = window.javaBridge || (window.parent && window.parent.javaBridge);\n" +
                "            if (bridge) {\n" +
                "              var ws = getWS(); if (ws) sync(ws);\n" +
                "              if (typeof window.X !== 'undefined') bridge.syncMap(JSON.stringify(window.X));\n" +
                "              try {\n" +
                "                var lvl = (typeof window.K !== 'undefined') ? window.K : 1;\n" +
                "                var mxb = (typeof window.Od !== 'undefined' && isFinite(window.Od)) ? window.Od : -1;\n" +
                "                var tb = document.getElementById('toolbox'); var tbHtml = tb ? tb.innerHTML : '';\n" +
                "                var sd = (typeof window.__stableStartT === 'number') ? window.__stableStartT : ((typeof window.T !== 'undefined') ? window.T : 1);\n" +
                "                var meta = JSON.stringify({ level: lvl, maxBlocks: mxb, startDirection: sd, allowLoops: tbHtml.indexOf('maze_forever') !== -1, allowConditionals: tbHtml.indexOf('maze_if') !== -1 });\n" +
                "                bridge.syncLevelMeta(meta);\n" +
                "              } catch(e) { log('syncLevelMeta: ' + e); }\n" +
                "              try { if (bridge.saveModelNow) bridge.saveModelNow(); } catch(e) { log('saveModelNow: ' + e); }\n" +
                "              try {\n" +
                "                var gen = (typeof window.__javaPageGen === 'number') ? window.__javaPageGen : 0;\n" +
                "                if (bridge.runSimulationWithGen) bridge.runSimulationWithGen(gen);\n" +
                "                else bridge.runSimulation();\n" +
                "              } catch(e) { bridge.runSimulation(); }\n" +
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

    /**
     * Inject debugger UI + JS-side glue for Pause/Resume/Stop/Step.
     * This runs for every Maze page load (default levels included), not only for XMI loads.
     */
    private void injectDebugControls(WebEngine webEngine) {
        try {
            webEngine.executeScript(
                "(function(){ " +
                "  try { "
                + "    if (window.__dbgButtonsBound) return; "
                + "  } catch(e) {} "
                + "  window.__dbgDisableAutoRun = true; "
                + "  function __execLogEnsure() { "
                + "    try { "
                + "      if (window.__execLogReady) return true; "
                + "      var host = document.getElementById('blockly'); "
                + "      if (!host) return false; "
                + "      var existing = document.getElementById('__execLogPanel'); "
                + "      if (!existing) { "
                + "        var panel = document.createElement('div'); panel.id = '__execLogPanel'; "
                + "        panel.style.position = 'absolute'; "
                + "        panel.style.left = '10px'; "
                + "        panel.style.bottom = '70px'; "
                + "        panel.style.top = 'auto'; "
                + "        panel.style.width = '380px'; "
                + "        panel.style.height = '220px'; "
                + "        panel.style.minWidth = '260px'; "
                + "        panel.style.minHeight = '120px'; "
                + "        panel.style.maxWidth = '560px'; "
                + "        panel.style.maxHeight = '520px'; "
                + "        panel.style.overflow = 'hidden'; "
                + "        panel.style.background = 'rgba(64,64,64,0.85)'; "
                + "        panel.style.borderRadius = '8px'; "
                + "        panel.style.border = '1px solid rgba(255,255,255,0.15)'; "
                + "        panel.style.boxShadow = '2px 2px 5px rgba(0,0,0,0.35)'; "
                + "        panel.style.zIndex = '999'; "
                + "        var header = document.createElement('div'); header.id = '__execLogHeader'; "
                + "        header.style.display = 'flex'; header.style.alignItems = 'center'; header.style.justifyContent = 'space-between'; "
                + "        header.style.padding = '6px 8px'; header.style.color = '#fff'; header.style.fontSize = '14px'; "
                + "        header.style.cursor = 'move'; "
                + "        header.style.userSelect = 'none'; "
                + "        var title = document.createElement('div'); title.textContent = 'Execution log'; title.style.fontWeight = 'bold'; "
                + "        var btn = document.createElement('button'); btn.id = '__execLogClearBtn'; btn.textContent = 'Clear'; "
                + "        btn.style.margin = '0'; btn.style.padding = '4px 8px'; btn.style.fontSize = '12px'; "
                + "        btn.style.borderRadius = '4px'; btn.style.border = '1px solid rgba(255,255,255,0.25)'; "
                + "        btn.style.background = 'rgba(255,255,255,0.10)'; btn.style.color = '#fff'; "
                + "        btn.addEventListener('click', function(){ try { if (window.__execLogClear) window.__execLogClear(); } catch(e) {} }); "
                + "        header.appendChild(title); header.appendChild(btn); "
                + "        var body = document.createElement('pre'); body.id = '__execLogBody'; "
                + "        body.style.margin = '0'; body.style.padding = '6px 8px'; "
                + "        body.style.height = 'calc(100% - 38px)'; body.style.overflow = 'auto'; "
                + "        body.style.color = '#fff'; body.style.fontFamily = 'Consolas, Menlo, Monaco, monospace'; body.style.fontSize = '12px'; "
                + "        body.style.whiteSpace = 'pre-wrap'; body.style.wordBreak = 'break-word'; "
                + "        panel.appendChild(header); panel.appendChild(body); "
                + "        var rh = document.createElement('div'); rh.id = '__execLogResizeHandle'; "
                + "        rh.style.position = 'absolute'; rh.style.right = '2px'; rh.style.bottom = '2px'; "
                + "        rh.style.width = '14px'; rh.style.height = '14px'; "
                + "        rh.style.cursor = 'se-resize'; "
                + "        rh.style.opacity = '0.85'; "
                + "        rh.style.background = 'linear-gradient(135deg, rgba(255,255,255,0.0) 0%, rgba(255,255,255,0.0) 45%, rgba(255,255,255,0.35) 46%, rgba(255,255,255,0.35) 55%, rgba(255,255,255,0.0) 56%, rgba(255,255,255,0.0) 100%)'; "
                + "        panel.appendChild(rh); "
                + "        host.appendChild(panel); "
                + "      } "
                + "      window.__execLogReady = true; "
                + "      if (!window.__execLogMaxLines) window.__execLogMaxLines = 500; "
                + "      if (!window.__execLogDragBound) { "
                + "        window.__execLogDragBound = true; "
                + "        (function(){ "
                + "          try { "
                + "            var panel = document.getElementById('__execLogPanel'); "
                + "            var header = document.getElementById('__execLogHeader'); "
                  + "            var handle = document.getElementById('__execLogResizeHandle'); "
                + "            if (!panel || !header) return; "
                + "            var dragging = false; "
                  + "            var resizing = false; "
                + "            var startX = 0, startY = 0, startLeft = 0, startTop = 0; "
                  + "            var startW = 0, startH = 0; "
                + "            function clamp(v, min, max) { return Math.max(min, Math.min(max, v)); } "
                + "            function onMove(ev) { "
                + "              try { "
                  + "                var host = document.getElementById('blockly'); "
                  + "                if (!host) return; "
                  + "                var hb = host.getBoundingClientRect(); "
                  + "                if (resizing) { "
                  + "                  var dx = ev.clientX - startX; "
                  + "                  var dy = ev.clientY - startY; "
                  + "                  var newW = startW + dx; "
                  + "                  var newH = startH + dy; "
                  + "                  var minW = 260, minH = 120, maxW = 560, maxH = 520; "
                  + "                  newW = clamp(newW, minW, maxW); "
                  + "                  newH = clamp(newH, minH, maxH); "
                  + "                  // Clamp so we stay within host bounds.\n"
                  + "                  var pb = panel.getBoundingClientRect(); "
                  + "                  var leftInHost = pb.left - hb.left; "
                  + "                  var topInHost = pb.top - hb.top; "
                  + "                  newW = clamp(newW, minW, Math.max(minW, hb.width - leftInHost)); "
                  + "                  newH = clamp(newH, minH, Math.max(minH, hb.height - topInHost)); "
                  + "                  panel.style.width = Math.round(newW) + 'px'; "
                  + "                  panel.style.height = Math.round(newH) + 'px'; "
                  + "                  return; "
                  + "                } "
                  + "                if (dragging) { "
                  + "                  var dx2 = ev.clientX - startX; "
                  + "                  var dy2 = ev.clientY - startY; "
                  + "                  var pb2 = panel.getBoundingClientRect(); "
                  + "                  var newLeft = startLeft + dx2; "
                  + "                  var newTop  = startTop + dy2; "
                  + "                  var maxLeft = Math.max(0, hb.width  - pb2.width); "
                  + "                  var maxTop  = Math.max(0, hb.height - pb2.height); "
                  + "                  newLeft = clamp(newLeft, 0, maxLeft); "
                  + "                  newTop  = clamp(newTop,  0, maxTop); "
                  + "                  panel.style.left = newLeft + 'px'; "
                  + "                  panel.style.top = newTop + 'px'; "
                  + "                  panel.style.bottom = 'auto'; "
                  + "                } "
                + "              } catch(e) {} "
                + "            } "
                + "            function onUp() { "
                + "              dragging = false; "
                  + "              resizing = false; "
                + "              try { document.removeEventListener('mousemove', onMove, true); document.removeEventListener('mouseup', onUp, true); } catch(e) {} "
                + "            } "
                + "            header.addEventListener('mousedown', function(ev){ "
                + "              try { "
                + "                if (ev && ev.button !== 0) return; "
                + "                if (ev && ev.target && ev.target.id === '__execLogClearBtn') return; "
                + "                var host = document.getElementById('blockly'); "
                + "                if (!host) return; "
                + "                var hb = host.getBoundingClientRect(); "
                + "                var pb = panel.getBoundingClientRect(); "
                + "                dragging = true; "
                + "                startX = ev.clientX; startY = ev.clientY; "
                + "                startLeft = pb.left - hb.left; "
                + "                startTop  = pb.top  - hb.top; "
                + "                panel.style.left = startLeft + 'px'; "
                + "                panel.style.top = startTop + 'px'; "
                + "                panel.style.bottom = 'auto'; "
                + "                document.addEventListener('mousemove', onMove, true); "
                + "                document.addEventListener('mouseup', onUp, true); "
                + "                if (ev && ev.preventDefault) ev.preventDefault(); "
                + "              } catch(e) {} "
                + "            }, true); "
                  + "            if (handle) { "
                  + "              handle.addEventListener('mousedown', function(ev){ "
                  + "                try { "
                  + "                  if (ev && ev.button !== 0) return; "
                  + "                  var host = document.getElementById('blockly'); "
                  + "                  if (!host) return; "
                  + "                  var hb = host.getBoundingClientRect(); "
                  + "                  var pb = panel.getBoundingClientRect(); "
                  + "                  resizing = true; "
                  + "                  startX = ev.clientX; startY = ev.clientY; "
                  + "                  startW = pb.width; startH = pb.height; "
                  + "                  // Make sure top/left anchoring is active during resize.\n"
                  + "                  panel.style.left = (pb.left - hb.left) + 'px'; "
                  + "                  panel.style.top = (pb.top - hb.top) + 'px'; "
                  + "                  panel.style.bottom = 'auto'; "
                  + "                  document.addEventListener('mousemove', onMove, true); "
                  + "                  document.addEventListener('mouseup', onUp, true); "
                  + "                  if (ev && ev.preventDefault) ev.preventDefault(); "
                  + "                } catch(e) {} "
                  + "              }, true); "
                  + "            } "
                + "          } catch(e) {} "
                + "        })(); "
                + "      } "
                + "      window.__execLogClear = function() { "
                + "        try { var b = document.getElementById('__execLogBody'); if (b) b.textContent = ''; window.__execLogLineCount = 0; } catch(e) {} "
                + "      }; "
                + "      window.__execLogAppend = function(lines) { "
                + "        try { "
                + "          var b = document.getElementById('__execLogBody'); if (!b) return; "
                + "          if (lines === undefined || lines === null) return; "
                + "          if (typeof lines === 'string') lines = [lines]; "
                + "          if (!lines.length) return; "
                + "          var txt = b.textContent || ''; "
                + "          for (var i=0; i<lines.length; i++) { "
                + "            var line = (lines[i] === undefined || lines[i] === null) ? '' : String(lines[i]); "
                + "            txt += (txt.length ? '\\n' : '') + line; "
                + "          } "
                + "          var parts = txt.split(/\\n/); "
                + "          var max = window.__execLogMaxLines || 500; "
                + "          if (parts.length > max) parts = parts.slice(parts.length - max); "
                + "          b.textContent = parts.join('\\n'); "
                + "          b.scrollTop = b.scrollHeight + 1000; "
                + "        } catch(e) {} "
                + "      }; "
                + "      return true; "
                + "    } catch(e) { return false; } "
                + "  } "
                + "  var attempts = 0, maxAttempts = 60, interval = 100; "
                + "  var id = setInterval(function() { "
                + "    try { "
                + "      __execLogEnsure(); "
                + "      var runBtn = document.getElementById('runButton'); "
                + "      if (!runBtn) { attempts++; return; } "
                + "      var container = runBtn.parentNode; "
                + "      if (container && !window.__dbgButtonsBound) { "
                + "        window.__dbgButtonsBound = true; "
                + "        window.__dbgTimer = null; "
                + "        window.__dbgTurnTimers = []; "
                + "        window.__dbgSessionStarted = false; "
                + "        window.__dbgActive = false; "
                + "        window.__dbgStepInFlight = false; "
                + "        try { "
                + "          if (typeof $d === 'function' && document.getElementById('finish')) { $d(false); } "
                + "          window.__stableStartT = (typeof window.T === 'number') ? window.T : 1; "
                + "        } catch(e) { window.__stableStartT = 1; } "
                + "        window.__dbgLastT = undefined; "
                + "        if (!window.__dbgTWatchdog) { "
                + "          window.__dbgTWatchdog = setInterval(function() { "
                + "            try { "
                + "              if (!window.__dbgSessionStarted) return; "
                + "              if (typeof window.T !== 'number') return; "
                + "              if (typeof window.__dbgLastT !== 'number') { window.__dbgLastT = window.T; return; } "
                + "              if (window.__dbgLastT !== window.T) { "
                + "                window.__dbgLastT = window.T; "
                + "                if (window.javaBridge) window.javaBridge.logJS('__dbgWatch T changed to ' + window.T + ' (Q=' + window.Q + ' S=' + window.S + ')'); "
                + "              } "
                + "            } catch(e) {} "
                + "          }, 50); "
                + "        } "
                + "        " + blocky_game.DebuggingService.renderDebugOverlayJsSnippet() + " "
                + "        function __dbgSync() { "
                + "          try { "
                + "            var bridge = window.javaBridge || (window.parent && window.parent.javaBridge); "
                + "            if (!bridge) return; "
                + "            if (typeof getWS === 'function' && typeof sync === 'function') { "
                + "              var ws = getWS(); if (ws) sync(ws); "
                + "            } "
                + "            /* intentionally skip syncMap/syncLevelMeta during debug stepping */ "
                + "          } catch(e) {} "
                + "        } "
                + "        function __dbgSetPegman(q, s, t) { "
                + "          try { "
                + "            if (window.__dbgTurnTimers && window.__dbgTurnTimers.length) { "
                + "              for (var k=0; k<window.__dbgTurnTimers.length; k++) { clearTimeout(window.__dbgTurnTimers[k]); } "
                + "              window.__dbgTurnTimers = []; "
                + "            } "
                + "            var oldQ = window.Q, oldS = window.S, oldT = window.T; "
                + "            if (typeof oldT !== 'number') oldT = t; "
                + "            try { if (window.javaBridge) window.javaBridge.logJS('__dbgSetPegman oldT=' + oldT + ' -> newT=' + t + ' oldQ=' + oldQ + ' oldS=' + oldS + ' newQ=' + q + ' newS=' + s); } catch(e) {} "
                + "            var oldD = 4 * oldT; "
                + "            var newD = 4 * t; "
                + "            window.Q = q; window.S = s; window.T = t; "
                + "            if (typeof Z === 'function') { "
                + "              if (oldQ === q && oldS === s && oldT !== t) { "
                + "                var frames = 4, frameDelay = 50; "
                + "                for (var i=1; i<=frames; i++) { "
                + "                  (function(step){ "
                + "                    var tid = setTimeout(function() { "
                + "                      var d = Math.round(oldD + (newD - oldD) * (step/frames)); "
                + "                      if (typeof Z === 'function') Z(q, s, d); "
                + "                    }, step * frameDelay); "
                + "                    window.__dbgTurnTimers.push(tid); "
                + "                  })(i); "
                + "                } "
                + "              } else { "
                + "                Z(q, s, newD); "
                + "              } "
                + "            } "
                + "          } catch(e) {} "
                + "        } "
                + "        function __dbgRenderFrame(fr) { "
                + "          try { "
                + "            if (!fr) return; "
                + "            window.__dbgSessionStarted = true; "
                + "            if (typeof __dbgRenderOverlay === 'function') __dbgRenderOverlay(fr.prefix); "
                + "            __dbgSetPegman(fr.q, fr.s, fr.t); "
                + "            var pauseBtn = document.getElementById('debugPauseResumeButton'); "
                + "            var stepBtn = document.getElementById('debugStepButton'); "
                + "            var skipBtn = document.getElementById('debugSkipEndButton'); "
                + "            var terminal = !!fr.result && fr.result !== 'RUNNING'; "
                + "            if (pauseBtn) pauseBtn.textContent = fr.paused ? 'Resume' : 'Pause'; "
                + "            if (pauseBtn && terminal) pauseBtn.textContent = 'Resume'; "
                + "            if (stepBtn) { stepBtn.disabled = terminal; stepBtn.title = terminal ? ('Debugger finished: ' + fr.result) : 'Execute one step'; } "
                + "            if (skipBtn) { skipBtn.disabled = terminal; skipBtn.title = terminal ? ('Debugger finished: ' + fr.result) : 'Jump to final outcome'; } "
                + "            if (fr.paused && window.__dbgTimer) { clearInterval(window.__dbgTimer); window.__dbgTimer = null; } "
                + "            if (terminal) { "
                + "              window.__dbgActive = false; "
                + "              if (window.__dbgTimer) { clearInterval(window.__dbgTimer); window.__dbgTimer = null; } "
                + "            } "
                + "          } catch(e) {} "
                + "        } "
                + "        function __dbgStart() { "
                + "          try { "
                + "            try { if (window.__execLogClear) window.__execLogClear(); } catch(e) {} "
                + "            var stepBtn = document.getElementById('debugStepButton'); "
                + "            var skipBtn = document.getElementById('debugSkipEndButton'); "
                + "            if (stepBtn) { stepBtn.disabled = false; stepBtn.title = 'Execute one step'; } "
                + "            if (skipBtn) { skipBtn.disabled = false; skipBtn.title = 'Jump to final outcome'; } "
                + "            window.__dbgStepInFlight = false; "
                + "            window.__dbgLastLoggedIndex = -1; "
                + "            var seedQ = window.Q, seedS = window.S; "
                + "            var seedT = (window.__modelStartT !== undefined) ? window.__modelStartT : ((typeof window.__stableStartT === 'number') ? window.__stableStartT : window.T); "
                + "            if (window.__modelStartT !== undefined) { window.T = seedT; } "
                + "            window.__dbgActive = true; __dbgSync(); "
                + "            if (!window.javaBridge || !window.javaBridge.debugStart) return null; "
                + "            try { window.javaBridge.logJS('__dbgSeed level=' + window.K + ' metaStartT=' + window.T + ' modelStartT=' + window.__modelStartT + ' seedT=' + seedT); } catch(e) {} "
                + "            try { window.javaBridge.logJS('__dbgStart call q=' + seedQ + ' s=' + seedS + ' t=' + seedT); } catch(e) {} "
                + "            var gen = (typeof window.__javaPageGen === 'number') ? window.__javaPageGen : 0; "
                + "            var fr = JSON.parse((window.javaBridge.debugStartWithGen ? window.javaBridge.debugStartWithGen(seedQ, seedS, seedT, gen) : window.javaBridge.debugStart(seedQ, seedS, seedT))); "
                + "            try { window.javaBridge.logJS('__dbgStart frame q=' + fr.q + ' s=' + fr.s + ' t=' + fr.t + ' paused=' + fr.paused + ' result=' + fr.result); } catch(e) {} "
                + "            __dbgRenderFrame(fr); "
                + "            try { if (fr && typeof fr.index === 'number' && window.__dbgLastLoggedIndex !== fr.index) { window.__dbgLastLoggedIndex = fr.index; if (fr.logLine && window.__execLogAppend) window.__execLogAppend(fr.logLine); } } catch(e) {} "
                + "            return fr; "
                + "          } catch(e) { window.__dbgActive = false; return null; } "
                + "        } "
                + "        function __dbgTogglePause() { "
                + "          try { "
                + "            if (!window.__dbgSessionStarted) { __dbgStart(); } "
                + "            try { if (window.javaBridge) window.javaBridge.logJS('__dbgTogglePause before paused=' + (!!window.__dbgTimer ? 'running' : 'paused') + ' T=' + window.T + ' Q=' + window.Q + ' S=' + window.S); } catch(e) {} "
                + "            __dbgSync(); "
                + "            var gen = (typeof window.__javaPageGen === 'number') ? window.__javaPageGen : 0; "
                + "            var fr = JSON.parse((window.javaBridge.debugTogglePauseWithGen ? window.javaBridge.debugTogglePauseWithGen(gen) : window.javaBridge.debugTogglePause())); "
                + "            try { if (window.javaBridge) window.javaBridge.logJS('__dbgTogglePause frame q=' + fr.q + ' s=' + fr.s + ' t=' + fr.t + ' paused=' + fr.paused + ' result=' + fr.result); } catch(e) {} "
                + "            __dbgRenderFrame(fr); "
                + "            try { if (fr && typeof fr.index === 'number' && window.__dbgLastLoggedIndex !== fr.index) { window.__dbgLastLoggedIndex = fr.index; if (fr.logLine && window.__execLogAppend) window.__execLogAppend(fr.logLine); } } catch(e) {} "
                + "            if (!fr.paused && !window.__dbgTimer) { "
                + "              window.__dbgTimer = setInterval(function() { "
                + "                try { "
                + "                  var gen2 = (typeof window.__javaPageGen === 'number') ? window.__javaPageGen : 0; "
                + "                  var fr2 = JSON.parse((window.javaBridge.debugTickWithGen ? window.javaBridge.debugTickWithGen(gen2) : window.javaBridge.debugTick())); "
                + "                  try { if (window.javaBridge) window.javaBridge.logJS('__dbgTick frame q=' + fr2.q + ' s=' + fr2.s + ' t=' + fr2.t + ' paused=' + fr2.paused + ' result=' + fr2.result); } catch(e) {} "
                + "                  __dbgRenderFrame(fr2); "
                + "                  try { if (fr2 && typeof fr2.index === 'number' && window.__dbgLastLoggedIndex !== fr2.index) { window.__dbgLastLoggedIndex = fr2.index; if (fr2.logLine && window.__execLogAppend) window.__execLogAppend(fr2.logLine); } } catch(e) {} "
                + "                  if (fr2.paused && window.__dbgTimer) { clearInterval(window.__dbgTimer); window.__dbgTimer = null; } "
                + "                } catch(e) {} "
                + "              }, " + blocky_game.DebuggingService.DEBUG_TICK_MS + "); "
                + "            } "
                + "          } catch(e) {} "
                + "        } "
                + "        function __dbgStep() { "
                + "          try { "
                + "            if (window.__dbgStepInFlight) return; "
                + "            if (!window.__dbgSessionStarted) { __dbgStart(); } "
                + "            var stepBtn = document.getElementById('debugStepButton'); "
                + "            if (stepBtn) { stepBtn.disabled = true; stepBtn.title = 'Stepping...'; } "
                + "            window.__dbgStepInFlight = true; "
                + "            try { if (window.javaBridge) window.javaBridge.logJS('__dbgStep before call T=' + window.T + ' Q=' + window.Q + ' S=' + window.S); } catch(e) {} "
                + "            __dbgSync(); "
                + "            var gen = (typeof window.__javaPageGen === 'number') ? window.__javaPageGen : 0; "
                + "            var fr = JSON.parse((window.javaBridge.debugStepWithGen ? window.javaBridge.debugStepWithGen(gen) : window.javaBridge.debugStep())); "
                + "            try { if (window.javaBridge) window.javaBridge.logJS('__dbgStep frame q=' + fr.q + ' s=' + fr.s + ' t=' + fr.t + ' paused=' + fr.paused + ' result=' + fr.result); } catch(e) {} "
                + "            __dbgRenderFrame(fr); "
                + "            try { if (fr && typeof fr.index === 'number' && window.__dbgLastLoggedIndex !== fr.index) { window.__dbgLastLoggedIndex = fr.index; if (fr.logLine && window.__execLogAppend) window.__execLogAppend(fr.logLine); } } catch(e) {} "
                + "            try { if (window.javaBridge) window.javaBridge.logJS('__dbgStep after render T=' + window.T + ' Q=' + window.Q + ' S=' + window.S); } catch(e) {} "
                + "            if (window.__dbgTimer) { clearInterval(window.__dbgTimer); window.__dbgTimer = null; } "
                + "          } catch(e) {} finally { "
                + "            window.__dbgStepInFlight = false; "
                + "            var stepBtn2 = document.getElementById('debugStepButton'); "
                + "            if (stepBtn2 && !stepBtn2.disabled) stepBtn2.title = 'Execute one step'; "
                + "          } "
                + "        } "
                + "        function __dbgStop() { "
                + "          try { "
                + "            if (!window.javaBridge || !window.javaBridge.debugStop) return; "
                + "            __dbgSync(); "
                + "            var gen = (typeof window.__javaPageGen === 'number') ? window.__javaPageGen : 0; "
                + "            var fr = JSON.parse((window.javaBridge.debugStopWithGen ? window.javaBridge.debugStopWithGen(gen) : window.javaBridge.debugStop())); "
                + "            __dbgRenderFrame(fr); "
                + "            try { if (window.__execLogClear) window.__execLogClear(); } catch(e) {} "
                + "            window.__dbgLastLoggedIndex = -1; "
                + "            window.__dbgActive = false; "
                + "            if (window.__dbgTimer) { clearInterval(window.__dbgTimer); window.__dbgTimer = null; } "
                + "          } catch(e) {} "
                + "        } "
                + "        function __dbgMkBtn(id, label, title) { "
                + "          var b = document.getElementById(id); "
                + "          if (!b) { b = document.createElement('button'); b.id = id; b.className = 'primary'; b.textContent = label; b.title = title; container.appendChild(b); } "
                + "          return b; "
                + "        } "
                + "        var debugPauseResumeBtn = __dbgMkBtn('debugPauseResumeButton', 'Resume', 'Pause/Resume debugging'); "
                + "        var debugStopBtn = __dbgMkBtn('debugStopButton', 'Stop', 'Stop debugging and reset'); "
                + "        var debugStepBtn = __dbgMkBtn('debugStepButton', 'Step', 'Execute one step'); "
                + "        var debugSkipBtn = __dbgMkBtn('debugSkipEndButton', 'Skip End', 'Jump to final outcome'); "
                + "        debugPauseResumeBtn.addEventListener('click', function() { __dbgTogglePause(); }); "
                + "        debugStopBtn.addEventListener('click', function() { __dbgStop(); }); "
                + "        debugStepBtn.addEventListener('click', function() { __dbgStep(); }); "
                + "        debugSkipBtn.addEventListener('click', function() { "
                + "          try { "
                + "            if (!window.__dbgSessionStarted) __dbgStart(); "
                + "            __dbgSync(); "
                + "            var gen = (typeof window.__javaPageGen === 'number') ? window.__javaPageGen : 0; "
                + "            var fr = JSON.parse((window.javaBridge.debugSkipToEndWithGen ? window.javaBridge.debugSkipToEndWithGen(gen) : window.javaBridge.debugSkipToEnd())); "
                + "            __dbgRenderFrame(fr); "
                + "            try { if (fr && typeof fr.index === 'number' && window.__dbgLastLoggedIndex !== fr.index) { window.__dbgLastLoggedIndex = fr.index; if (fr.logLine && window.__execLogAppend) window.__execLogAppend(fr.logLine); } } catch(e) {} "
                + "            if (window.__dbgTimer) { clearInterval(window.__dbgTimer); window.__dbgTimer = null; } "
                + "          } catch(e) {} "
                + "        }); "
                + "      } "
                + "      if (window.__dbgButtonsBound) clearInterval(id); "
                + "      attempts++; "
                + "      if (attempts >= maxAttempts) clearInterval(id); "
                + "    } catch(e) {} "
                + "  }, interval); "
                + "})();"
            );
        } catch (Exception e) {
            System.err.println("[BlockyUI] injectDebugControls executeScript failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public class JSBridge {
        public void logJS(String msg) {
            System.out.println("[WebView JS] " + msg);
        }

        /** Saves the current model to XMI. Called only when Run Program is clicked. */
        public void saveModelNow() {
            engine.saveModel();
            System.out.println("[JSBridge] saveModelNow -> save XMI");
        }

        /** Receives a base64 dataUrl PNG generated in the WebView. */
        public void receivePngDataUrl(String dataUrl) {
            snapshotService.receivePngDataUrl(dataUrl);
        }

        /** Called by WebView after loaded model state is injected and stable. */
        public void injectComplete() {
            awaitingInjectComplete = false;
            suppressSync = false;
            System.out.println("[JSBridge] Injection complete; sync re-enabled.");
        }

        public void runSimulation() {
            System.out.println("[JSBridge] 'Run' detected. Starting Java simulation...");
            try {
                webView.getEngine().executeScript("try { if (window.__execLogClear) window.__execLogClear(); } catch(e) {}");
            } catch (Exception e) {
                System.err.println("[JSBridge] Failed to clear exec log: " + e.getMessage());
            }
            List<String> logs = engine.simulateUserProgramWithLogs();
            try {
                webView.getEngine().executeScript("try { if (window.__execLogAppend) window.__execLogAppend(" + toJsonStringArrayLiteral(logs) + "); } catch(e) {}");
            } catch (Exception e) {
                System.err.println("[JSBridge] Failed to append exec log: " + e.getMessage());
            }
        }

        /** Same as runSimulation(), but ignores calls from stale WebView pages or while injecting a model. */
        public void runSimulationWithGen(int gen) {
            if (gen != currentPageGen || awaitingInjectComplete) {
                System.out.println("[JSBridge] runSimulationWithGen ignored. gen=" + gen + " current=" + currentPageGen + " injecting=" + awaitingInjectComplete);
                return;
            }
            runSimulation();
        }

        // --- Debugger controls (Java-driven stepping) ---

        /**
         * Starts a debug session from the WebView’s current pegman state.
         * Q,S are cell coordinates; T is direction code (0=N,1=E,2=S,3=W).
         */
        public String debugStart(int q, int s, int t) {
            System.out.println("[JSBridge] debugStart q=" + q + " s=" + s + " t=" + t);
            return engine.debugStart(q, s, t);
        }

        public String debugStartWithGen(int q, int s, int t, int gen) {
            if (gen != currentPageGen || awaitingInjectComplete) {
                System.out.println("[JSBridge] debugStartWithGen ignored. gen=" + gen + " current=" + currentPageGen + " injecting=" + awaitingInjectComplete);
                return engine.debugStop();
            }
            return debugStart(q, s, t);
        }

        public String debugTogglePause() {
            return engine.debugTogglePause();
        }

        public String debugTogglePauseWithGen(int gen) {
            if (gen != currentPageGen || awaitingInjectComplete) return engine.debugStop();
            return debugTogglePause();
        }

        public String debugStop() {
            return engine.debugStop();
        }

        public String debugStopWithGen(int gen) {
            if (gen != currentPageGen) return engine.debugStop();
            return debugStop();
        }

        public String debugStep() {
            return engine.debugStepOnce();
        }

        public String debugStepWithGen(int gen) {
            if (gen != currentPageGen || awaitingInjectComplete) return engine.debugStop();
            return debugStep();
        }

        public String debugSkipToEnd() {
            return engine.debugSkipToEnd();
        }

        public String debugSkipToEndWithGen(int gen) {
            if (gen != currentPageGen || awaitingInjectComplete) return engine.debugStop();
            return debugSkipToEnd();
        }

        public String debugTick() {
            return engine.debugTick();
        }

        public String debugTickWithGen(int gen) {
            if (gen != currentPageGen || awaitingInjectComplete) return engine.debugStop();
            return debugTick();
        }

        public void syncMap(String mapJson) {
            if (suppressSync) return;
            System.out.println("[JSBridge] Received map JSON from JS: " + mapJson);
            engine.setMapFromJson(mapJson);
        }

        public void syncLevelMeta(String metaJson) {
            if (suppressSync) return;
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

    private static String toJsonStringArrayLiteral(List<String> lines) {
        if (lines == null || lines.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJsonString(lines.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeJsonString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
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
        awaitingInjectComplete = true;
        try {
            int[][] grid = engine.buildGridForWebView(level.getMap());
            Cell startCell = engine.getStartCell(level.getMap());
            Cell goalCell = engine.getGoalCell(level.getMap());
            int levelId = Math.max(1, Math.min(10, level.getId()));
            int maxBlocks = level.getMaxBlocks() <= 0 ? -1 : level.getMaxBlocks();

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
            webEngine.executeScript("window.__modelStartT = " + engine.directionToT(level.getStartOrientation()) + ";");
            // Immediate feedback overlay data (old stored trace vs new simulated trace).
            webEngine.executeScript(ImmediateFeedbackService.buildWindowInjectPathsScript(engine.getPastPath(), engine.getNewPath()));

            String xml = engine.solutionToBlocklyXml(level);
            String escapedForJson = xml.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
            webEngine.executeScript("window.__loadXml = \"" + escapedForJson + "\";");

            // 2) Poll until maze DOM and Blockly are ready, then apply our data (overwrite maze defaults), redraw, load blocks, reset pegman
            webEngine.executeScript(
                "(function(){ " +
                "window.__dbgDisableAutoRun = true; " +
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
                "  try { window.__forcedT = undefined; } catch(e) {} " +
                "  if (typeof $d === 'function' && document.getElementById('finish')) { try { $d(false); } catch(e) { if (window.javaBridge) window.javaBridge.logJS('$d: ' + e); } } " +
                "  try { "
                + "    if (window.__modelStartT !== undefined) { "
                + "      window.T = window.__modelStartT; "
                + "      if (typeof Z === 'function') Z(window.Q, window.S, 4 * window.T); "
                + "    } "
                + "  } catch(e) { if (window.javaBridge) window.javaBridge.logJS('set modelStartT: ' + e); } " +
                ImmediateFeedbackService.buildOverlayRenderJs() +
                blocky_game.DebuggingService.renderDebugOverlayJsSnippet() +
                "  /* Debug buttons are injected by injectDebugControls() on every page load. */ " +
                "  try { var bridge = window.javaBridge || (window.parent && window.parent.javaBridge); if (bridge && bridge.injectComplete) bridge.injectComplete(); } catch(e) {} " +
                "}, interval); " +
                "})();"
            );
        } finally {
            // keep suppressSync until injectComplete() callback
            if (!awaitingInjectComplete) suppressSync = false;
        }
    }
}