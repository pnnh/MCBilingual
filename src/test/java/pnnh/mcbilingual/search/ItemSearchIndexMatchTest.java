package pnnh.mcbilingual.search;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ItemSearchIndexMatchTest {
    @Test
    void queryContainedInAnyTermMatches() {
        List<String> terms = List.of("苹果", "apple", "pingguo", "pg");

        assertTrue(ItemSearchIndex.anyContains(terms, "ping"));
        assertTrue(ItemSearchIndex.anyContains(terms, "APP"));
        assertTrue(ItemSearchIndex.anyContains(terms, "苹"));
        assertTrue(ItemSearchIndex.anyContains(terms, "pg"));
    }

    @Test
    void missingOrBlankQueryDoesNotMatch() {
        List<String> terms = List.of("apple");

        assertFalse(ItemSearchIndex.anyContains(terms, null));
        assertFalse(ItemSearchIndex.anyContains(terms, "  "));
        assertFalse(ItemSearchIndex.anyContains(null, "apple"));
        assertFalse(ItemSearchIndex.anyContains(List.of(), "apple"));
    }

    @Test
    void englishOnlyTermsDoNotMatchPinyin() {
        List<String> terms = ItemSearchTerms.queries(null, "Apple");

        assertTrue(ItemSearchIndex.anyContains(terms, "app"));
        assertFalse(ItemSearchIndex.anyContains(terms, "pingguo"));
    }
}
