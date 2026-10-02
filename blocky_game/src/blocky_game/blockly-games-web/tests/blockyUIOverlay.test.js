const { describe, it, beforeEach, afterEach } = require('node:test');
const assert = require('node:assert');
const { JSDOM } = require('jsdom');
const path = require('node:path');
const fs = require('node:fs');

describe('blockyUIOverlay.js Test Suite', () => {
    let dom;
    let window;

    beforeEach(() => {
        dom = new JSDOM(`<!DOCTYPE html>
<html>
<body>
  <div id="blockly">
    <button id="runButton">Run Program</button>
  </div>
  <svg id="svgMaze"></svg>
</body>
</html>`, {
            url: 'http://localhost/',
            runScripts: 'dangerously'
        });
        window = dom.window;
        global.window = window;
        global.document = window.document;

        // Mock window.X grid
        window.X = [[2, 1, 3]];
        window.Q = 0; window.S = 0; window.T = 1;

        const overlayScript = fs.readFileSync(path.join(__dirname, '../common/blockyUIOverlay.js'), 'utf8');
        window.eval(overlayScript);
    });

    afterEach(() => {
        if (dom && dom.window) {
            dom.window.close();
        }
    });

    it('injects overlays and debugger buttons into DOM', async () => {
        await new Promise(r => setTimeout(r, 150));
        const loadingOverlay = window.document.getElementById('__lvlLoadingOverlay');
        assert.ok(loadingOverlay, '#__lvlLoadingOverlay should exist');

        const execLogPanel = window.document.getElementById('__execLogPanel');
        assert.ok(execLogPanel, '#__execLogPanel should exist');

        const momotPanel = window.document.getElementById('__momotPanel');
        assert.ok(momotPanel, '#__momotPanel should exist');

        const pauseBtn = window.document.getElementById('debugPauseResumeButton');
        const stepBtn = window.document.getElementById('debugStepButton');
        const stopBtn = window.document.getElementById('debugStopButton');
        const skipBtn = window.document.getElementById('debugSkipEndButton');
        const dmBtn = window.document.getElementById('directManipulationButton');

        assert.ok(pauseBtn, '#debugPauseResumeButton should exist');
        assert.ok(stepBtn, '#debugStepButton should exist');
        assert.ok(stopBtn, '#debugStopButton should exist');
        assert.ok(skipBtn, '#debugSkipEndButton should exist');
        assert.ok(dmBtn, '#directManipulationButton should exist');
    });

    it('execution log append and clear works properly', async () => {
        await new Promise(r => setTimeout(r, 150));
        assert.ok(typeof window.__execLogAppend === 'function');
        window.__execLogAppend(['Line 1', 'Line 2']);

        const body = window.document.getElementById('__execLogBody');
        assert.ok(body.textContent.includes('Line 1'));
        assert.ok(body.textContent.includes('Line 2'));

        window.__execLogClear();
        assert.strictEqual(body.textContent, '');
    });

    it('handles panel drag and resize mousedown events', async () => {
        await new Promise(r => setTimeout(r, 150));
        const header = window.document.getElementById('__execLogHeader');
        assert.ok(header);

        const mouseDownEv = new window.MouseEvent('mousedown', { clientX: 100, clientY: 100, button: 0 });
        header.dispatchEvent(mouseDownEv);

        const mouseMoveEv = new window.MouseEvent('mousemove', { clientX: 150, clientY: 150 });
        window.document.dispatchEvent(mouseMoveEv);

        const mouseUpEv = new window.MouseEvent('mouseup', {});
        window.document.dispatchEvent(mouseUpEv);
    });
});
