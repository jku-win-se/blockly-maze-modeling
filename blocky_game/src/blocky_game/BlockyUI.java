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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import netscape.javascript.JSObject;

import blocky.Cell;
import blocky.Level;
import blocky.Game;
import blocky.Direction;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.common.util.URI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    /** If true, after the next injectComplete we show+refresh the MoMoT solutions panel. */
    private volatile boolean pendingShowMomotPanel;
    /** When set, MoMoT panel only shows solutions from this output directory (current run). */
    private volatile String momotCurrentOutputDir;
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
                    System.out.println("[BlockyUI] WebView page load SUCCEEDED: " + webEngine.getLocation());
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
                }
            });

            // Menu Bar
            MenuBar menuBar = new MenuBar();
            Menu fileMenu = new Menu("File");
            MenuItem loadItem = new MenuItem("Load XMI...");
            loadItem.setOnAction(e -> loadModelImpl());
            MenuItem saveItem = new MenuItem("Save XMI...");
            saveItem.setOnAction(e -> saveModelImpl());
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.setOnAction(e -> Platform.exit());
            fileMenu.getItems().addAll(loadItem, saveItem, exitItem);
            menuBar.getMenus().add(fileMenu);

            StackPane content = new StackPane(webView, snapshotButton);
            BorderPane root = new BorderPane();
            root.setTop(menuBar);
            root.setCenter(content);
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
                "  try { window.__blockyRunStarted = false; } catch(e) {}\n" +
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
                "    try { if (xml && xml.indexOf('<block') < 0) xml = null; } catch(e0) {}\n" +
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
                "      try { if (window.__lvlLoadingHide) window.__lvlLoadingHide(); } catch(eL) {}\n" +
                "      try { var __ov = document.getElementById('__lvlLoadingOverlay'); if (__ov) __ov.style.display = 'none'; } catch(eL2) {}\n" +
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
                "            var hasIfElse = tbHtml.indexOf('maze_ifElse') !== -1;\n" +
                "            var sd = (typeof window.__stableStartT === 'number') ? window.__stableStartT : ((typeof window.T !== 'undefined') ? window.T : 1);\n" +
                "            var meta = JSON.stringify({ level: lvl, maxBlocks: mxb, startDirection: sd,\n" +
                "                                        allowLoops: hasLoops, allowConditionals: hasConds, allowIfElse: hasIfElse });\n" +
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
                "            try {\n" +
                "              // Blockly often resets the start direction to EAST (T=1) when Run starts.\n" +
                "              // If we loaded a model-specific start orientation, keep pegman facing it.\n" +
                "              if (typeof window.__modelStartT === 'number') {\n" +
                "                window.__stableStartT = window.__modelStartT;\n" +
                "                window.T = window.__modelStartT;\n" +
                "                if (typeof Z === 'function') Z(window.Q, window.S, 4 * window.T);\n" +
                "              }\n" +
                "            } catch(e) { /* ignore */ }\n" +
                "            try { window.__blockyRunStarted = true; } catch(e) {}\n" +
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
                + "  // --- Level loading overlay (prevents early clicks while WebView syncs meta) ---\n"
                + "  function __lvlLoadingEnsure() { "
                + "    try { "
                + "      if (window.__lvlLoadingReady) return true; "
                + "      var ov = document.getElementById('__lvlLoadingOverlay'); "
                + "      if (!ov) { "
                + "        ov = document.createElement('div'); "
                + "        ov.id = '__lvlLoadingOverlay'; "
                + "        ov.style.position = 'fixed'; "
                + "        ov.style.left = '0'; ov.style.top = '0'; ov.style.right = '0'; ov.style.bottom = '0'; "
                + "        ov.style.background = 'rgba(0,0,0,0.35)'; "
                + "        ov.style.zIndex = '999999'; "
                + "        ov.style.display = 'none'; "
                + "        ov.style.alignItems = 'center'; "
                + "        ov.style.justifyContent = 'center'; "
                + "        ov.style.pointerEvents = 'auto'; "
                + "        var box = document.createElement('div'); "
                + "        box.style.width = '360px'; "
                + "        box.style.maxWidth = '90vw'; "
                + "        box.style.padding = '16px 16px 14px 16px'; "
                + "        box.style.borderRadius = '10px'; "
                + "        box.style.background = 'rgba(40,40,40,0.92)'; "
                + "        box.style.border = '1px solid rgba(255,255,255,0.18)'; "
                + "        box.style.boxShadow = '0 8px 30px rgba(0,0,0,0.35)'; "
                + "        var title = document.createElement('div'); "
                + "        title.id = '__lvlLoadingTitle'; "
                + "        title.textContent = 'Loading level…'; "
                + "        title.style.color = '#fff'; "
                + "        title.style.fontSize = '15px'; "
                + "        title.style.fontWeight = '600'; "
                + "        title.style.marginBottom = '10px'; "
                + "        var bar = document.createElement('div'); "
                + "        bar.style.height = '10px'; "
                + "        bar.style.borderRadius = '8px'; "
                + "        bar.style.overflow = 'hidden'; "
                + "        bar.style.background = 'rgba(255,255,255,0.14)'; "
                + "        var fill = document.createElement('div'); "
                + "        fill.id = '__lvlLoadingFill'; "
                + "        fill.style.height = '100%'; "
                + "        fill.style.width = '40%'; "
                + "        fill.style.borderRadius = '8px'; "
                + "        fill.style.background = 'linear-gradient(90deg,#4d90fe,#3bd46a,#ffcc00)'; "
                + "        fill.style.backgroundSize = '200% 100%'; "
                + "        fill.style.animation = '__lvlLoadAnim 1.8s linear infinite'; "
                + "        bar.appendChild(fill); "
                + "        var sub = document.createElement('div'); "
                + "        sub.textContent = 'Syncing toolbox & metadata…'; "
                + "        sub.style.color = 'rgba(255,255,255,0.82)'; "
                + "        sub.style.fontSize = '12px'; "
                + "        sub.style.marginTop = '10px'; "
                + "        box.appendChild(title); "
                + "        box.appendChild(bar); "
                + "        box.appendChild(sub); "
                + "        ov.appendChild(box); "
                + "        document.body.appendChild(ov); "
                + "        var st = document.getElementById('__lvlLoadingStyle'); "
                + "        if (!st) { "
                + "          st = document.createElement('style'); st.id = '__lvlLoadingStyle'; "
                + "          st.textContent = '@keyframes __lvlLoadAnim { 0%{transform:translateX(-30%);background-position:0% 50%;} 100%{transform:translateX(220%);background-position:100% 50%;} }'; "
                + "          document.head.appendChild(st); "
                + "        } "
                + "      } "
                + "      window.__lvlLoadingReady = true; "
                + "      window.__lvlLoadingShow = function(txt) { "
                + "        try { __lvlLoadingEnsure(); var t = document.getElementById('__lvlLoadingTitle'); if (t && txt) t.textContent = txt; "
                + "          var o = document.getElementById('__lvlLoadingOverlay'); if (o) o.style.display = 'flex'; "
                + "          window.__lvlLoadingStart = Date.now(); "
                + "        } catch(e) {} "
                + "      }; "
                + "      window.__lvlLoadingHide = function() { "
                + "        try { "
                + "          var o2 = document.getElementById('__lvlLoadingOverlay'); if (!o2) return; "
                + "          var min = 1500; "
                + "          var elapsed = Date.now() - (window.__lvlLoadingStart || 0); "
                + "          if (elapsed < min) { "
                + "            setTimeout(function() { o2.style.display = 'none'; }, min - elapsed); "
                + "          } else { "
                + "            o2.style.display = 'none'; "
                + "          } "
                + "        } catch(e) {} "
                + "      }; "
                + "      return true; "
                + "    } catch(e) { return false; } "
                + "  } "
                + "  __lvlLoadingEnsure(); "
                + "  try { window.__lvlLoadingShow('Loading level…'); } catch(e) {} "
                + "  // Show overlay when user clicks a level pill.\n"
                + "  try { "
                + "    if (!window.__lvlLoadingClickBound) { "
                + "      window.__lvlLoadingClickBound = true; "
                + "      document.addEventListener('click', function(ev) { "
                + "        try { "
                + "          var el = ev && ev.target ? ev.target : null; "
                + "          if (!el) return; "
                + "          if (el.id === 'levelModel' || (el.classList && el.classList.contains('level_number'))) { "
                + "            var label = (el.textContent && el.textContent.trim()) ? el.textContent.trim() : 'level'; "
                + "            if (label.toLowerCase() === 'save' || label.toLowerCase() === 'load') return; "
                + "            window.__lvlLoadingShow('Loading ' + label + '…'); "
                + "          } "
                + "        } catch(e2) {} "
                + "      }, true); "
                + "    } "
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
                + "  function __momotEnsure() { "
                + "    try { "
                + "      if (window.__momotReady) return true; "
                + "      var host = document.getElementById('blockly'); "
                + "      if (!host) return false; "
                + "      var existing = document.getElementById('__momotPanel'); "
                + "      if (!existing) { "
                + "        var panel = document.createElement('div'); panel.id = '__momotPanel'; "
                + "        panel.style.position = 'absolute'; "
                + "        panel.style.right = '10px'; "
                + "        panel.style.bottom = '70px'; "
                + "        panel.style.width = '420px'; "
                + "        panel.style.height = '260px'; "
                + "        panel.style.minWidth = '300px'; "
                + "        panel.style.minHeight = '140px'; "
                + "        panel.style.maxWidth = '680px'; "
                + "        panel.style.maxHeight = '560px'; "
                + "        panel.style.overflow = 'hidden'; "
                + "        panel.style.background = 'rgba(32,32,32,0.88)'; "
                + "        panel.style.borderRadius = '8px'; "
                + "        panel.style.border = '1px solid rgba(255,255,255,0.15)'; "
                + "        panel.style.boxShadow = '2px 2px 5px rgba(0,0,0,0.35)'; "
                + "        panel.style.zIndex = '999'; "
                + "        panel.style.display = 'none'; "
                + "        var header = document.createElement('div'); header.id = '__momotHeader'; "
                + "        header.style.display = 'flex'; header.style.alignItems = 'center'; header.style.justifyContent = 'space-between'; "
                + "        header.style.padding = '6px 8px'; header.style.color = '#fff'; header.style.fontSize = '14px'; header.style.userSelect = 'none'; "
                + "        header.style.cursor = 'move'; "
                + "        var title = document.createElement('div'); title.textContent = 'MoMoT solutions'; title.style.fontWeight = 'bold'; "
                + "        var right = document.createElement('div'); right.style.display = 'flex'; right.style.gap = '6px'; "
                + "        function mkBtn(id, label, tip) { "
                + "          var b = document.createElement('button'); b.id = id; b.textContent = label; b.title = tip; "
                + "          b.style.margin = '0'; b.style.padding = '4px 8px'; b.style.fontSize = '12px'; "
                + "          b.style.borderRadius = '4px'; b.style.border = '1px solid rgba(255,255,255,0.25)'; "
                + "          b.style.background = 'rgba(255,255,255,0.10)'; b.style.color = '#fff'; "
                + "          b.style.cursor = 'pointer'; "
                + "          return b; "
                + "        } "
                + "        function mkInput(id, val, tip, width) { "
                + "          var i = document.createElement('input'); i.id = id; i.value = val; i.title = tip; "
                + "          i.style.width = width || '40px'; i.style.fontSize = '11px'; i.style.padding = '2px 4px'; "
                + "          i.style.background = 'rgba(0,0,0,0.3)'; i.style.color = '#fff'; i.style.border = '1px solid rgba(255,255,255,0.2)'; "
                + "          i.style.borderRadius = '3px'; "
                + "          return i; "
                + "        } "
                + "        var settings = document.createElement('div'); settings.id = '__momotSettings'; "
                + "        settings.style.display = 'flex'; settings.style.flexWrap = 'wrap'; settings.style.gap = '6px'; "
                + "        settings.style.padding = '6px 8px'; settings.style.borderBottom = '1px solid rgba(255,255,255,0.1)'; "
                + "        settings.style.alignItems = 'center'; settings.style.fontSize = '11px'; "
                + "        settings.style.color = '#fff'; "
                + "        var labSeed = document.createElement('span'); labSeed.textContent = 'Seed:'; "
                + "        var inpSeed = mkInput('__momotInpSeed', '0', 'Random seed (0 for auto)', '35px'); "
                + "        var labPop = document.createElement('span'); labPop.textContent = 'Pop:'; "
                + "        var inpPop = mkInput('__momotInpPop', '50', 'Population size', '35px'); "
                + "        var labIter = document.createElement('span'); labIter.textContent = 'Iter:'; "
                + "        var inpIter = mkInput('__momotInpIter', '40', 'Number of iterations (generations)', '30px'); "
                + "        var labRuns = document.createElement('span'); labRuns.textContent = 'Runs:'; "
                + "        var inpRuns = mkInput('__momotInpRuns', '10', 'Number of algorithm runs', '25px'); "
                + "        var labSolLen = document.createElement('span'); labSolLen.textContent = 'SolLen:'; "
                + "        var inpSolLen = mkInput('__momotInpSolLen', '10', 'Solution length (number of transformation steps)', '25px'); "
                + "        var btnCont = document.createElement('div'); btnCont.style.display = 'flex'; btnCont.style.gap = '4px'; "
                + "        var mRunBtn = mkBtn('__momotRunBtn', 'Run', 'Execute MOMoT search'); "
                + "        mRunBtn.style.background = 'rgba(70, 150, 70, 0.6)'; "
                + "        var mStopBtn = mkBtn('__momotStopBtn', 'Stop', 'Stop current MOMoT search'); "
                + "        mStopBtn.style.background = 'rgba(180, 50, 50, 0.6)'; "
                + "        btnCont.appendChild(mRunBtn); btnCont.appendChild(mStopBtn); "
                + "        settings.appendChild(labSeed); settings.appendChild(inpSeed); "
                + "        settings.appendChild(labPop); settings.appendChild(inpPop); "
                + "        settings.appendChild(labIter); settings.appendChild(inpIter); "
                + "        settings.appendChild(labRuns); settings.appendChild(inpRuns); "
                + "        settings.appendChild(labSolLen); settings.appendChild(inpSolLen); "
                + "        settings.appendChild(btnCont); "
                + "        var refreshBtn = mkBtn('__momotRefreshBtn', 'Refresh', 'Reload solutions from output folders'); "
                + "        right.appendChild(refreshBtn); "
                + "        header.appendChild(title); header.appendChild(right); "
                + "        var body = document.createElement('div'); body.id = '__momotBody'; "
                + "        body.style.padding = '6px 8px'; body.style.height = 'calc(100% - 38px)'; body.style.overflow = 'auto'; "
                + "        body.style.color = '#fff'; body.style.fontFamily = 'Consolas, Menlo, Monaco, monospace'; body.style.fontSize = '12px'; "
                + "        var list = document.createElement('div'); list.id = '__momotList'; "
                + "        list.style.height = '140px'; list.style.overflow = 'auto'; "
                + "        list.style.border = '1px solid rgba(255,255,255,0.1)'; "
                + "        list.style.borderRadius = '4px'; "
                + "        list.style.background = 'rgba(0,0,0,0.2)'; "
                + "        list.style.display = 'none'; "
                + "        var actions = document.createElement('div'); actions.id = '__momotActions'; "
                + "        actions.style.display = 'flex'; actions.style.gap = '6px'; actions.style.marginTop = '8px'; "
                + "        var loadBtn = mkBtn('__momotLoadBtn', 'Load', 'Load selected model into the game'); "
                + "        actions.appendChild(loadBtn); "
                + "        var status = document.createElement('div'); status.id = '__momotStatus'; status.style.marginTop = '6px'; "
                + "        status.style.color = '#0f0'; status.style.fontWeight = 'bold'; "
                + "        status.textContent = 'Click Refresh or Run to see solutions.'; "
                + "        var log = document.createElement('pre'); log.id = '__momotLog'; "
                + "        log.style.margin = '8px 0 0 0'; log.style.padding = '6px 8px'; "
                + "        log.style.height = '80px'; log.style.overflow = 'auto'; "
                + "        log.style.background = 'rgba(0,0,0,0.4)'; "
                + "        log.style.color = '#ddd'; "
                + "        log.style.border = '1px solid rgba(255,255,255,0.15)'; "
                + "        log.style.borderRadius = '6px'; "
                + "        log.style.whiteSpace = 'pre-wrap'; log.style.wordBreak = 'break-word'; "
                + "        log.textContent = ''; "
                + "        body.appendChild(list); body.appendChild(actions); body.appendChild(status); body.appendChild(log); "
                + "        panel.appendChild(header); panel.appendChild(settings); panel.appendChild(body); "
                + "        var rh = document.createElement('div'); rh.id = '__momotResizeHandle'; "
                + "        rh.style.position = 'absolute'; rh.style.right = '2px'; rh.style.bottom = '2px'; "
                + "        rh.style.width = '14px'; rh.style.height = '14px'; "
                + "        rh.style.cursor = 'se-resize'; "
                + "        rh.style.opacity = '0.85'; "
                + "        rh.style.background = 'linear-gradient(135deg, rgba(255,255,255,0.0) 0%, rgba(255,255,255,0.0) 45%, rgba(255,255,255,0.35) 46%, rgba(255,255,255,0.35) 55%, rgba(255,255,255,0.0) 56%, rgba(255,255,255,0.0) 100%)'; "
                + "        panel.appendChild(rh); "
                + "        host.appendChild(panel); "
                + "        window.__momotSelectedPath = null; "
                + "        window.__momotSortCol = 0; "
                + "        window.__momotSortDir = 1; "
                + "        window.__momotLastData = []; "
                + "        function setStatus(msg) { try { status.textContent = msg || ''; } catch(e) {} } "
                + "        function logClear() { try { log.textContent = ''; } catch(e) {} } "
                + "        function logAppend(txt) { "
                + "          try { "
                + "            if (txt === undefined || txt === null) return; "
                + "            var s = String(txt); "
                + "            if (!s.length) return; "
                + "            var cur = log.textContent || ''; "
                + "            cur += (cur.length ? '\\n' : '') + s; "
                + "            var parts = cur.split(/\\n/); "
                + "            var max = 400; "
                + "            if (parts.length > max) parts = parts.slice(parts.length - max); "
                + "            log.textContent = parts.join('\\n'); "
                + "            log.scrollTop = log.scrollHeight + 1000; "
                + "          } catch(e) {} "
                + "        } "
                + "        window.__momotSetStatus = setStatus; "
                + "        window.__momotLogClear = logClear; "
                + "        window.__momotLogAppend = logAppend; "
                + "        function __dbgDrawComparisonPath(path) { "
                + "          try { "
                + "            window.__injectDmEnabled = !!(path && path.length >= 2); "
                + "            window.__injectDmSolutionPath = path || []; "
                + "            // For simple comparison without a baseline, use an empty baseline\n"
                + "            if (!window.__injectDmBaselinePath) window.__injectDmBaselinePath = []; "
                + "            if (typeof window.__ifRender === 'function') window.__ifRender(); "
                + "          } catch(e) {} "
                + "        } "
                + "        window.__dbgDrawComparisonPath = __dbgDrawComparisonPath; "
                + "        function renderSolutions(arr) { "
                + "          try { "
                + "            if (arr) window.__momotLastData = arr; else arr = window.__momotLastData; "
                + "            list.innerHTML = ''; "
                + "            window.__momotSelectedPath = null; "
                + "            if (!arr || !arr.length) { "
                + "              setStatus('No solutions found.'); "
                + "              list.style.display = 'none'; "
                + "              return; "
                + "            } "
                + "            list.style.display = 'block'; "
                + "            var maxObj = 0; "
                + "            var processed = arr.map(function(it) { "
                + "              var ln = it.objectiveLine || \"\"; "
                + "              if (ln === \"(unknown)\") ln = \"\"; "
                + "              var objs = ln.trim().split(/\\s+/).filter(Boolean).map(Number); "
                + "              if (objs.length > maxObj) maxObj = objs.length; "
                + "              var name = (it.modelPath || \"\").split(/[\\\\\\/]/).pop(); "
                + "              return { it: it, objs: objs, modelName: name }; "
                + "            }); "
                + "            if (window.__momotSortCol !== -1) { "
                + "              processed.sort(function(a, b) { "
                + "                var vA, vB; "
                + "                if (window.__momotSortCol < maxObj) { "
                + "                  vA = a.objs[window.__momotSortCol] !== undefined ? a.objs[window.__momotSortCol] : Infinity; "
                + "                  vB = b.objs[window.__momotSortCol] !== undefined ? b.objs[window.__momotSortCol] : Infinity; "
                + "                } else { "
                + "                  vA = a.modelName; vB = b.modelName; "
                + "                } "
                + "                if (vA < vB) return -1 * window.__momotSortDir; "
                + "                if (vA > vB) return 1 * window.__momotSortDir; "
                + "                return 0; "
                + "              }); "
                + "            } "
                + "            setStatus(arr.length + ' solution(s) found.'); "
                 + "            var table = document.createElement('table'); "
                 + "            table.style.width = '100%'; table.style.borderCollapse = 'collapse'; table.style.fontSize = '11px'; "
                 + "            table.style.border = '1px solid rgba(255,255,255,0.25)'; "
                 + "            var thead = document.createElement('thead'); "
                 + "            var hRow = document.createElement('tr'); "
                 + "            hRow.style.position = 'sticky'; hRow.style.top = '0'; hRow.style.background = '#444'; hRow.style.zIndex = '10'; "
                 + "            function mkTh(label, colIdx) { "
                 + "              var th = document.createElement('th'); "
                 + "              th.textContent = label; th.style.padding = '6px 8px'; th.style.border = '1px solid rgba(255,255,255,0.25)'; "
                 + "              th.style.cursor = 'pointer'; th.style.textAlign = 'left'; "
                 + "              if (window.__momotSortCol === colIdx) th.textContent += (window.__momotSortDir === 1 ? ' ▲' : ' ▼'); "

                + "              th.addEventListener('click', function() { "
                + "                if (window.__momotSortCol === colIdx) window.__momotSortDir *= -1; "
                + "                else { window.__momotSortCol = colIdx; window.__momotSortDir = 1; } "
                + "                renderSolutions(); "
                + "              }); "
                + "              return th; "
                + "            } "
                + "            var objNames = ['Goal Reached', 'Edits', 'Shortest Path', 'Closest to Goal']; "
                + "            var displayCols = Math.max(maxObj, objNames.length); "
                + "            for (var i=0; i<displayCols; i++) hRow.appendChild(mkTh(objNames[i] || ('Obj ' + (i+1)), i)); "
                + "            hRow.appendChild(mkTh('Model', 999)); "
                + "            thead.appendChild(hRow); table.appendChild(thead); "
                + "            var tbody = document.createElement('tbody'); "
                 + "            processed.forEach(function(p) { "
                 + "              var tr = document.createElement('tr'); "
                 + "              tr.style.cursor = 'pointer'; "
                   + "              for (var i=0; i<displayCols; i++) { "
                   + "                var td = document.createElement('td'); "
                   + "                var val = p.objs[i]; "
                  + "                if (i === 0) { "
                  + "                  if (val === -1) td.textContent = 'TRUE'; "
                  + "                  else if (val === 0) td.textContent = 'FALSE'; "
                   + "                  else td.textContent = (val !== undefined && !isNaN(val)) ? val : '-'; "
                   + "                } else { "
                   + "                  td.textContent = (val !== undefined && !isNaN(val)) ? val : '-'; "
                  + "                } "
                  + "                td.style.padding = '6px 8px'; td.style.textAlign = 'right'; "
                  + "                td.style.border = '1px solid rgba(255,255,255,0.15)'; "
                  + "                tr.appendChild(td); "
                  + "              } "
                 + "              var tdM = document.createElement('td'); "
                 + "              tdM.textContent = p.modelName; tdM.style.padding = '6px 8px'; "
                 + "              tdM.style.border = '1px solid rgba(255,255,255,0.15)'; "
                 + "              tr.appendChild(tdM); "
                 + "              tr.addEventListener('mouseenter', function() { if (window.__momotSelectedPath !== p.it.modelPath) tr.style.background = 'rgba(255,255,255,0.05)'; }); "
                 + "              tr.addEventListener('mouseleave', function() { if (window.__momotSelectedPath !== p.it.modelPath) tr.style.background = 'transparent'; }); "

                + "              tr.addEventListener('click', function() { "
                + "                window.__momotSelectedPath = p.it.modelPath; "
                + "                var kids = tbody.children; "
                + "                for (var k=0; k<kids.length; k++) kids[k].style.background = 'transparent'; "
                + "                tr.style.background = 'rgba(255,255,255,0.15)'; "
                + "                setStatus('Selected: ' + p.modelName); "
                + "                try { "
                + "                  var bridge = window.javaBridge || (window.parent && window.parent.javaBridge); "
                + "                  if (bridge && bridge.getSolutionPath) { "
                + "                    var pathJson = bridge.getSolutionPath(p.it.modelPath); "
                + "                    var path = JSON.parse(pathJson); "
                + "                    if (window.__dbgDrawComparisonPath) window.__dbgDrawComparisonPath(path); "
                + "                  } "
                + "                } catch(eP) {} "
                + "              }); "
                + "              tr.addEventListener('dblclick', function() { "
                + "                var bridge = window.javaBridge || (window.parent && window.parent.javaBridge); "
                + "                if (bridge && bridge.loadMomotSolution) { "
                + "                  setStatus('Loading ' + p.modelName + '...'); "
                + "                  bridge.loadMomotSolution(p.it.modelPath); "
                + "                } "
                + "              }); "
                + "              tbody.appendChild(tr); "
                + "            }); "
                + "            table.appendChild(tbody); list.appendChild(table); "
                + "          } catch(e) { setStatus('Failed to render: ' + e); } "
                + "        } "
                + "        function refresh() { "
                + "          try { "
                + "            var bridge = window.javaBridge || (window.parent && window.parent.javaBridge); "
                + "            if (!bridge || !bridge.listMomotSolutions) { setStatus('Java bridge listMomotSolutions not available'); return; } "
                + "            setStatus('Loading solutions...'); "
                + "            var txt = bridge.listMomotSolutions(); "
                + "            var arr = []; "
                + "            try { arr = JSON.parse(txt); } catch(e2) { arr = []; } "
                + "            renderSolutions(arr); "
                + "            try { if (window.__dbgDrawComparisonPath) window.__dbgDrawComparisonPath([]); } catch(eC) {} "
                + "            try { "
                + "              var oldMarker = document.getElementById('dmgMarker'); "
                + "              if (oldMarker && oldMarker.parentNode) oldMarker.parentNode.removeChild(oldMarker); "
                + "            } catch(eM) {} "
                + "          } catch(e) { setStatus('Refresh failed'); } "
                + "        } "
                + "        window.__momotShowAndRefresh = function(){ try { panel.style.display = 'block'; } catch(e) {} try { refresh(); } catch(e2) {} }; "
                + "        refreshBtn.addEventListener('click', function(){ refresh(); }); "
                + "        mStopBtn.addEventListener('click', function(){ "
                + "          try { "
                + "            var bridge = window.javaBridge || (window.parent && window.parent.javaBridge); "
                + "            if (bridge && bridge.stopMomotRun) { "
                + "              bridge.stopMomotRun(); "
                + "              setStatus('MoMoT stopping...'); "
                + "            } "
                + "          } catch(e) { setStatus('Stop failed'); } "
                + "        }); "
                + "        mRunBtn.addEventListener('click', function(){ "
                + "          try { "
                + "            var bridge = window.javaBridge || (window.parent && window.parent.javaBridge); "
                + "            if (!bridge || !bridge.runMomotWithParams) { setStatus('Java bridge runMomotWithParams not available'); return; } "
                + "            try { "
                + "              if (window.Z && typeof window.__preDmQ === 'number') { "
                + "                window.Q = window.__preDmQ; "
                + "                window.S = window.__preDmS; "
                + "                var t = (typeof window.__stableStartT === 'number') ? window.__stableStartT : window.T; "
                + "                window.T = t; "
                + "                Z(window.Q, window.S, 4 * t); "
                + "              } "
                + "            } catch(eT) {} "
                + "            var s = parseInt(document.getElementById('__momotInpSeed').value) || 0; "
                + "            var p = parseInt(document.getElementById('__momotInpPop').value) || 50; "
                + "            var it = parseInt(document.getElementById('__momotInpIter').value) || 40; "
                + "            var e = p * it; "
                + "            var r = parseInt(document.getElementById('__momotInpRuns').value) || 10; "
                + "            var sl = parseInt(document.getElementById('__momotInpSolLen').value) || 10; "
                + "            setStatus('Starting MoMoT (seed=' + s + ', pop=' + p + ', iter=' + it + ' (eval=' + e + '), runs=' + r + ', solLen=' + sl + ')...'); "
                + "            try { if (window.__dbgDrawComparisonPath) window.__dbgDrawComparisonPath([]); } catch(eC) {} "
                + "            bridge.runMomotWithParams(s, p, e, r, sl); "
                + "          } catch(e) { setStatus('Run failed: ' + e); } "
                + "        }); "
                + "        loadBtn.addEventListener('click', function(){ "
                + "          try { "
                + "            var p = window.__momotSelectedPath; "
                + "            if (!p) { setStatus('Select a solution first'); return; } "
                + "            var bridge = window.javaBridge || (window.parent && window.parent.javaBridge); "
                + "            if (!bridge || !bridge.loadMomotSolution) { setStatus('Java bridge loadMomotSolution not available'); return; } "
                + "            setStatus('Loading model...'); "
                + "            bridge.loadMomotSolution(p); "
                + "          } catch(e) { setStatus('Load failed'); } "
                + "        }); "
                + "        setStatus('Hidden until Direct Manipulation teleport.'); "
                + "        (function(){ "
                + "          try { "
                + "            var dragging = false; var resizing = false; "
                + "            var startX = 0, startY = 0, startLeft = 0, startTop = 0; "
                + "            var startW = 0, startH = 0; "
                + "            function clamp(v, min, max) { return Math.max(min, Math.min(max, v)); } "
                + "            function onMove(ev) { "
                + "              try { "
                + "                var hb = host.getBoundingClientRect(); "
                + "                if (resizing) { "
                + "                  var dx = ev.clientX - startX; var dy = ev.clientY - startY; "
                + "                  var newW = startW + dx; var newH = startH + dy; "
                + "                  var minW = 300, minH = 140, maxW = 680, maxH = 560; "
                + "                  newW = clamp(newW, minW, maxW); newH = clamp(newH, minH, maxH); "
                + "                  var pb = panel.getBoundingClientRect(); "
                + "                  var leftInHost = pb.left - hb.left; var topInHost = pb.top - hb.top; "
                + "                  newW = clamp(newW, minW, Math.max(minW, hb.width - leftInHost)); "
                + "                  newH = clamp(newH, minH, Math.max(minH, hb.height - topInHost)); "
                + "                  panel.style.width = Math.round(newW) + 'px'; panel.style.height = Math.round(newH) + 'px'; "
                + "                  return; "
                + "                } "
                + "                if (dragging) { "
                + "                  var dx2 = ev.clientX - startX; var dy2 = ev.clientY - startY; "
                + "                  var pb2 = panel.getBoundingClientRect(); "
                + "                  var newLeft = startLeft + dx2; var newTop = startTop + dy2; "
                + "                  var maxLeft = Math.max(0, hb.width - pb2.width); "
                + "                  var maxTop  = Math.max(0, hb.height - pb2.height); "
                + "                  newLeft = clamp(newLeft, 0, maxLeft); newTop = clamp(newTop, 0, maxTop); "
                + "                  panel.style.left = newLeft + 'px'; panel.style.top = newTop + 'px'; "
                + "                  panel.style.right = 'auto'; panel.style.bottom = 'auto'; "
                + "                } "
                + "              } catch(e) {} "
                + "            } "
                + "            function onUp() { "
                + "              dragging = false; resizing = false; "
                + "              try { document.removeEventListener('mousemove', onMove, true); document.removeEventListener('mouseup', onUp, true); } catch(e) {} "
                + "            } "
                + "            header.addEventListener('mousedown', function(ev){ "
                + "              try { "
                + "                if (ev && ev.button !== 0) return; "
                + "                var hb = host.getBoundingClientRect(); "
                + "                var pb = panel.getBoundingClientRect(); "
                + "                dragging = true; startX = ev.clientX; startY = ev.clientY; "
                + "                startLeft = pb.left - hb.left; startTop = pb.top - hb.top; "
                + "                panel.style.left = startLeft + 'px'; panel.style.top = startTop + 'px'; "
                + "                panel.style.right = 'auto'; panel.style.bottom = 'auto'; "
                + "                document.addEventListener('mousemove', onMove, true); document.addEventListener('mouseup', onUp, true); "
                + "                if (ev && ev.preventDefault) ev.preventDefault(); "
                + "              } catch(e) {} "
                + "            }, true); "
                + "            rh.addEventListener('mousedown', function(ev){ "
                + "              try { "
                + "                if (ev && ev.button !== 0) return; "
                + "                var hb = host.getBoundingClientRect(); "
                + "                var pb = panel.getBoundingClientRect(); "
                + "                resizing = true; startX = ev.clientX; startY = ev.clientY; "
                + "                startW = pb.width; startH = pb.height; "
                + "                panel.style.left = (pb.left - hb.left) + 'px'; panel.style.top = (pb.top - hb.top) + 'px'; "
                + "                panel.style.right = 'auto'; panel.style.bottom = 'auto'; "
                + "                document.addEventListener('mousemove', onMove, true); document.addEventListener('mouseup', onUp, true); "
                + "                if (ev && ev.preventDefault) ev.preventDefault(); "
                + "              } catch(e) {} "
                + "            }, true); "
                + "          } catch(e) {} "
                + "        })(); "
                + "      } "
                + "      window.__momotReady = true; "
                + "      return true; "
                + "    } catch(e) { return false; } "
                + "  } "
                + "  var attempts = 0, maxAttempts = 60, interval = 100; "
                + "  var id = setInterval(function() { "
                + "    try { "
                + "      __execLogEnsure(); "
                + "      __momotEnsure(); "
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
                + "        window.__dbgLastHighlightedId = null; "
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
                + "        " + blocky_game.ImmediateFeedbackService.buildOverlayRenderJs() + " "
                + "        function __dbgSync() { "
                + "          try { "
                + "            var bridge = window.javaBridge || (window.parent && window.parent.javaBridge); "
                + "            if (!bridge) return; "
                + "            if (typeof getWS === 'function' && typeof sync === 'function') { "
                + "              var ws = getWS(); if (ws) sync(ws); "
                + "            } "
                + "            // After a paused edit, refresh the current debug frame so buttons/overlays\n"
                + "            // reflect dirty=true even if we are currently in a terminal state.\n"
                + "            try { "
                + "              if (window.__dbgSessionStarted && bridge.debugTick) { "
                + "                var gen0 = (typeof window.__javaPageGen === 'number') ? window.__javaPageGen : 0; "
                + "                var fr0 = JSON.parse((bridge.debugTickWithGen ? bridge.debugTickWithGen(gen0) : bridge.debugTick())); "
                + "                __dbgRenderFrame(fr0); "
                + "              } "
                + "            } catch(e2) {} "
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
                + "            if (typeof __dbgRenderOverlay === 'function') __dbgRenderOverlay(fr.prefix, fr.pastPrefix, fr.newPreview, fr.common); "
                + "            try { "
                + "              // Resolve workspace (Blockly Games Maze often exposes it via BlocklyInterface).\n"
                + "              var ws = null; "
                + "              try { "
                + "                if (window.BlocklyInterface && window.BlocklyInterface.getWorkspace) { "
                + "                  ws = window.BlocklyInterface.getWorkspace(); "
                + "                } "
                + "              } catch(e0) {} "
                + "              if (!ws) { "
                + "                try { "
                + "                  if (window.Blockly && window.Blockly.getMainWorkspace) ws = window.Blockly.getMainWorkspace(); "
                + "                } catch(e1) {} "
                + "              } "
                + "              if (!ws && window.Blockly) { "
                + "                ws = Blockly.mainWorkspace || null; "
                + "              } "
                + "              var bid = (fr.blockId && fr.blockId.length) ? fr.blockId : null; "
                + "              if (window.__dbgLastHighlightedId !== bid) { "
                + "                window.__dbgLastHighlightedId = bid; "
                + "                // Blockly Games Maze build doesn't always expose workspace.highlightBlock().\n"
                + "                // Fall back to selecting/unselecting blocks (uses .blocklySelected styling).\n"
                + "                var ok = false; "
                + "                try { "
                + "                  if (ws && typeof ws.highlightBlock === 'function') { ws.highlightBlock(bid); ok = true; } "
                + "                } catch(eh) {} "
                + "                if (!ok && window.Blockly) { "
                + "                  try { if (Blockly.selected && Blockly.selected.unselect) Blockly.selected.unselect(); } catch(e2) {} "
                + "                  var b = null; "
                + "                  try { if (ws && bid && typeof ws.getBlockById === 'function') b = ws.getBlockById(bid); } catch(e3) {} "
                + "                  try { if (!b && bid && Blockly.getBlockById) b = Blockly.getBlockById(bid); } catch(e4) {} "
                + "                  try { if (!b && ws && bid && ws.getBlockById) b = ws.getBlockById(bid); } catch(e5) {} "
                + "                  try { "
                + "                    if (!b && ws && bid && typeof ws.getAllBlocks === 'function') { "
                + "                      var all = ws.getAllBlocks(false) || []; "
                + "                      for (var ai=0; ai<all.length; ai++) { "
                + "                        if (all[ai] && all[ai].id === bid) { b = all[ai]; break; } "
                + "                      } "
                + "                    } "
                + "                  } catch(e7) {} "
                + "                  try { if (b && typeof b.select === 'function') { b.select(); ok = true; } } catch(e6) {} "
                + "                } "
                + "                if (!ok) { "
                + "                  // Final fallback: manipulate SVG DOM (Blockly blocks have data-id=blockId).\n"
                + "                  try { "
                + "                    if (window.__dbgLastHighlightedEl) { "
                + "                      window.__dbgLastHighlightedEl.classList.remove('blocklySelected'); "
                + "                      window.__dbgLastHighlightedEl = null; "
                + "                    } "
                + "                    if (bid) { "
                + "                      // Avoid querySelector with raw ids (some MoMoT-loaded ids contain characters that break selectors).\n"
                + "                      var el = null; "
                + "                      try { "
                + "                        var nodes = document.querySelectorAll('[data-id]'); "
                + "                        for (var ni=0; ni<nodes.length; ni++) { "
                + "                          var cand = nodes[ni]; "
                + "                          if (!cand || !cand.getAttribute) continue; "
                + "                          if (cand.getAttribute('data-id') === bid) { el = cand; break; } "
                + "                        } "
                + "                      } catch(eq) {} "
                + "                      if (el && el.classList) { el.classList.add('blocklySelected'); window.__dbgLastHighlightedEl = el; ok = true; } "
                + "                    } "
                + "                  } catch(ed) {} "
                + "                } "
                + "                if (!ok) { "
                + "                  try { "
                + "                    if (window.javaBridge && window.javaBridge.logJS) { "
                + "                      window.javaBridge.logJS('__dbgHighlight FAILED bid=' + bid + ' hasBlockly=' + (!!window.Blockly) "
                + "                        + ' hasWS=' + (!!ws) "
                + "                        + ' ws.highlightBlock=' + (ws && typeof ws.highlightBlock) "
                + "                        + ' ws.getBlockById=' + (ws && typeof ws.getBlockById) "
                + "                        + ' ws.getAllBlocks=' + (ws && typeof ws.getAllBlocks) "
                + "                        + ' Blockly.getBlockById=' + (window.Blockly && typeof Blockly.getBlockById)); "
                + "                    } "
                + "                  } catch(el) {} "
                + "                } "
                + "              } "
                + "            } catch(e) {} "
                + "            __dbgSetPegman(fr.q, fr.s, fr.t); "
                + "            var pauseBtn = document.getElementById('debugPauseResumeButton'); "
                + "            var stepBtn = document.getElementById('debugStepButton'); "
                + "            var skipBtn = document.getElementById('debugSkipEndButton'); "
                + "            var terminal = !!fr.result && fr.result !== 'RUNNING'; "
                + "            var terminalEditable = terminal && !!fr.dirty; "
                + "            if (pauseBtn) pauseBtn.textContent = fr.paused ? 'Resume' : 'Pause'; "
                + "            if (pauseBtn && terminal) pauseBtn.textContent = 'Resume'; "
                + "            if (stepBtn) { "
                + "              stepBtn.disabled = terminal && !terminalEditable; "
                + "              stepBtn.title = (terminal && terminalEditable) ? ('Program changed after ' + fr.result + ' — realign & step') : (terminal ? ('Debugger finished: ' + fr.result) : 'Execute one step'); "
                + "            } "
                + "            if (skipBtn) { "
                + "              skipBtn.disabled = terminal && !terminalEditable; "
                + "              skipBtn.title = (terminal && terminalEditable) ? ('Program changed after ' + fr.result + ' — realign & jump') : (terminal ? ('Debugger finished: ' + fr.result) : 'Jump to final outcome'); "
                + "            } "
                + "            if (fr.paused && window.__dbgTimer) { clearInterval(window.__dbgTimer); window.__dbgTimer = null; } "
                + "            if (terminal && !terminalEditable) { "
                + "              window.__dbgActive = false; "
                + "              if (window.__dbgTimer) { clearInterval(window.__dbgTimer); window.__dbgTimer = null; } "
                + "            } "
                + "          } catch(e) {} "
                + "        } "
                + "        function __dbgStart() { "
                + "          try { "
                + "            try { "
                + "              // If a MoMoT load just appended an alignment note, start debugging with a clean log\n"
                + "              // but keep that note at the top.\n"
                + "              var keepNote = window.__dbgPreserveMomotAlignNote ? (window.__momotLastAlignNote || '') : ''; "
                + "              if (window.__execLogClear) window.__execLogClear(); "
                + "              if (keepNote && keepNote.length && window.__execLogAppend) window.__execLogAppend(keepNote); "
                + "              window.__dbgPreserveMomotAlignNote = false; "
                + "            } catch(e) {} "
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
                + "            try { "
                + "              // If debugging starts from a mid-trace aligned index, render the full log prefix.\n"
                + "              if (fr && window.__execLogAppend) { "
                + "                // If we just loaded a MoMoT model and aligned to a last-common cell,\n"
                + "                // prefer showing the log starting from that alignment point.\n"
                + "                var arr = (window.__dbgPreserveMomotAlignNote && fr.logFromAlign && fr.logFromAlign.length) ? fr.logFromAlign : fr.logPrefix; "
                + "                if (arr && arr.length) window.__execLogAppend(arr); "
                + "                if (typeof fr.index === 'number') window.__dbgLastLoggedIndex = fr.index; "
                + "              } "
                + "            } catch(eLP) {} "
                + "            try { if (fr && fr.note && fr.note.length && window.__execLogAppend) { if (!window.__dbgLastNote || window.__dbgLastNote !== fr.note) { window.__dbgLastNote = fr.note; window.__execLogAppend(fr.note); } } } catch(e) {} "
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
                + "            try { if (fr && fr.note && fr.note.length && window.__execLogAppend) { if (!window.__dbgLastNote || window.__dbgLastNote !== fr.note) { window.__dbgLastNote = fr.note; window.__execLogAppend(fr.note); } } } catch(e) {} "
                + "            try { if (fr && typeof fr.index === 'number' && window.__dbgLastLoggedIndex !== fr.index) { window.__dbgLastLoggedIndex = fr.index; if (fr.logLine && window.__execLogAppend) window.__execLogAppend(fr.logLine); } } catch(e) {} "
                + "            if (!fr.paused && !window.__dbgTimer) { "
                + "              window.__dbgTimer = setInterval(function() { "
                + "                try { "
                + "                  var gen2 = (typeof window.__javaPageGen === 'number') ? window.__javaPageGen : 0; "
                + "                  var fr2 = JSON.parse((window.javaBridge.debugTickWithGen ? window.javaBridge.debugTickWithGen(gen2) : window.javaBridge.debugTick())); "
                + "                  try { if (window.javaBridge) window.javaBridge.logJS('__dbgTick frame q=' + fr2.q + ' s=' + fr2.s + ' t=' + fr2.t + ' paused=' + fr2.paused + ' result=' + fr2.result); } catch(e) {} "
                + "                  __dbgRenderFrame(fr2); "
                + "                  try { if (fr2 && fr2.note && fr2.note.length && window.__execLogAppend) { if (!window.__dbgLastNote || window.__dbgLastNote !== fr2.note) { window.__dbgLastNote = fr2.note; window.__execLogAppend(fr2.note); } } } catch(e) {} "
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
                + "            try { if (fr && fr.note && fr.note.length && window.__execLogAppend) { if (!window.__dbgLastNote || window.__dbgLastNote !== fr.note) { window.__dbgLastNote = fr.note; window.__execLogAppend(fr.note); } } } catch(e) {} "
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
                + "            try { "
                + "              var ws = null; "
                + "              if (window.Blockly) { ws = (Blockly.getMainWorkspace && Blockly.getMainWorkspace()) || Blockly.mainWorkspace || null; } "
                + "              if (ws && typeof ws.highlightBlock === 'function') { ws.highlightBlock(null); } "
                + "              try { if (window.Blockly && Blockly.selected && Blockly.selected.unselect) Blockly.selected.unselect(); } catch(e3) {} "
                + "              try { if (window.__dbgLastHighlightedEl) { window.__dbgLastHighlightedEl.classList.remove('blocklySelected'); window.__dbgLastHighlightedEl = null; } } catch(e4) {} "
                + "              window.__dbgLastHighlightedId = null; "
                + "            } catch(e0) {} "
                + "            try { if (window.__execLogClear) window.__execLogClear(); } catch(e) {} "
                + "            window.__dbgLastLoggedIndex = -1; "
                + "            window.__dbgLastNote = null; "
                + "            window.__dbgSessionStarted = false; "
                + "            window.__dbgActive = false; "
                + "            window.__dbgStepInFlight = false; "
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
                + "        var directManipBtn = __dbgMkBtn('directManipulationButton', 'Direct Manipulation', 'Teleport pegman to an empty/goal cell (paused or before run)'); "
                + "        // Keep same look as other debug buttons (className='primary'); only add spacing.\n"
                + "        directManipBtn.style.marginLeft = '8px'; "
                + "        debugPauseResumeBtn.addEventListener('click', function() { __dbgTogglePause(); }); "
                + "        debugStopBtn.addEventListener('click', function() { __dbgStop(); }); "
                + "        debugStepBtn.addEventListener('click', function() { __dbgStep(); }); "
                + "        // Some in-place operations (e.g. MoMoT block injection) can recreate DOM nodes.\n"
                + "        // Bind a capture-phase delegated handler once so debug buttons keep working.\n"
                + "        if (!window.__dbgDelegationBound) { "
                + "          window.__dbgDelegationBound = true; "
                + "          document.addEventListener('click', function(ev) { "
                + "            try { "
                + "              var tgt = ev && ev.target ? ev.target : null; "
                + "              if (!tgt || !tgt.id) return; "
                + "              if (tgt.id === 'debugPauseResumeButton') { __dbgTogglePause(); ev.preventDefault(); ev.stopPropagation(); } "
                + "              else if (tgt.id === 'debugStopButton') { __dbgStop(); ev.preventDefault(); ev.stopPropagation(); } "
                + "              else if (tgt.id === 'debugStepButton') { __dbgStep(); ev.preventDefault(); ev.stopPropagation(); } "
                + "              else if (tgt.id === 'debugSkipEndButton') { "
                + "                // Keep existing button handler; this is here only to ensure the listener exists.\n"
                + "              } "
                + "            } catch(e) {} "
                + "          }, true); "
                + "        } "
                + "        debugSkipBtn.addEventListener('click', function() { "
                + "          try { "
                + "            if (!window.__dbgSessionStarted) __dbgStart(); "
                + "            __dbgSync(); "
                + "            var gen = (typeof window.__javaPageGen === 'number') ? window.__javaPageGen : 0; "
                + "            var fr = JSON.parse((window.javaBridge.debugSkipToEndWithGen ? window.javaBridge.debugSkipToEndWithGen(gen) : window.javaBridge.debugSkipToEnd())); "
                + "            __dbgRenderFrame(fr); "
                + "            try { if (fr && fr.note && fr.note.length && window.__execLogAppend) { if (!window.__dbgLastNote || window.__dbgLastNote !== fr.note) { window.__dbgLastNote = fr.note; window.__execLogAppend(fr.note); } } } catch(e) {} "
                + "            try { if (fr && typeof fr.index === 'number' && window.__dbgLastLoggedIndex !== fr.index) { window.__dbgLastLoggedIndex = fr.index; if (fr.logLine && window.__execLogAppend) window.__execLogAppend(fr.logLine); } } catch(e) {} "
                + "            if (window.__dbgTimer) { clearInterval(window.__dbgTimer); window.__dbgTimer = null; } "
                + "          } catch(e) {} "
                + "        }); "
                + "        window.__dmActive = false; "
                + "        function __dmCanEnable() { "
                + "          try { "
                + "            var paused = (!window.__dbgTimer); "
                + "            var inDebug = !!window.__dbgSessionStarted; "
                + "            var beforeRun = !window.__blockyRunStarted && !inDebug; "
                + "            return beforeRun || (inDebug && paused); "
                + "          } catch(e) { return false; } "
                + "        } "
                + "        function __dmUpdateButton() { "
                + "          try { "
                + "            if (!directManipBtn) return; "
                + "            var en = __dmCanEnable(); "
                + "            directManipBtn.disabled = !en; "
                + "            directManipBtn.style.opacity = en ? '1.0' : '0.55'; "
                + "            directManipBtn.textContent = window.__dmActive ? 'Direct Manipulation (click a cell)' : 'Direct Manipulation'; "
                + "          } catch(e) {} "
                + "        } "
                + "        function __dmStop() { "
                + "          try { window.__dmActive = false; __dmUpdateButton(); } catch(e) {} "
                + "          try { if (window.__dmClickHandler && window.__dmClickTarget) window.__dmClickTarget.removeEventListener('click', window.__dmClickHandler, true); } catch(e) {} "
                + "          try { window.__dmClickHandler = null; window.__dmClickTarget = null; } catch(e) {} "
                + "          try { document.body.style.cursor = ''; } catch(e) {} "
                + "        } "
                + "        function __dmStart() { "
                + "          try { "
                + "            if (!__dmCanEnable()) { __dmStop(); return; } "
                + "            var svg = document.getElementById('svgMaze'); "
                + "            if (!svg) return; "
                + "            window.__dmActive = true; __dmUpdateButton(); "
                + "            try { document.body.style.cursor = 'crosshair'; } catch(e) {} "
                + "            window.__dmClickTarget = svg; "
                + "            window.__dmClickHandler = function(ev) { "
                + "              try { "
                + "                if (!window.__dmActive) return; "
                + "                if (!ev) return; "
                + "                var rect = svg.getBoundingClientRect(); "
                + "                var cx = ev.clientX - rect.left; "
                + "                var cy = ev.clientY - rect.top; "
                + "                var grid = window.X; "
                + "                if (!grid || !grid.length || !grid[0] || !grid[0].length) return; "
                + "                var height = grid.length; "
                + "                var width  = grid[0].length; "
                + "                var col = Math.floor((cx / rect.width)  * width); "
                + "                var row = Math.floor((cy / rect.height) * height); "
                + "                try { "
                + "                  var bridge = window.javaBridge || (window.parent && window.parent.javaBridge); "
                + "                  if (bridge) bridge.logJS('[DM] Click: client(' + ev.clientX + ',' + ev.clientY + ') rectTop=' + Math.round(rect.top) + ' -> local(' + Math.round(cx) + ',' + Math.round(cy) + ') -> cell(' + col + ',' + row + ') size(' + Math.round(rect.width) + 'x' + Math.round(rect.height) + ')'); "
                + "                } catch(eLog) {} "
                + "                if (col < 0 || row < 0 || col >= width || row >= height) return; "
                + "                var v = grid[row][col]; "
                + "                if (!(v === 1 || v === 3)) { "
                + "                  try { if (window.javaBridge) window.javaBridge.logJS('[DM] Reject cell row=' + row + ' col=' + col + ' v=' + v); } catch(e2) {} "
                + "                  return; "
                + "                } "
                + "                var t = (typeof window.T === 'number') ? window.T : ((typeof window.__stableStartT === 'number') ? window.__stableStartT : 1); "
                + "                try { if (typeof __dbgSetPegman === 'function') __dbgSetPegman(col, row, t); } catch(e3) {} "
                + "                try { "
                + "                  var bridge = window.javaBridge || (window.parent && window.parent.javaBridge); "
                + "                  if (bridge && bridge.teleportPegman) bridge.teleportPegman(col, row, t); "
                + "                } catch(e4) {} "
                + "                try { if (window.__momotShowAndRefresh) window.__momotShowAndRefresh(); } catch(e4b) {} "
                + "                __dmStop(); "
                + "              } catch(e5) { __dmStop(); } "
                + "            }; "
                + "            svg.addEventListener('click', window.__dmClickHandler, true); "
                + "          } catch(e) {} "
                + "        } "
                + "        directManipBtn.addEventListener('click', function(){ "
                + "          try { "
                + "            if (window.__dmActive) __dmStop(); "
                + "            else { "
                + "              window.__preDmQ = window.Q; "
                + "              window.__preDmS = window.S; "
                + "              __dmStart(); "
                + "            } "
                + "          } catch(e) {} "
                + "        }); "
                + "        // Keep enablement in sync with debug state.\n"
                + "        try { setInterval(__dmUpdateButton, 200); } catch(e) {} "
                + "        __dmUpdateButton(); "
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

        /** Receives a base64 dataUrl PNG generated in the WebView. */
        public void receivePngDataUrl(String dataUrl) {
            snapshotService.receivePngDataUrl(dataUrl);
        }

        public String getSolutionPath(String xmiPath) {
            if (xmiPath == null || xmiPath.isEmpty()) return "[]";
            try {
                File file = new File(xmiPath.trim());
                if (!file.exists()) return "[]";

                ResourceSet resSet = new ResourceSetImpl();
                URI uri = URI.createFileURI(file.getAbsolutePath());
                Resource res = resSet.createResource(uri);
                res.load(null);

                if (res.getContents().isEmpty()) return "[]";
                Object root = res.getContents().get(0);
                Level level = null;
                if (root instanceof Game) {
                    Game game = (Game) root;
                    if (!game.getLevels().isEmpty()) level = game.getLevels().get(0);
                } else if (root instanceof Level) {
                    level = (Level) root;
                }

                if (level == null || level.getMap() == null) return "[]";

                Cell startCell = engine.getStartCell(level.getMap());
                Direction startDir = SimUtils.determineStartOrientation(level, startCell);

                DebuggingService.DebugTraceResult result = DebuggingService.computeTraceFromState(level,
                    startCell != null ? startCell.getX() : 0,
                    startCell != null ? startCell.getY() : 0,
                    startDir);

                int[][] pts = result.statePositions;
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < pts.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append("[").append(pts[i][0]).append(",").append(pts[i][1]).append("]");
                }
                sb.append("]");
                return sb.toString();
            } catch (Exception e) {
                System.err.println("[JSBridge] getSolutionPath failed: " + e.getMessage());
                return "[]";
            }
        }

        /** Called by WebView after loaded model state is injected and stable. */
        public void injectComplete() {
            awaitingInjectComplete = false;
            suppressSync = false;
            System.out.println("[JSBridge] Injection complete; sync re-enabled.");
            try {
                Platform.runLater(() -> {
                    try {
                        webView.getEngine().executeScript("try { if (window.__lvlLoadingHide) window.__lvlLoadingHide(); } catch(e) {}");
                    } catch (Exception ignored) {
                    }
                });
            } catch (Exception ignored) {
            }
            if (pendingShowMomotPanel) {
                pendingShowMomotPanel = false;
                try {
                    webView.getEngine().executeScript("try { if (window.__momotShowAndRefresh) window.__momotShowAndRefresh(); } catch(e) {}");
                } catch (Exception ignored) {
                }
            }
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

        /**
         * Direct manipulation teleport from WebView.
         * q,s are cell coordinates; t is direction code (0=N,1=E,2=S,3=W).
         */
        public void teleportPegman(int q, int s, int t) {
            try {
                System.out.println("[JSBridge] teleportPegman q=" + q + " s=" + s + " t=" + t);
                engine.teleportPegman(q, s, t);
                // Show MoMoT panel after a DM selection (do NOT auto-run).
                try {
                    showMomotPanelOnly();
                } catch (Exception ignored) {
                }
            } catch (Exception e) {
                System.err.println("[JSBridge] teleportPegman failed: " + e.getMessage());
                e.printStackTrace();
            }
        }

        /** Returns MoMoT solutions as a JSON array for WebView rendering. */
        public String listMomotSolutions() {
            try {
                List<MomotResultsService.SolutionEntry> sols;
                String filterDir = momotCurrentOutputDir;
                if (filterDir != null && !filterDir.trim().isEmpty()) {
                    File outDir = new File(filterDir.trim());
                    if (outDir.exists() && outDir.isDirectory()) {
                        sols = MomotResultsService.loadFromOutputDir(outDir);
                    } else {
                        // Current run directory not yet on disk or invalid; show nothing yet.
                        sols = Collections.emptyList();
                    }
                } else {
                    // No run triggered yet; show nothing.
                    sols = Collections.emptyList();
                }
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (int i = 0; i < sols.size(); i++) {
                    MomotResultsService.SolutionEntry e = sols.get(i);
                    if (i > 0) sb.append(",");
                    sb.append("{");
                    sb.append("\"outputDir\":\"").append(escapeJsonString(e.outputDir)).append("\",");
                    sb.append("\"modelPath\":\"").append(escapeJsonString(e.modelPath)).append("\",");
                    sb.append("\"objectiveLine\":\"").append(escapeJsonString(e.objectiveLine)).append("\",");
                    sb.append("\"summary\":\"").append(escapeJsonString(e.summary)).append("\"");
                    sb.append("}");
                }
                sb.append("]");
                return sb.toString();
            } catch (Exception ex) {
                return "[]";
            }
        }

        /** Loads a MoMoT-produced solution model XMI and applies it to the WebView. */
        public void loadMomotSolution(String xmiPath) {
            if (xmiPath == null || xmiPath.trim().isEmpty()) return;
            System.out.println("[JSBridge] loadMomotSolution path=" + xmiPath);
            // Keep the MoMoT panel open (no page reload).
            pendingShowMomotPanel = true;
            Platform.runLater(() -> loadMomotSolutionInPlace(xmiPath.trim()));
        }

        public void runMomotWithParams(int seed, int populationSize, int maxEvaluations, int nrRuns, int solutionLength) {
            System.out.println("[JSBridge] runMomotWithParams seed=" + seed + " pop=" + populationSize + " eval=" + maxEvaluations + " runs=" + nrRuns + " solLen=" + solutionLength);
            Platform.runLater(() -> {
                startMomotWithParams(seed, populationSize, maxEvaluations, nrRuns, solutionLength);
            });
        }

        public void stopMomotRun() {
            System.out.println("[JSBridge] stopMomotRun");
            MomotRunService.stopCurrentRun();
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
            
            // Stop MoMoT if level changed
            MomotRunService.stopCurrentRun();

            engine.syncLevelMeta(metaJson);
            try {
                Platform.runLater(() -> {
                    try {
                        webView.getEngine().executeScript("try { if (window.__lvlLoadingHide) window.__lvlLoadingHide(); } catch(e) {}");
                    } catch (Exception ignored) {
                    }
                });
            } catch (Exception ignored) {
            }
        }

        public void syncModel(String xml) {
            if (suppressSync) {
                // log this occasionally or just once to verify suppression works
                return;
            }
            if (xml == null) return;
            // Guard: WebView can transiently produce an empty <xml/> snapshot during in-place injections
            // or while Blockly is re-rendering. Ignore these so we don't wipe the Java-side solution.
            if (xml.indexOf("<block") < 0) {
                System.out.println("[JSBridge] Ignoring empty workspace XML snapshot.");
                return;
            }
            try {
                System.out.println("[JSBridge] Syncing workspace XML (len=" + xml.length() + ")");
                List<Map<String, Object>> data = parseBlocklyXml(xml);
                engine.rebuildProgram(data);
                System.out.println("[JSBridge] Sync complete. Top-level blocks: " + (data != null ? data.size() : 0));
                
                // If a debug session is active, trigger an immediate re-simulation and redraw 
                // so the orange predicted path updates as the user edits blocks.
                try {
                    webView.getEngine().executeScript("if (window.__dbgActive) { " +
                        "  var bridge = window.javaBridge || (window.parent && window.parent.javaBridge); " +
                        "  if (bridge && bridge.debugTick) { " +
                        "    var gen = (typeof window.__javaPageGen === 'number') ? window.__javaPageGen : 0; " +
                        "    var fr = JSON.parse((bridge.debugTickWithGen ? bridge.debugTickWithGen(gen) : bridge.debugTick())); " +
                        "    if (typeof __dbgRenderOverlay === 'function') __dbgRenderOverlay(fr.prefix, fr.pastPrefix, fr.newPreview, fr.common); " +
                        "  } " +
                        "}");
                } catch (Exception ignored) {}
            } catch (Exception e) {
                System.err.println("[JSBridge] Sync Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        /**
         * Force-sync workspace XML even during injection.
         * Used by MoMoT in-place model loading to ensure the Java model + debugger mapping
         * matches the newly injected Blockly blocks (new ids, new statement instances).
         */
        public void syncModelForce(String xml) {
            try {
                if (xml == null || xml.indexOf("<block") < 0) {
                    System.out.println("[JSBridge] Force-sync ignored empty workspace XML snapshot.");
                    return;
                }
                System.out.println("[JSBridge] Force-syncing workspace XML:\n" + xml);
                List<Map<String, Object>> data = parseBlocklyXml(xml);
                engine.rebuildProgram(data);
                System.out.println("[JSBridge] Force-sync complete. Top-level blocks: " + (data != null ? data.size() : 0));
            } catch (Exception e) {
                System.err.println("[JSBridge] Force-sync Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showMomotPanelOnly() {
        try {
            momotCurrentOutputDir = null; // Clear previous run results when panel is shown for a new location
            
            // Infer default solution length
            int defSolLen = 10;
            try {
                String input = MomotRunService.defaultDirectManipulationSpec().inputXmi;
                File f = new File(input);
                if (!f.isAbsolute()) {
                    // Try to resolve it as MomotRunService does
                    if (!f.exists()) f = new File("..", input);
                    if (!f.exists()) f = new File("..", ".." + File.separator + input);
                }
                if (f.exists()) {
                    Class<?> metrics = Class.forName("blocky_momot.BlockyProgramMetrics");
                    Object v = metrics.getMethod("inferSolutionLength", String.class).invoke(null, f.getAbsoluteFile().getPath());
                    if (v instanceof Number) defSolLen = Math.max(1, ((Number) v).intValue() * 2);
                }
            } catch (Exception ignored) {}

            pendingShowMomotPanel = true;
            final int finalDefSolLen = defSolLen;
            webView.getEngine().executeScript(
                "try { " +
                "  if (window.__momotShowAndRefresh) window.__momotShowAndRefresh();" +
                "  if (window.__momotSetStatus) window.__momotSetStatus('MoMoT panel ready. Set parameters and click Run.');" +
                "  var slInp = document.getElementById('__momotInpSolLen');" +
                "  if (slInp) slInp.value = '" + finalDefSolLen + "';" +
                "} catch(e) {}"
            );
        } catch (Exception ignored) {
        }
    }

    private void startMomotWithParams(int seed, int populationSize, int maxEvaluations, int nrRuns, int solutionLength) {
        try {
            pendingShowMomotPanel = true;
            webView.getEngine().executeScript(
                "try { " +
                "  if (window.__momotShowAndRefresh) window.__momotShowAndRefresh();" +
                "  if (window.__momotLogClear) window.__momotLogClear();" +
                "  if (window.__momotSetStatus) window.__momotSetStatus('Running MoMoT...');" +
                "} catch(e) {}"
            );
        } catch (Exception ignored) {
        }

        // Set parameters as system properties so MomotRunService can read them.
        System.setProperty("blocky.seed", String.valueOf(seed));
        System.setProperty("blocky.populationSize", String.valueOf(populationSize));
        System.setProperty("blocky.maxEvaluations", String.valueOf(maxEvaluations));
        System.setProperty("blocky.nrRuns", String.valueOf(nrRuns));
        System.setProperty("blocky.solutionLength", String.valueOf(solutionLength));

        // Determine correct Henshin file based on level constraints
        String henshin = "statement_insertions_henshin_text.henshin";
        Level lvl = engine.getCurrentLevel();
        if (lvl != null) {
            boolean loops = lvl.isAllowLoops();
            boolean conds = lvl.isAllowConditionals();
            boolean ifElse = lvl.isAllowIfElse();
            System.setProperty("blocky.allowIfElse", String.valueOf(ifElse));
            
            if (!loops && !conds) {
                henshin = "statement_insertions_atomic_only.henshin";
            } else if (!conds) {
                henshin = "statement_insertions_no_conds.henshin";
            } else if (!ifElse) {
                henshin = "statement_insertions_no_else.henshin";
            } else {
                henshin = "statement_insertions_henshin_text.henshin";
            }
        }
        System.setProperty("blocky.henshin", "../blocky_model/transformations/" + henshin);

        MomotRunService.RunSpec spec = MomotRunService.defaultDirectManipulationSpec();
        momotCurrentOutputDir = spec.outputBase;
        MomotRunService.runAsync(spec, (txt) -> {
            if (txt == null) return;
            final String safe = txt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
            Platform.runLater(() -> {
                try {
                    webView.getEngine().executeScript(
                        "try { if (window.__momotLogAppend) window.__momotLogAppend(\"" + safe + "\"); } catch(e) {}"
                    );
                } catch (Exception ignored2) {
                }
            });
        }, () -> {
            try {
                webView.getEngine().executeScript(
                    "try { " +
                    "  if (window.__momotSetStatus) window.__momotSetStatus('MoMoT finished. Refreshing solutions…');" +
                    "  if (window.__momotShowAndRefresh) window.__momotShowAndRefresh();" +
                    "  if (window.__momotSetStatus) window.__momotSetStatus('Done.');" +
                    "} catch(e) {}"
                );
            } catch (Exception ignored3) {
            }
        }, (finalOutDir) -> {
            if (finalOutDir != null && !finalOutDir.trim().isEmpty()) {
                momotCurrentOutputDir = finalOutDir.trim();
            }
        });
    }

    private void loadXmiFromPathImpl(String xmiPath) {
        File xmiFile = new File(xmiPath);
        if (!xmiFile.exists()) {
            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Solution model not found: " + xmiFile.getPath()).showAndWait());
            return;
        }
        try {
            engine.loadFromFile(xmiFile);
        } catch (IOException e) {
            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Failed to load solution model: " + e.getMessage()).showAndWait());
            return;
        } catch (IllegalArgumentException e) {
            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Invalid solution model: " + e.getMessage()).showAndWait());
            return;
        }
        Level level = engine.getCurrentLevel();
        int levelId = level != null ? Math.max(1, Math.min(10, level.getId())) : 1;
        pendingApplyLevel = true;
        suppressSync = true;
        webView.getEngine().load(getMazeBaseUrl() + "?lang=en&level=" + levelId);
    }

    /**
     * Loads a MoMoT-produced solution XMI and applies it to the CURRENT WebView page (no reload).
     * This keeps the MoMoT panel and execution log intact while updating blocks as-if the user edited them.
     */
    private void loadMomotSolutionInPlace(String xmiPath) {
        File xmiFile = new File(xmiPath);
        if (!xmiFile.exists()) {
            try {
                String msg = escapeForJsStringLiteral("Solution model not found: " + xmiFile.getPath());
                webView.getEngine().executeScript(
                    "try { if (window.__momotSetStatus) window.__momotSetStatus(\"" + msg + "\"); } catch(e) {}"
                );
            } catch (Exception ignored) {}
            return;
        }

        // Arm DM comparison overlays so THIS model load computes baseline vs solution diff.
        try { engine.armDirectManipulationComparison(); } catch (Exception ignored) {}

        try {
            engine.loadFromFile(xmiFile);
        } catch (IOException e) {
            try {
                String msg = escapeForJsStringLiteral("Failed to load solution model: " + e.getMessage());
                webView.getEngine().executeScript(
                    "try { if (window.__momotSetStatus) window.__momotSetStatus(\"" + msg + "\"); } catch(e) {}"
                );
            } catch (Exception ignored) {}
            return;
        } catch (IllegalArgumentException e) {
            try {
                String msg = escapeForJsStringLiteral("Invalid solution model: " + e.getMessage());
                webView.getEngine().executeScript(
                    "try { if (window.__momotSetStatus) window.__momotSetStatus(\"" + msg + "\"); } catch(e) {}"
                );
            } catch (Exception ignored) {}
            return;
        }

        Level level = engine.getCurrentLevel();
        if (level == null) return;

        // Build block XML to inject into the existing workspace.
        String xml = engine.solutionToBlocklyXml(level);
        try {
            final String preview = xml == null ? "null" : (xml.length() > 400 ? xml.substring(0, 400) + "…" : xml);
            System.out.println("[BlockyUI] MoMoT inject XML preview: " + preview);
        } catch (Exception ignored) {
        }
        String escapedForJson = escapeForJsStringLiteral(xml);

        // Prepare overlay data for immediate feedback + DM diff (if armed).
        String injectPastNew = ImmediateFeedbackService.buildWindowInjectPathsScript(engine.getPastPath(), engine.getNewPath());
        String injectDm;
        if (engine.hasDirectManipulationComparison()) {
            injectDm = ""
                + "window.__injectDmEnabled = true;"
                + "window.__injectDmBaselinePath = " + ImmediateFeedbackService.toJsonArray(engine.getDmBaselinePath()) + ";"
                + "window.__injectDmSolutionPath = " + ImmediateFeedbackService.toJsonArray(engine.getDmSolutionPath()) + ";"
                + "window.__injectDmCommonLen = " + engine.getDmCommonLen() + ";";
            if (engine.isDmAlignedValid()) {
                injectDm += "window.__injectQ = " + engine.getDmAlignedX() + ";"
                          + "window.__injectS = " + engine.getDmAlignedY() + ";";
            } else {
                injectDm += "window.__injectQ = undefined; window.__injectS = undefined;";
            }
        } else {
            injectDm = ""
                + "window.__injectDmEnabled = false;"
                + "window.__injectDmBaselinePath = [];"
                + "window.__injectDmSolutionPath = [];"
                + "window.__injectDmCommonLen = 0;"
                + "window.__injectQ = undefined; window.__injectS = undefined;";
        }

        suppressSync = true;
        awaitingInjectComplete = true;

        try {
            System.out.println("[BlockyUI] loadMomotSolutionInPlace: preparing injection...");
            
            // First, inject the XML and overlay data as global variables.
            webView.getEngine().executeScript("window.__momotXml = \"" + escapedForJson + "\";");
            webView.getEngine().executeScript("try { " + injectPastNew + " } catch(e) {}");
            webView.getEngine().executeScript("try { " + injectDm + " } catch(e) {}");
            
            System.out.println("[BlockyUI] loadMomotSolutionInPlace: sending execution script...");

            // Now, run the script that applies the XML.
            // Using alert() as a fallback diagnostic since setOnAlert is hooked to System.out.
            Object result = webView.getEngine().executeScript(
                "(function(){\n" +
                "  var bridge = window.javaBridge || (window.parent && window.parent.javaBridge);\n" +
                "  function logJS(m) {\n" +
                "    console.log('MoMoT: ' + m);\n" +
                "    if (bridge && bridge.logJS) bridge.logJS('MoMoT: ' + m);\n" +
                "    else alert('MoMoT: ' + m);\n" +
                "  }\n" +
                "  function finish() { if (bridge && bridge.injectComplete) bridge.injectComplete(); }\n" +
                "  try {\n" +
                "    logJS('In-place script started');\n" +
                "    if (window.__momotSetStatus) window.__momotSetStatus('Applying solution…');\n" +
                "    \n" +
                "    if (window.localStorage) {\n" +
                "      logJS('Clearing maze cache');\n" +
                "      for(var i=1; i<=10; i++) window.localStorage.removeItem('maze'+i);\n" +
                "    }\n" +
                "    if (window.sessionStorage) window.sessionStorage.removeItem('Vp');\n" +
                "\n" +
                "    var attempts = 0, maxAttempts = 50;\n" +
                "    var interval = setInterval(function() {\n" +
                "      attempts++;\n" +
                "      var ws = (window.BlocklyInterface && window.BlocklyInterface.getWorkspace && window.BlocklyInterface.getWorkspace()) ||\n" +
                "               (window.Blockly && (window.Blockly.getMainWorkspace && window.Blockly.getMainWorkspace() || window.Blockly.mainWorkspace));\n" +
                "      var bK = (window.h && window.h.K) || (window.Blockly && window.Blockly.Xml);\n" +
                "      \n" +
                "      if (ws && bK) {\n" +
                "        clearInterval(interval);\n" +
                "        logJS('Workspace found. Applying blocks...');\n" +
                "        try {\n" +
                "          var xmlText = window.__momotXml;\n" +
                "          var ok = false;\n" +
                "          if (window.BlocklyInterface && window.BlocklyInterface.Kv) {\n" +
                "             try { window.BlocklyInterface.Kv(xmlText); ok = true; logJS('Applied via Kv'); } catch(eK) { ok = false; }\n" +
                "          }\n" +
                "          if (!ok) {\n" +
                "            var dom = (bK.textToDom ? bK.textToDom(xmlText) : (bK.$f ? bK.$f(xmlText) : null));\n" +
                "            if (!dom) dom = (new DOMParser()).parseFromString(xmlText, 'text/xml').documentElement;\n" +
                "            if (dom) {\n" +
                "              if (dom.nodeName && dom.nodeName.toLowerCase() !== 'xml') {\n" +
                "                var wrap = document.createElement('xml'); wrap.appendChild(dom); dom = wrap;\n" +
                "              }\n" +
                "              ws.clear();\n" +
                "              if (bK.domToWorkspace) bK.domToWorkspace(dom, ws);\n" +
                "              else if (bK.Eg) bK.Eg(dom, ws);\n" +
                "              ok = true; logJS('Applied via fallback');\n" +
                "            }\n" +
                "          }\n" +
                "          if (!ok) logJS('CRITICAL: No injection method worked');\n" +
                "\n" +
                "                      // Teleport pegman to last common cell if available (DM/MoMoT result alignment)\n" +
                "          if (window.__injectQ !== undefined && window.__injectS !== undefined) {\n" +
                "            logJS('Teleporting pegman to aligned cell (' + window.__injectQ + ',' + window.__injectS + ')');\n" +
                "            window.Q = window.__injectQ;\n" +
                "            window.S = window.__injectS;\n" +
                "            if (typeof window.__modelStartT === 'number') window.T = window.__modelStartT;\n" +
                "            if (typeof Z === 'function') {\n" +
                "              Z(window.Q, window.S, 4 * window.T);\n" +
                "            }\n" +
                "          }\n" +
                "        } catch(e) {\n" +
                "          logJS('Error applying blocks: ' + e);\n" +
                "        }\n" +
                "        finish();\n" +
                "      } else if (attempts > maxAttempts) {\n" +
                "        clearInterval(interval);\n" +
                "        logJS('Timed out waiting for workspace (ws=' + !!ws + ', bK=' + !!bK + ')');\n" +
                "        finish();\n" +
                "      }\n" +
                "    }, 200);\n" +
                "  } catch(e) {\n" +
                "    alert('MoMoT TOP ERROR: ' + e);\n" +
                "    finish();\n" +
                "  }\n" +
                "})();"
            );
            System.out.println("[BlockyUI] loadMomotSolutionInPlace: executeScript returned: " + result);

            // 2) Redraw overlays
            try {
                webView.getEngine().executeScript("(function(){ try { " + ImmediateFeedbackService.buildOverlayRenderJs() + " } catch(e) {} })();");
            } catch (Exception ignored) {}

        } catch (Exception e) {
            System.err.println("[BlockyUI] loadMomotSolutionInPlace failed: " + e.getMessage());
            e.printStackTrace();
            awaitingInjectComplete = false;
            suppressSync = false;
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

    /**
     * Escapes a Java string for inclusion inside a JS double-quoted string literal.
     * Keep this slightly stricter than JSON to avoid parsing edge-cases.
     */
    private static String escapeForJsStringLiteral(String s) {
        if (s == null) return "";
        return escapeJsonString(s)
                .replace("\t", "\\t")
                .replace("\u2028", "\\u2028")
                .replace("\u2029", "\\u2029");
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
                if (block == null) {
                    continue;
                }

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

    /** Loads a model XMI via FileChooser and applies it to the WebView. */
    private void loadModelImpl() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Solution Model (XMI)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XMI Files", "*.xmi"));
        
        // Default directory: blocky_game/ if exists, else current dir.
        File initialDir = new File("blocky_game");
        if (!initialDir.exists()) initialDir = new File(".");
        fileChooser.setInitialDirectory(initialDir);

        Window window = webView != null && webView.getScene() != null ? webView.getScene().getWindow() : null;
        File xmiFile = fileChooser.showOpenDialog(window);

        if (xmiFile == null) {
            // User cancelled; ensure overlay is hidden if it was triggered by a pill click
            webView.getEngine().executeScript("try { if (window.__lvlLoadingHide) window.__lvlLoadingHide(); } catch(e) {}");
            return;
        }

        try {
            engine.loadFromFile(xmiFile);
        } catch (IOException e) {
            webView.getEngine().executeScript("try { if (window.__lvlLoadingHide) window.__lvlLoadingHide(); } catch(e) {}");
            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Failed to load model: " + e.getMessage()).showAndWait());
            return;
        } catch (IllegalArgumentException e) {
            webView.getEngine().executeScript("try { if (window.__lvlLoadingHide) window.__lvlLoadingHide(); } catch(e) {}");
            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Invalid model: " + e.getMessage()).showAndWait());
            return;
        }
        Level level = engine.getCurrentLevel();
        int levelId = level != null ? Math.max(1, Math.min(10, level.getId())) : 1;
        pendingApplyLevel = true;
        suppressSync = true;
        webView.getEngine().load(getMazeBaseUrl() + "?lang=en&level=" + levelId);
    }

    /** Saves the current model XMI via FileChooser. */
    private void saveModelImpl() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Model (XMI)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XMI Files", "*.xmi"));
        
        // Suggest a name like "save.xmi" or use the currently loaded file name if available
        fileChooser.setInitialFileName("save.xmi");

        // Default directory: blocky_game/ if exists, else current dir.
        File initialDir = new File("blocky_game");
        if (!initialDir.exists()) initialDir = new File(".");
        fileChooser.setInitialDirectory(initialDir);

        Window window = webView != null && webView.getScene() != null ? webView.getScene().getWindow() : null;
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            engine.saveToFile(file);
            System.out.println("[BlockyUI] Manually saved model to: " + file.getAbsolutePath());
        }
        
        // Always ensure overlay is hidden after Save dialog closes (might have been triggered by a pill click)
        webView.getEngine().executeScript("try { if (window.__lvlLoadingHide) window.__lvlLoadingHide(); } catch(e) {}");
    }

    /** Path for Model load: load.xmi (blocky_game/ or current dir). */
    private static File getModelXmiFile() {
        File f = new File("blocky_game/save.xmi");
        if (f.exists()) return f;
        f = new File("save.xmi");
        if (f.exists()) return f;
        f = new File("blocky_game/load.xmi");
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

    /**
     * Injects the loaded level state into the WebView: map grid, nd/od, metadata (K, Od, T, Q, S),
     * Blockly workspace XML, and resets pegman. Call with suppressSync already set and clear it after.
     */
    private void applyLevelToWebView(Level level, WebEngine webEngine) {
        if (level == null || level.getMap() == null || webEngine == null) return;
        System.out.println("[BlockyUI] applyLevelToWebView: Level ID=" + level.getId());

        suppressSync = true;
        awaitingInjectComplete = true;

        try {
            int[][] grid = engine.buildGridForWebView(level.getMap());
            Cell startCell = engine.getStartCell(level.getMap());
            Cell dmgCell = engine.getDmgCell(level.getMap());
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
            // Visual goal marker in Maze is driven by window.od.
            // Prefer DMG if present; otherwise show the original GOAL.
            Cell odCell = dmgCell != null ? dmgCell : goalCell;
            if (odCell != null) {
                webEngine.executeScript("window.__injectOd = {x: " + odCell.getX() + ", y: " + odCell.getY() + "};");
            }
            webEngine.executeScript("window.__injectK = " + levelId + ";");
            webEngine.executeScript("window.__injectOdVal = " + (maxBlocks < 0 ? "Infinity" : String.valueOf(maxBlocks)) + ";");
            webEngine.executeScript("window.__modelStartT = " + engine.directionToT(level.getStartOrientation()) + ";");
            // Immediate feedback overlay data (old stored trace vs new simulated trace).
            webEngine.executeScript(ImmediateFeedbackService.buildWindowInjectPathsScript(engine.getPastPath(), engine.getNewPath()));
            // Direct Manipulation (MoMoT results): baseline vs solution diff overlays.
            if (engine.hasDirectManipulationComparison()) {
                webEngine.executeScript("window.__injectDmEnabled = true;");
                webEngine.executeScript("window.__injectDmBaselinePath = " + ImmediateFeedbackService.toJsonArray(engine.getDmBaselinePath()) + ";");
                webEngine.executeScript("window.__injectDmSolutionPath = " + ImmediateFeedbackService.toJsonArray(engine.getDmSolutionPath()) + ";");
                webEngine.executeScript("window.__injectDmCommonLen = " + engine.getDmCommonLen() + ";");
            } else {
                webEngine.executeScript("window.__injectDmEnabled = false;");
                webEngine.executeScript("window.__injectDmBaselinePath = [];");
                webEngine.executeScript("window.__injectDmSolutionPath = [];");
                webEngine.executeScript("window.__injectDmCommonLen = 0;");
            }

            String xml = engine.solutionToBlocklyXml(level);
            String escapedForJson = escapeForJsStringLiteral(xml);
            webEngine.executeScript("window.__loadXml = \"" + escapedForJson + "\";");

            // 2) Poll until maze DOM and Blockly are ready, then apply our data (overwrite maze defaults), redraw, load blocks, reset pegman
            webEngine.executeScript(
                "(function(){ " +
                "var bridge = window.javaBridge || (window.parent && window.parent.javaBridge); " +
                "function logJS(m) { if (bridge && bridge.logJS) bridge.logJS('ApplyLevel: ' + m); console.log('ApplyLevel: ' + m); } " +
                "function finish() { if (bridge && bridge.injectComplete) bridge.injectComplete(); } " +
                "logJS('Injection script started for Level " + levelId + "'); " +
                "window.__dbgDisableAutoRun = true; " +
                "var attempts = 0, maxAttempts = 60, interval = 100; " +
                "var id = setInterval(function() { " +
                "  var ws = (window.BlocklyInterface && window.BlocklyInterface.getWorkspace && window.BlocklyInterface.getWorkspace()) || " +
                "           (window.Blockly && window.Blockly.getMainWorkspace && window.Blockly.getMainWorkspace()); " +
                "  var bK = (window.h && window.h.K) || (window.Blockly && window.Blockly.Xml); " +
                "  if (!document.getElementById('svgMaze') || !ws || !bK) { " +
                "    attempts++; if (attempts >= maxAttempts) { " +
                "       clearInterval(id); " +
                "       var m=[]; if(!document.getElementById('svgMaze'))m.push('svgMaze'); if(!ws)m.push('ws'); if(!bK)m.push('bK'); " +
                "       logJS('Timed out waiting for workspace. Missing: ' + m.join(',')); " +
                "       finish(); " +
                "    } " +
                "    return; " +
                "  } " +
                "  clearInterval(id); " +
                "  logJS('Workspace found. Overwriting maze state...'); " +
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
                "  if (typeof Wd === 'function') { " +
                "    Wd(); " +
                "    try { " +
                "      if (window.od && typeof window.od.x === 'number') { " +
                "        var c = document.getElementById('svgMaze'); " +
                "        var ns = 'http://www.w3.org/2000/svg'; " +
                "        var g = document.createElementNS(ns, 'g'); " +
                "        g.setAttribute('id', 'dmgMarker'); " +
                "        g.setAttribute('transform', 'translate(' + (50 * window.od.x + 25) + ',' + (50 * window.od.y + 25) + ')'); " +
                "        var star = document.createElementNS(ns, 'path'); " +
                "        star.setAttribute('d', 'M 0,-15 L 4.5,-4.5 L 15,-4.5 L 6.5,3 L 10,14 L 0,7 L -10,14 L -6.5,3 L -15,-4.5 L -4.5,-4.5 Z'); " +
                "        star.setAttribute('fill', '#ff0000'); " +
                "        star.setAttribute('stroke', '#ffffff'); " +
                "        star.setAttribute('stroke-width', '2'); " +
                "        g.appendChild(star); " +
                "        var peg = document.getElementById('pegman'); " +
                "        if (peg && peg.parentNode) c.insertBefore(g, peg); else c.appendChild(g); " +
                "      } " +
                "    } catch(e) { logJS('DMG Marker Error: ' + e); } " +
                "    logJS('Map redrawn via Wd()'); " +
                "  } " +
                "  if (!document.getElementById('look') && c) { " +
                "    var ns = 'http://www.w3.org/2000/svg'; " +
                "    var g = document.createElementNS(ns, 'g'); g.id = 'look'; " +
                "    var p1 = document.createElementNS(ns, 'path'); p1.setAttribute('d', 'M 0,-15 a 15 15 0 0 1 15 15'); " +
                "    var p2 = document.createElementNS(ns, 'path'); p2.setAttribute('d', 'M 0,-35 a 35 35 0 0 1 35 35'); " +
                "    var p3 = document.createElementNS(ns, 'path'); p3.setAttribute('d', 'M 0,-55 a 55 55 0 0 1 55 55'); " +
                "    g.appendChild(p1); g.appendChild(p2); g.appendChild(p3); c.appendChild(g); " +
                "  } " +
                "  if (window.__loadXml !== undefined) { " +
                "    var ok = false; " +
                "    if (window.BlocklyInterface && window.BlocklyInterface.Kv) { " +
                "      try { window.BlocklyInterface.Kv(window.__loadXml); ok = true; logJS('Blocks loaded via Kv'); } catch(e) { logJS('Kv error: ' + e); ok = false; } " +
                "    } " +
                "    if (!ok) { " +
                "      try { " +
                "        var dom = (bK.textToDom ? bK.textToDom(window.__loadXml) : (bK.$f ? bK.$f(window.__loadXml) : null)); " +
                "        if (!dom) dom = (new DOMParser()).parseFromString(window.__loadXml, 'text/xml').documentElement; " +
                "        if (dom) { " +
                "          if (dom.nodeName && dom.nodeName.toLowerCase() !== 'xml') { " +
                "            var wrap = document.createElement('xml'); wrap.appendChild(dom); dom = wrap; " +
                "          } " +
                "          ws.clear(); " +
                "          if (bK.domToWorkspace) bK.domToWorkspace(dom, ws); " +
                "          else if (bK.Eg) bK.Eg(dom, ws); " +
                "          ok = true; logJS('Blocks loaded via fallback'); " +
                "        } " +
                "      } catch(e) { logJS('Fallback error: ' + e); ok = false; } " +
                "      if (!ok) logJS('CRITICAL: All block loading methods failed'); " +
                "    } " +
                "    try { delete window.__loadXml; } catch(e7) {} " +
                "  } " +
                "  try { window.__forcedT = undefined; } catch(e) {} " +
                "  if (typeof $d === 'function' && document.getElementById('finish')) { try { $d(false); } catch(e) { logJS('$d error: ' + e); } } " +
                "  try { "
                + "    if (window.__modelStartT !== undefined) { "
                + "      window.T = window.__modelStartT; "
                + "      if (typeof Z === 'function') { Z(window.Q, window.S, 4 * window.T); logJS('Pegman reset to T=' + window.T); } "
                + "    } "
                + "  } catch(e) { logJS('set modelStartT error: ' + e); } " +
                "  try { if (typeof window.__ifRender === 'function') window.__ifRender(); } catch(eR) {} " +
                "  finish(); " +
                "}, interval); " +
                "})();"
            );
        } catch (Exception e) {
            System.err.println("[BlockyUI] applyLevelToWebView failed: " + e.getMessage());
            e.printStackTrace();
            awaitingInjectComplete = false;
        } finally {
            // keep suppressSync until injectComplete() callback
            if (!awaitingInjectComplete) suppressSync = false;
        }
    }
}