const { describe, it, beforeEach, afterEach } = require('node:test');
const assert = require('node:assert');
const { JSDOM } = require('jsdom');
const path = require('node:path');
const fs = require('node:fs');

describe('webBridge.js Test Suite', () => {
    let dom;
    let window;

    beforeEach(() => {
        dom = new JSDOM('<!DOCTYPE html><html><body><div id="blockly"></div><svg id="svgMaze"></svg></body></html>', {
            url: 'http://localhost/',
            runScripts: 'dangerously',
            resources: 'usable'
        });
        window = dom.window;
        global.window = window;
        global.document = window.document;
        global.localStorage = window.localStorage;

        // Mock fetch
        global.fetch = async (url, options = {}) => {
            if (url.includes('/api/session/new')) {
                return {
                    json: async () => ({ sessionId: 'test-session-123' })
                };
            }
            if (url.includes('/api/session/state')) {
                return {
                    json: async () => ({
                        status: 'ok',
                        grid: [[2, 1, 3]],
                        startPos: { x: 0, y: 0 },
                        levelId: 1,
                        maxBlocks: 10,
                        newPath: [[0, 0], [1, 0]],
                        pastPath: []
                    })
                };
            }
            if (url.includes('/api/session/sync') || url.includes('/api/simulation/run')) {
                return {
                    json: async () => ({
                        status: 'ok',
                        newPath: [[0, 0], [1, 0], [2, 0]],
                        logs: ['Step 1: MoveForward']
                    })
                };
            }
            if (url.includes('/api/momot/solutions')) {
                return {
                    json: async () => ([
                        { modelPath: 'solution1.xmi', objectiveLine: '-1 3 2 0' }
                    ])
                };
            }
            if (url.includes('/api/momot/load')) {
                return {
                    json: async () => ({ status: 'ok', xml: '<xml></xml>' })
                };
            }
            return { json: async () => ({ status: 'ok' }) };
        };

        window.fetch = global.fetch;

        const bridgeScript = fs.readFileSync(path.join(__dirname, '../common/webBridge.js'), 'utf8');
        window.eval(bridgeScript);
    });

    afterEach(() => {
        if (dom && dom.window) {
            dom.window.close();
        }
    });

    it('creates javaBridge on window object', () => {
        assert.ok(window.javaBridge, 'window.javaBridge should be defined');
        assert.strictEqual(typeof window.javaBridge.syncModel, 'function');
        assert.strictEqual(typeof window.javaBridge.debugStart, 'function');
    });

    it('syncModel updates last synced model', async () => {
        window.javaBridge.syncModel('<xml><block type="maze_moveForward"></block></xml>');
        await new Promise(r => setTimeout(r, 50));
        assert.ok(window.__lastSyncedXml);
    });

    it('local debugger computes step trace without error', () => {
        window.X = [[2, 1, 3]];
        const frame0Str = window.javaBridge.debugStart(0, 0, 1);
        const frame0 = JSON.parse(frame0Str);

        assert.strictEqual(frame0.q, 0);
        assert.strictEqual(frame0.s, 0);
        assert.strictEqual(frame0.paused, true);
    });

    it('reports an empty solutions cache as an empty list', () => {
        assert.strictEqual(window.javaBridge.listMomotSolutions(), '[]');
    });

    it('renders MoMoT solutions while the run is active and again when it finishes', async () => {
        const rendered = [];
        window.__momotRenderSolutions = function(data) { rendered.push(data); };

        let phase = 'running';
        const realSetTimeout = window.setTimeout.bind(window);
        const realClearTimeout = window.clearTimeout.bind(window);
        let pollCb = null;
        let retryCb = null;

        window.setInterval = function(cb) {
            pollCb = cb;
            return 41;
        };
        window.clearInterval = function(id) {
            if (id === 41) pollCb = null;
        };
        window.setTimeout = function(cb, ms) {
            if (ms === 150) return realSetTimeout(cb, 0);
            if (ms === 400) {
                retryCb = cb;
                return 42;
            }
            return realSetTimeout(cb, ms);
        };
        window.clearTimeout = function(id) {
            if (id === 42) {
                retryCb = null;
                return;
            }
            realClearTimeout(id);
        };

        const previousFetch = window.fetch;
        window.fetch = async (url) => {
            if (url.includes('/api/momot/run')) {
                return { json: async () => ({ status: 'ok', running: true }) };
            }
            if (url.includes('/api/momot/status')) {
                if (phase === 'running') {
                    return { json: async () => ({ running: true, status: 'Running', logs: ['tick'] }) };
                }
                return { json: async () => ({ running: false, status: 'Finished', logs: ['tick', 'done'] }) };
            }
            if (url.includes('/api/momot/solutions')) {
                return { json: async () => ([{ modelPath: 'live.xmi', objectiveLine: '-1 2 1 0' }]) };
            }
            return previousFetch(url);
        };
        global.fetch = window.fetch;

        window.javaBridge.runMomotWithParams(1, 20, 100, 1, 8);
        await new Promise(r => realSetTimeout(r, 30));
        assert.ok(pollCb, 'status polling should start after the run is accepted');

        const before = rendered.length;
        pollCb();
        await new Promise(r => realSetTimeout(r, 30));
        assert.ok(rendered.length > before, 'solutions render while the run is still active');
        assert.strictEqual(rendered[rendered.length - 1][0].modelPath, 'live.xmi');
        assert.ok(pollCb, 'polling continues while the run is active');

        phase = 'finished';
        pollCb();
        await new Promise(r => realSetTimeout(r, 30));
        assert.strictEqual(pollCb, null, 'polling stops when the run finishes');
        assert.ok(rendered.length > before + 1, 'solutions render again when the run finishes');
        assert.strictEqual(typeof retryCb, 'function', 'a follow-up solutions fetch is scheduled');

        const afterFinish = rendered.length;
        retryCb();
        await new Promise(r => realSetTimeout(r, 30));
        assert.ok(rendered.length > afterFinish, 'the delayed fetch renders the final solutions');
    });
});
