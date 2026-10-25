package pnnh.mcbilingual.dictionary;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WordCardFormatterTest {
    @Test
    void cardIncludesPosExchangeAndSenses() {
        LexiconEntry entry = new LexiconEntry(
                "tuck",
                "tʌk",
                "v.",
                "d:tucked/i:tucking",
                "① 塞进\\n② 掖好");
        String card = WordCardFormatter.format(new LemmaResolution("tucked", entry, LemmaResolution.Via.EXCHANGE));
        assertTrue(card.contains("tuck"));
        assertTrue(card.contains("tucked"));
        assertTrue(card.contains("v."));
        assertTrue(card.contains("塞进"));
        assertTrue(card.contains("掖好"));
        assertTrue(card.contains("\n"));
        assertFalse(card.equalsIgnoreCase("tucked"));
    }
}
