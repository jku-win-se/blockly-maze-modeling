package blocky_game;

import javafx.application.Application;

public class Main {
    static {
        // JavaFX WebView can crash on some Windows setups when it tries to initialize the
        // HTML5 media pipeline (GStreamer) even though Blockly Maze doesn't require it.
        // Disable WebKit media player to avoid native JVM crashes in javafx.media.
        System.setProperty("com.sun.webkit.useMediaPlayer", "false");
        // Best-effort extra guard for older JavaFX media stacks.
        System.setProperty("com.sun.media.jfxmediaimpl.disableGStreamer", "true");

        // Fix for JavaFX Prism D3D texture pool race condition on Windows (JDK-8352209:
        // com.sun.prism.d3d.D3DTextureResource.getResource() returning null during texture updates):
        if (System.getProperty("prism.dirtyopts") == null) {
            System.setProperty("prism.dirtyopts", "false");
        }
        if (System.getProperty("prism.disableRegionCaching") == null) {
            System.setProperty("prism.disableRegionCaching", "true");
        }
        if (System.getProperty("prism.cacheshapes") == null) {
            System.setProperty("prism.cacheshapes", "false");
        }
        if (System.getProperty("prism.primtextures") == null) {
            System.setProperty("prism.primtextures", "false");
        }
        if (System.getProperty("prism.maxvram") == null) {
            System.setProperty("prism.maxvram", "1G");
        }
        if (System.getProperty("prism.targetvram") == null) {
            System.setProperty("prism.targetvram", "512M");
        }
    }

    public static void main(String[] args) {
        Application.launch(BlockyUI.class, args);
    }
}
