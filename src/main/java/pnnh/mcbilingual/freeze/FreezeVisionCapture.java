package pnnh.mcbilingual.freeze;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.mojang.blaze3d.platform.NativeImage;

import pnnh.mcbilingual.MCBilingual;
import pnnh.mcbilingual.config.ClientConfig;
import pnnh.mcbilingual.freeze.WordLookup.Hint;
import pnnh.mcbilingual.translate.ExtendedTranslate;
import pnnh.mcbilingual.translate.MachineTranslationCache.Job;
import pnnh.mcbilingual.translate.VisionRoi;
import pnnh.mcbilingual.translate.VisionRoi.Rect;

/**
 * Crops the frozen frame ROI and starts a vision MT job. Client-only; failures return empty.
 */
public final class FreezeVisionCapture {
    private FreezeVisionCapture() {
    }

    /**
     * UNIT mode + no draw-path string: request vision translation for the cursor ROI.
     */
    public static Hint resolve(
            NativeImage frame,
            long frameGeneration,
            int mouseGuiX,
            int mouseGuiY,
            int guiWidth,
            int guiHeight,
            int imageWidth,
            int imageHeight) {
        if (!ClientConfig.extendedTranslation()) {
            return Hint.none();
        }
        if (frame == null || imageWidth <= 0 || imageHeight <= 0 || guiWidth <= 0 || guiHeight <= 0) {
            return Hint.none();
        }
        int cursorX = mouseGuiX * imageWidth / guiWidth;
        int cursorY = mouseGuiY * imageHeight / guiHeight;
        Rect roi = VisionRoi.clampCentered(cursorX, cursorY, imageWidth, imageHeight, VisionRoi.DEFAULT_SIZE);
        if (roi.width() <= 0 || roi.height() <= 0) {
            return Hint.none();
        }
        String key = VisionRoi.cacheKey(frameGeneration, roi);
        Optional<Job> existing = ExtendedTranslate.ollama().cache().get(
                pnnh.mcbilingual.translate.MachineTranslationCache.Kind.VISION, key);
        if (existing.isPresent()) {
            return WordLookup.fromJob(existing.get());
        }
        Optional<byte[]> png = encodeRoiPng(frame, roi);
        if (png.isEmpty()) {
            return Hint.none();
        }
        Optional<Job> job = ExtendedTranslate.ollama().requestVision(key, png.get());
        if (job.isEmpty()) {
            return Hint.none();
        }
        return WordLookup.fromJob(job.get());
    }

    static Optional<byte[]> encodeRoiPng(NativeImage frame, Rect roi) {
        if (frame == null || roi == null || roi.width() <= 0 || roi.height() <= 0) {
            return Optional.empty();
        }
        int fw = frame.getWidth();
        int fh = frame.getHeight();
        if (roi.x() < 0 || roi.y() < 0 || roi.x() + roi.width() > fw || roi.y() + roi.height() > fh) {
            return Optional.empty();
        }
        try {
            BufferedImage buffered = new BufferedImage(roi.width(), roi.height(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < roi.height(); y++) {
                for (int x = 0; x < roi.width(); x++) {
                    int abgr = frame.getPixelRGBA(roi.x() + x, roi.y() + y);
                    buffered.setRGB(x, y, abgrToArgb(abgr));
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (!ImageIO.write(buffered, "png", out)) {
                return Optional.empty();
            }
            return Optional.of(out.toByteArray());
        } catch (Exception exception) {
            MCBilingual.LOGGER.debug("Vision ROI encode failed", exception);
            return Optional.empty();
        }
    }

    /** NativeImage packs ABGR; BufferedImage wants ARGB. */
    static int abgrToArgb(int abgr) {
        int a = (abgr >> 24) & 0xFF;
        int b = (abgr >> 16) & 0xFF;
        int g = (abgr >> 8) & 0xFF;
        int r = abgr & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
