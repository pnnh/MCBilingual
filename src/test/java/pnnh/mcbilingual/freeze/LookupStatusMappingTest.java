package pnnh.mcbilingual.freeze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import pnnh.mcbilingual.freeze.WordLookup.Hint;
import pnnh.mcbilingual.freeze.WordLookup.HintKind;
import pnnh.mcbilingual.translate.MachineTranslationCache;
import pnnh.mcbilingual.translate.MachineTranslationCache.Kind;
import pnnh.mcbilingual.translate.MachineTranslationCache.Job;
import pnnh.mcbilingual.translate.MachineTranslationCache.Status;
import pnnh.mcbilingual.translate.OllamaClient;

class LookupStatusMappingTest {
    @Test
    void textPendingMapsToTextRecognizingKey() {
        assertEquals(HintKind.TEXT_LOADING, LookupStatusKeys.hintKind(Kind.WORD, Status.PENDING));
        assertEquals(HintKind.TEXT_LOADING, LookupStatusKeys.hintKind(Kind.SENTENCE, Status.PENDING));
        assertEquals(LookupStatusKeys.TEXT_RECOGNIZING, LookupStatusKeys.langKey(HintKind.TEXT_LOADING));
    }

    @Test
    void visionPendingMapsToImageRecognizingKey() {
        assertEquals(HintKind.IMAGE_LOADING, LookupStatusKeys.hintKind(Kind.VISION, Status.PENDING));
        assertEquals(LookupStatusKeys.IMAGE_RECOGNIZING, LookupStatusKeys.langKey(HintKind.IMAGE_LOADING));
        assertNotEquals(LookupStatusKeys.langKey(HintKind.TEXT_LOADING), LookupStatusKeys.langKey(HintKind.IMAGE_LOADING));
    }

    @Test
    void failuresDistinguishPipelines() {
        assertEquals(LookupStatusKeys.TEXT_FAILED, LookupStatusKeys.langKey(HintKind.TEXT_FAILED));
        assertEquals(LookupStatusKeys.IMAGE_FAILED, LookupStatusKeys.langKey(HintKind.IMAGE_FAILED));
        assertEquals(LookupStatusKeys.TEXT_TIMEOUT, LookupStatusKeys.langKey(HintKind.TEXT_TIMEOUT));
        assertEquals(LookupStatusKeys.IMAGE_TIMEOUT, LookupStatusKeys.langKey(HintKind.IMAGE_TIMEOUT));
    }

    @Test
    void fromJobUsesKindForHintPipeline() {
        Hint text = WordLookup.fromJob(Job.pending(Kind.WORD, "foo"));
        Hint image = WordLookup.fromJob(Job.pending(Kind.VISION, "1:0,0,32x32"));
        assertEquals(HintKind.TEXT_LOADING, text.kind());
        assertEquals(HintKind.IMAGE_LOADING, image.kind());
    }

    @Test
    void textRequestRejectsVisionKindAndDisabledSkipsVision() {
        MachineTranslationCache cache = new MachineTranslationCache();
        OllamaClient client = new OllamaClient(cache, () -> "http://127.0.0.1:9", () -> 1000, () -> 2000, () -> false);
        assertTrue(client.request(Kind.VISION, "anything").isEmpty());
        assertTrue(client.requestVision("key", new byte[] {1, 2, 3}).isEmpty());
        assertTrue(cache.get(Kind.VISION, "key").isEmpty());
    }
}
