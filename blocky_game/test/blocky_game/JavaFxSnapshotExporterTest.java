package blocky_game;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaFxSnapshotExporterTest {

    @BeforeAll
    static void initJavaFx() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        assertTrue(latch.await(30, TimeUnit.SECONDS), "JavaFX toolkit did not start in time.");
    }

    @Test
    void exportScene_writes7680x4320Png(@TempDir Path tempDir) throws Exception {
        AtomicReference<JavaFxSnapshotExporter.ExportResult> resultRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                Label label = new Label("8K export test");
                StackPane root = new StackPane(label);
                Scene scene = new Scene(root, 640, 480);
                new Stage().setScene(scene);

                Path output = tempDir.resolve("test-8k.png");
                resultRef.set(JavaFxSnapshotExporter.exportScene(scene, output));
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(60, TimeUnit.SECONDS), "Export did not complete in time.");

        JavaFxSnapshotExporter.ExportResult exportResult = resultRef.get();
        assertTrue(exportResult.success(), exportResult.message());
        assertEquals(JavaFxSnapshotExporter.TARGET_WIDTH, exportResult.width());
        assertEquals(JavaFxSnapshotExporter.TARGET_HEIGHT, exportResult.height());
        assertTrue(exportResult.fileSizeBytes() > 0, "PNG should be non-empty.");

        JavaFxSnapshotExporter.ExportResult verified = JavaFxSnapshotExporter.verifyPng(
                exportResult.outputPath(),
                JavaFxSnapshotExporter.TARGET_WIDTH,
                JavaFxSnapshotExporter.TARGET_HEIGHT);
        assertTrue(verified.success(), verified.message());
    }

    @Test
    void exportScene_rejectsNullScene(@TempDir Path tempDir) throws Exception {
        AtomicReference<JavaFxSnapshotExporter.ExportResult> resultRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                resultRef.set(JavaFxSnapshotExporter.exportScene(null, tempDir.resolve("null-scene.png")));
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        JavaFxSnapshotExporter.ExportResult result = resultRef.get();
        assertTrue(!result.success());
    }

    @Test
    void stretchMode_fillsCanvas(@TempDir Path tempDir) throws Exception {
        AtomicReference<JavaFxSnapshotExporter.ExportResult> resultRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                StackPane root = new StackPane(new Label("stretch"));
                Scene scene = new Scene(root, 800, 600);
                new Stage().setScene(scene);

                Path output = tempDir.resolve("test-8k-stretch.png");
                resultRef.set(JavaFxSnapshotExporter.exportScene(
                        scene, output, JavaFxSnapshotExporter.ScalingMode.STRETCH_TO_FILL));
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(60, TimeUnit.SECONDS));
        JavaFxSnapshotExporter.ExportResult result = resultRef.get();
        assertTrue(result.success(), result.message());
        assertEquals(7680, result.width());
        assertEquals(4320, result.height());
    }
}
