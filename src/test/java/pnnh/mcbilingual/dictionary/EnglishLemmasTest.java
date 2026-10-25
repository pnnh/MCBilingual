package pnnh.mcbilingual.dictionary;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EnglishLemmasTest {
    @Test
    void pastTenseYieldsStem() {
        assertTrue(EnglishLemmas.candidates("tucked").contains("tuck"));
    }

    @Test
    void pluralYieldsStem() {
        assertTrue(EnglishLemmas.candidates("citizens").contains("citizen"));
    }

    @Test
    void exchangeFormsParsed() {
        assertTrue(EnglishLemmas.formsFromExchange("d:tucked/p:tucked/i:tucking").contains("tucked"));
        assertTrue(EnglishLemmas.formsFromExchange("d:tucked/p:tucked/i:tucking").contains("tucking"));
    }
}
