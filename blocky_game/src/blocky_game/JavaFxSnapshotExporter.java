package blocky_game;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Exports a JavaFX scene root as a true-resolution PNG via {@code Node#snapshot},
 * independent of the on-screen window size.
 */
public final class JavaFxSnapshotExporter {

    public static final int TARGET_WIDTH = 7680;
    public static final int TARGET_HEIGHT = 4320;

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private JavaFxSnapshotExporter() {
    }

    public enum ScalingMode {
        /** Scale uniformly and center on a {@value TARGET_WIDTH}x{@value TARGET_HEIGHT} canvas. */
        PRESERVE_ASPECT_RATIO,
        /** Stretch content to fill the entire {@value TARGET_WIDTH}x{@value TARGET_HEIGHT} canvas. */
        STRETCH_TO_FILL
    }

    public record ExportResult(
            boolean success,
            Path outputPath,
            int width,
            int height,
            long fileSizeBytes,
            String message) {
    }

    /**
     * Exports {@code scene} to {@code outputPath} on the JavaFX application thread.
     * If called from a background thread, schedules work on the FX thread and blocks until done.
     */
    public static ExportResult exportScene(Scene scene, Path outputPath) {
        return exportScene(scene, outputPath, ScalingMode.PRESERVE_ASPECT_RATIO);
    }

    public static ExportResult exportScene(Scene scene, Path outputPath, ScalingMode mode) {
        if (!Platform.isFxApplicationThread()) {
            AtomicReference<ExportResult> result = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    result.set(doExport(scene, outputPath, mode));
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return failure(null, "Interrupted while waiting for JavaFX export thread: " + e.getMessage());
            }
            ExportResult exportResult = result.get();
            return exportResult != null ? exportResult : failure(outputPath, "JavaFX export produced no result.");
        }
        return doExport(scene, outputPath, mode);
    }

    public static Path defaultTimestampedPath(Path directory) {
        String filename = "window-8k-" + TIMESTAMP_FORMAT.format(LocalDateTime.now()) + ".png";
        return directory.resolve(filename);
    }

    /**
     * Re-opens a PNG and verifies its pixel dimensions and non-zero size.
     */
    public static ExportResult verifyPng(Path path, int expectedWidth, int expectedHeight) {
        if (path == null) {
            return failure(null, "Verification path is null.");
        }
        if (!Files.exists(path)) {
            return failure(path, "PNG does not exist: " + path.toAbsolutePath());
        }
        long size;
        try {
            size = Files.size(path);
        } catch (IOException e) {
            return failure(path, "Could not read file size: " + e.getMessage());
        }
        if (size <= 0) {
            return failure(path, "PNG file is empty.");
        }
        try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) {
                return failure(path, "ImageIO could not decode PNG: " + path.toAbsolutePath());
            }
            int w = image.getWidth();
            int h = image.getHeight();
            if (w != expectedWidth || h != expectedHeight) {
                return failure(path, "PNG dimensions " + w + "x" + h
                        + " do not match expected " + expectedWidth + "x" + expectedHeight + ".");
            }
            String message = String.format(
                    "Verified PNG %s (%dx%d, %,d bytes).",
                    path.toAbsolutePath(), w, h, size);
            return new ExportResult(true, path.toAbsolutePath(), w, h, size, message);
        } catch (IOException e) {
            return failure(path, "Failed to read PNG for verification: " + e.getMessage());
        }
    }

    private static ExportResult doExport(Scene scene, Path outputPath, ScalingMode mode) {
        if (scene == null) {
            return failure(outputPath, "Scene is null.");
        }
        Parent root = scene.getRoot();
        if (root == null) {
            return failure(outputPath, "Scene root is null.");
        }
        if (outputPath == null) {
            return failure(null, "Output path is null.");
        }

        Path normalized;
        try {
            normalized = outputPath.toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            return failure(outputPath, "Invalid output path: " + e.getMessage());
        }

        double sourceW = scene.getWidth();
        double sourceH = scene.getHeight();
        root.applyCss();
        if (sourceW <= 0 || sourceH <= 0) {
            Bounds bounds = root.getBoundsInLocal();
            sourceW = bounds.getWidth();
            sourceH = bounds.getHeight();
        }
        if (sourceW <= 0 || sourceH <= 0) {
            return failure(normalized, "Scene dimensions are zero (width=" + sourceW + ", height=" + sourceH + ").");
        }

        root.layout();

        try {
            Path parent = normalized.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            WritableImage canvas = renderToCanvas(root, sourceW, sourceH, mode);
            BufferedImage buffered = toBufferedImage(canvas);
            if (!ImageIO.write(buffered, "png", normalized.toFile())) {
                return failure(normalized, "ImageIO.write failed: no PNG writer available.");
            }

            long fileSize = Files.size(normalized);
            String message = String.format(
                    "8K PNG exported to %s (%dx%d, %,d bytes).",
                    normalized, TARGET_WIDTH, TARGET_HEIGHT, fileSize);
            System.out.println("[JavaFxSnapshotExporter] " + message);
            return new ExportResult(true, normalized, TARGET_WIDTH, TARGET_HEIGHT, fileSize, message);
        } catch (IOException e) {
            return failure(normalized, "PNG write failed: " + e.getMessage());
        } catch (Exception e) {
            return failure(normalized, "Snapshot export failed: " + e.getMessage());
        }
    }

    private static WritableImage renderToCanvas(Parent root, double sourceW, double sourceH, ScalingMode mode) {
        WritableImage canvas = new WritableImage(TARGET_WIDTH, TARGET_HEIGHT);
        fillWhite(canvas);

        if (mode == ScalingMode.STRETCH_TO_FILL) {
            WritableImage stretched = new WritableImage(TARGET_WIDTH, TARGET_HEIGHT);
            SnapshotParameters params = new SnapshotParameters();
            double scaleX = TARGET_WIDTH / sourceW;
            double scaleY = TARGET_HEIGHT / sourceH;
            params.setTransform(Transform.scale(scaleX, scaleY));
            params.setViewport(new Rectangle2D(0, 0, sourceW, sourceH));
            root.snapshot(params, stretched);
            blit(stretched, canvas, 0, 0);
            return canvas;
        }

        double scale = Math.min((double) TARGET_WIDTH / sourceW, (double) TARGET_HEIGHT / sourceH);
        int contentW = Math.max(1, (int) Math.round(sourceW * scale));
        int contentH = Math.max(1, (int) Math.round(sourceH * scale));
        int offsetX = (TARGET_WIDTH - contentW) / 2;
        int offsetY = (TARGET_HEIGHT - contentH) / 2;

        WritableImage content = new WritableImage(contentW, contentH);
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(Transform.scale(scale, scale));
        params.setViewport(new Rectangle2D(0, 0, sourceW, sourceH));
        root.snapshot(params, content);
        blit(content, canvas, offsetX, offsetY);
        return canvas;
    }

    private static void fillWhite(WritableImage image) {
        PixelWriter writer = image.getPixelWriter();
        for (int y = 0; y < TARGET_HEIGHT; y++) {
            for (int x = 0; x < TARGET_WIDTH; x++) {
                writer.setArgb(x, y, 0xFFFFFFFF);
            }
        }
    }

    private static void blit(WritableImage source, WritableImage target, int destX, int destY) {
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = target.getPixelWriter();
        int w = (int) source.getWidth();
        int h = (int) source.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                writer.setArgb(destX + x, destY + y, reader.getArgb(x, y));
            }
        }
    }

    private static BufferedImage toBufferedImage(WritableImage image) {
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        BufferedImage buffered = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = image.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                buffered.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        return buffered;
    }

    private static ExportResult failure(Path outputPath, String message) {
        System.err.println("[JavaFxSnapshotExporter] " + message);
        return new ExportResult(false, outputPath != null ? outputPath.toAbsolutePath() : null,
                0, 0, 0L, message);
    }
}
