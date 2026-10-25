package pnnh.mcbilingual.dictionary;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Guards the frequency-truncated ECDICT bundle (feature 010).
 */
class BundledLexiconScaleTest {
    @Test
    void bundledLexiconCoversHighFrequencyLemmas() throws Exception {
        LearnerDictionary dictionary = LearnerDictionary.loadClasspath(LearnerDictionary.DEFAULT_RESOURCE);
        assertTrue(dictionary.size() >= 50_000, "expected ~60k frq-ranked lemmas, got " + dictionary.size());
        assertTrue(dictionary.lookupRich("into").isPresent());
        assertTrue(dictionary.lookupRich("tucked").isPresent());
        assertTrue(dictionary.lookupRich("the").isPresent());
    }
}
