package pnnh.mcbilingual.translate;

/**
 * Pure geometry for vision fallback ROI. No Minecraft imports.
 */
public final class VisionRoi {
    /** Default crop edge length in freeze-frame pixels. */
    public static final int DEFAULT_SIZE = 320;

    public record Rect(int x, int y, int width, int height) {
        public Rect {
            if (width < 0 || height < 0) {
                throw new IllegalArgumentException("size");
            }
        }
    }

    private VisionRoi() {
    }

    /**
     * Axis-aligned crop centered on the cursor, clamped inside the frame.
     */
    public static Rect clampCentered(int cursorX, int cursorY, int frameW, int frameH, int size) {
        if (frameW <= 0 || frameH <= 0) {
            return new Rect(0, 0, 0, 0);
        }
        int edge = Math.max(1, size);
        int w = Math.min(edge, frameW);
        int h = Math.min(edge, frameH);
        int x = cursorX - w / 2;
        int y = cursorY - h / 2;
        x = Math.max(0, Math.min(x, frameW - w));
        y = Math.max(0, Math.min(y, frameH - h));
        return new Rect(x, y, w, h);
    }

    /**
     * Quantize ROI to reduce hover jitter cache misses (8px grid).
     */
    public static Rect quantize(Rect rect, int grid) {
        int g = Math.max(1, grid);
        int x = (rect.x() / g) * g;
        int y = (rect.y() / g) * g;
        return new Rect(x, y, rect.width(), rect.height());
    }

    public static String cacheKey(long frameGeneration, Rect rect) {
        Rect q = quantize(rect, 8);
        return frameGeneration + ":" + q.x() + "," + q.y() + "," + q.width() + "x" + q.height();
    }
}
