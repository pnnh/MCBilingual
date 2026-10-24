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
 * WORD mode must not enqueue vision jobs (FR-011).
 */
class VisionWordModeGuardTest {
    @Test
    void wordLookupNeverTouchesVisionKind() {
        MachineTranslationCache cache = new MachineTranslationCache();
        OllamaClient client = new OllamaClient(cache, () -> "http://127.0.0.1:9", () -> 500, () -> true);
        // Simulate what WordLookup uses: only Kind.WORD.
        client.request(Kind.WORD, "zzznotindict");
        assertTrue(cache.get(Kind.VISION, "zzznotindict").isEmpty());
        assertTrue(cache.get(Kind.WORD, "zzznotindict").isPresent());
    }

    @Test
    void blankVisibleYieldsNoneNotVision() {
        Hint hint = WordLookup.resolve("   ", 0, s -> s.length() * 4);
        assertEquals(HintKind.NONE, hint.kind());
    }
}
