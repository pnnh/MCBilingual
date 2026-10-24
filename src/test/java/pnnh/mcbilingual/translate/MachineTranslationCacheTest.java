package pnnh.mcbilingual.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import pnnh.mcbilingual.translate.MachineTranslationCache.Kind;
import pnnh.mcbilingual.translate.MachineTranslationCache.Job;
import pnnh.mcbilingual.translate.MachineTranslationCache.Status;

class MachineTranslationCacheTest {
    @Test
    void sameKindAndSourceHitsCache() {
        MachineTranslationCache cache = new MachineTranslationCache();
        cache.put(Job.ok(Kind.WORD, "apple", "苹果"));
        assertEquals(Status.OK, cache.get(Kind.WORD, "apple").orElseThrow().status());
        assertEquals("苹果", cache.get(Kind.WORD, "apple").orElseThrow().result());
    }

    @Test
    void differentKindsDoNotCollide() {
        MachineTranslationCache cache = new MachineTranslationCache();
        cache.put(Job.ok(Kind.WORD, "Hello.", "你好。"));
        cache.put(Job.ok(Kind.SENTENCE, "Hello.", "你好呀。"));
        assertNotEquals(
                cache.get(Kind.WORD, "Hello.").orElseThrow().result(),
                cache.get(Kind.SENTENCE, "Hello.").orElseThrow().result());
        assertTrue(cache.putIfAbsentPending(Kind.WORD, "new"));
        assertTrue(cache.get(Kind.WORD, "new").orElseThrow().status() == Status.PENDING);
    }
}
