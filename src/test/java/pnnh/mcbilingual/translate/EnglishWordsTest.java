package pnnh.mcbilingual.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import pnnh.mcbilingual.translate.EnglishWords.WordSpan;

class EnglishWordsTest {
    @Test
    void tokenizesMultipleWords() {
        List<WordSpan> words = EnglishWords.tokenize("Save Game", String::length);
        assertEquals(2, words.size());
        assertEquals("Save", words.get(0).word());
        assertEquals("Game", words.get(1).word());
    }

    @Test
    void pixelHitsSecondWord() {
        // widths: Save=4, space=1, Game=4 → Game starts at 5
        Optional<WordSpan> hit = EnglishWords.atPixel("Save Game", 5, String::length);
        assertTrue(hit.isPresent());
        assertEquals("Game", hit.get().word());
    }
}
