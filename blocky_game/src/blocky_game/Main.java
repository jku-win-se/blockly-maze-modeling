package blocky_game;

import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        // JavaFX WebView can crash on some Windows setups when it tries to initialize the
        // HTML5 media pipeline (GStreamer) even though Blockly Maze doesn't require it.
        // Disable WebKit media player to avoid native JVM crashes in javafx.media.
        System.setProperty("com.sun.webkit.useMediaPlayer", "false");
        // Best-effort extra guard for older JavaFX media stacks.
        System.setProperty("com.sun.media.jfxmediaimpl.disableGStreamer", "true");
        Application.launch(BlockyUI.class, args);
    }
}
