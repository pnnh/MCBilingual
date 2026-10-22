package pnnh.mcbilingual.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;

class ItemSearchTermsTest {
    @Test
    void queriesIncludeBothNamesAndPinyin() {
        List<String> queries = ItemSearchTerms.queries("苹果", "Apple");

        assertEquals(List.of("苹果", "apple", "pingguo", "pg"), queries);
    }

    @Test
    void englishCaseIsFoldedOnce() {
        assertEquals(List.of("apple"), ItemSearchTerms.queries(null, " Apple "));
    }

    @Test
    void missingChineseAddsNoPinyin() {
        List<String> queries = ItemSearchTerms.queries(null, "Apple");

        assertEquals(List.of("apple"), queries);
        assertFalse(queries.contains("pingguo"));
        assertFalse(queries.contains("pg"));
    }

    @Test
    void blankChineseAddsNoPinyin() {
        assertEquals(List.of("apple"), ItemSearchTerms.queries("  ", "Apple"));
    }

    @Test
    void identicalLatinNamesDoNotInventPinyin() {
        assertEquals(List.of("tnt"), ItemSearchTerms.queries("TNT", "TNT"));
    }
}
