package pnnh.mcbilingual.freeze;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Draft rectangles for one unfrozen frame. Chat sequences outlive the frame
 * because a line is wrapped once and drawn again later. Tooltip sequences
 * live only until the frame that drew them is published.
 */
public final class FreezeCapture {
    private static final int MAX_SEQUENCES = 2048;

    private static int depth;
    private static boolean captured;
    private static int order;
    private static final List<TextHit> building = new ArrayList<>();
    private static List<TextHit> latest = List.of();
    private static final ArrayDeque<Component> owners = new ArrayDeque<>();
    private static final ArrayDeque<Clip> clips = new ArrayDeque<>();
    private static final Map<FormattedCharSequence, Component> sequences = new IdentityHashMap<>();
    private static final ArrayDeque<FormattedCharSequence> sequenceOrder = new ArrayDeque<>();
    private static final Map<FormattedCharSequence, Component> frameSequences = new IdentityHashMap<>();
    private static final Map<Component, Optional<UiSentence>> sentences = new IdentityHashMap<>();

    private FreezeCapture() {
    }

    public static void beginDraw() {
        depth++;
        captured = true;
    }

    public static void endDraw() {
        if (depth > 0) {
            depth--;
        }
    }

    public static boolean recording() {
        return depth > 0 && !FreezeSession.get().active();
    }

    public static void pushOwner(Component component) {
        if (component != null) {
            owners.push(component);
        }
    }

    public static void popOwner(Component component) {
        if (component == null || owners.isEmpty()) {
            return;
        }
        if (owners.peek() == component) {
            owners.pop();
            return;
        }
        owners.remove(component);
    }

    /** Pop whatever was last pushed (for synthetic String-draw owners). */
    public static void popTopOwner() {
        if (!owners.isEmpty()) {
            owners.pop();
        }
    }

    public static void pushScissor(int minX, int minY, int maxX, int maxY) {
        if (!recording()) {
            clips.push(Clip.NONE);
            return;
        }
        int x = minX;
        int y = minY;
        int width = maxX - minX;
        int height = maxY - minY;
        if (!clips.isEmpty() && clips.peek() != Clip.NONE) {
            Clip parent = clips.peek();
            int right = Math.min(x + width, parent.x + parent.width);
            int bottom = Math.min(y + height, parent.y + parent.height);
            x = Math.max(x, parent.x);
            y = Math.max(y, parent.y);
            width = right - x;
            height = bottom - y;
        }
        clips.push(new Clip(x, y, width, height));
    }

    public static void popScissor() {
        if (!clips.isEmpty()) {
            clips.pop();
        }
    }

    /**
     * Chat keeps the wrapped sequence and draws it on later frames.
     */
    public static void remember(FormattedCharSequence sequence, Component component) {
        if (sequence == null || component == null || sequences.containsKey(sequence)) {
            return;
        }
        while (sequenceOrder.size() >= MAX_SEQUENCES) {
            sequences.remove(sequenceOrder.removeFirst());
        }
        sequenceOrder.addLast(sequence);
        sequences.put(sequence, component);
    }

    /**
     * Tooltip lines are created and drawn in the same frame.
     */
    public static void rememberFrame(FormattedCharSequence sequence, Component component) {
        if (sequence == null || component == null) {
            return;
        }
        frameSequences.put(sequence, component);
    }

    public static void glyph(Font font, FormattedCharSequence text, float x, float y, Matrix4f matrix) {
        if (!recording() || font == null || text == null || matrix == null) {
            return;
        }
        Component component = owners.peek();
        if (component == null) {
            component = frameSequences.get(text);
        }
        if (component == null) {
            component = sequences.get(text);
        }
        Optional<UiSentence> sentence;
        if (component != null) {
            sentence = sentences.computeIfAbsent(component, ComponentSentences::from);
        } else {
            // Mod GUIs (e.g. Integrated Dynamics guides) often draw sequences
            // without a Component owner. Keep the visible string for word / Ollama.
            String visible = sequenceText(text);
            if (visible.isBlank()) {
                return;
            }
            sentence = Optional.of(new UiSentence("", visible));
        }
        if (sentence.isEmpty()) {
            return;
        }
        Vector4f point = new Vector4f(x, y, 0.0F, 1.0F);
        matrix.transform(point);
        if (point.w != 0.0F && point.w != 1.0F) {
            point.x /= point.w;
            point.y /= point.w;
        }
        float scaleX = (float) Math.hypot(Math.hypot(matrix.m00(), matrix.m01()), matrix.m02());
        float scaleY = (float) Math.hypot(Math.hypot(matrix.m10(), matrix.m11()), matrix.m12());
        if (!Float.isFinite(point.x) || !Float.isFinite(point.y) || !Float.isFinite(scaleX) || !Float.isFinite(scaleY)) {
            return;
        }
        if (scaleX <= 0.0F || scaleY <= 0.0F) {
            return;
        }
        int left = Math.round(point.x);
        int top = Math.round(point.y);
        int width = Math.round(font.width(text) * scaleX);
        int height = Math.round(font.lineHeight * scaleY);
        if (!clips.isEmpty() && clips.peek() != Clip.NONE) {
            Clip clip = clips.peek();
            int right = Math.min(left + width, clip.x + clip.width);
            int bottom = Math.min(top + height, clip.y + clip.height);
            left = Math.max(left, clip.x);
            top = Math.max(top, clip.y);
            width = right - left;
            height = bottom - top;
        }
        if (width <= 0 || height <= 0) {
            return;
        }
        building.add(new TextHit(left, top, width, height, sentence.get(), order++));
    }

    /**
     * Rebuild the drawn string from a visual sequence. Never used as a translation key.
     */
    static String sequenceText(FormattedCharSequence sequence) {
        if (sequence == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });
        return builder.toString().strip();
    }

    public static List<TextHit> latestHits() {
        return latest;
    }

    public static void publishFrame() {
        if (captured && !FreezeSession.get().active()) {
            latest = List.copyOf(building);
        }
        building.clear();
        order = 0;
        owners.clear();
        clips.clear();
        frameSequences.clear();
        sentences.clear();
        captured = false;
        depth = 0;
    }

    private record Clip(int x, int y, int width, int height) {
        private static final Clip NONE = new Clip(0, 0, 0, 0);
    }
}
