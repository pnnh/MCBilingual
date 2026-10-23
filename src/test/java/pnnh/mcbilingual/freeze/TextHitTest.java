package pnnh.mcbilingual.freeze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class TextHitTest {
    private final UiSentence sentence = new UiSentence("苹果", "Apple");

    @Test
    void overlappingHitUsesTheLaterOne() {
        TextHit lower = new TextHit(0, 0, 20, 10, sentence, 1);
        TextHit upper = new TextHit(5, 0, 20, 10, new UiSentence("Apple", "苹果"), 4);

        Optional<TextHit> hit = TextHit.topmost(List.of(lower, upper), 10, 2);

        assertEquals(upper, hit.orElseThrow());
    }

    @Test
    void pointOutsideReturnsEmpty() {
        TextHit hit = new TextHit(0, 0, 10, 10, sentence, 1);

        assertTrue(TextHit.topmost(List.of(hit), 10, 2).isEmpty());
    }

    @Test
    void emptyRectangleIsDropped() {
        TextHit empty = new TextHit(0, 0, 0, 9, sentence, 1);
        TextHit kept = new TextHit(0, 0, 8, 9, sentence, 2);

        assertEquals(List.of(kept), TextHit.visibleOnly(List.of(empty, kept)));
    }
}
