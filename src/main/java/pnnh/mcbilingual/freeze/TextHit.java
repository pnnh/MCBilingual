package pnnh.mcbilingual.freeze;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * One visible rectangle of a game sentence, in GUI pixels.
 */
public record TextHit(int x, int y, int width, int height, UiSentence sentence, int order) {
    public boolean contains(int pointX, int pointY) {
        return width > 0
                && height > 0
                && pointX >= x
                && pointY >= y
                && pointX < x + width
                && pointY < y + height;
    }

    public static Optional<TextHit> topmost(List<TextHit> hits, int pointX, int pointY) {
        TextHit best = null;
        for (TextHit hit : hits) {
            if (!hit.contains(pointX, pointY)) {
                continue;
            }
            if (best == null || hit.order > best.order) {
                best = hit;
            }
        }
        return Optional.ofNullable(best);
    }

    public static List<TextHit> visibleOnly(List<TextHit> hits) {
        List<TextHit> kept = new ArrayList<>();
        for (TextHit hit : hits) {
            if (hit.width > 0 && hit.height > 0) {
                kept.add(hit);
            }
        }
        return List.copyOf(kept);
    }
}
