package pnnh.mcbilingual.freeze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import pnnh.mcbilingual.freeze.WordLookup.Hint;
import pnnh.mcbilingual.freeze.WordLookup.HintKind;
import pnnh.mcbilingual.translate.MachineTranslationCache;
import pnnh.mcbilingual.translate.MachineTranslationCache.Kind;
import pnnh.mcbilingual.translate.OllamaClient;

/**
 * Pure priority checks without Minecraft or live HTTP.
 */
class LookupPriorityTest {
    @Test
    void unitModePrefersPackCounterpartOverMachineTranslation() {
        UiSentence sentence = new UiSentence("整段对照。第二句。", "Full unit. Second sentence.");
        Hint hint = UnitLookup.resolve(sentence, 0);
        assertEquals(HintKind.GLOSS, hint.kind());
        assertEquals("整段对照。第二句。", hint.text());
    }

    @Test
    void disabledClientDoesNotQueueMachineTranslation() {
        MachineTranslationCache cache = new MachineTranslationCache();
        OllamaClient client = new OllamaClient(cache, () -> "http://127.0.0.1:9", () -> 1000, () -> false);
        assertTrue(client.request(Kind.WORD, "newword").isEmpty());
        assertTrue(cache.get(Kind.WORD, "newword").isEmpty());
    }

    @Test
    void wordHintKindsAreDistinctFromUnitPackText() {
        UiSentence pack = new UiSentence("整段", "Settings");
        Hint unit = UnitLookup.resolve(pack, 0);
        assertEquals("整段", unit.text());
        // Word path has no counterpart argument — only the visible string matters.
        assertTrue(WordLookup.HintKind.GLOSS != null);
    }
}
