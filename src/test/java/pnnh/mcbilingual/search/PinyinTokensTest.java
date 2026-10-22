package pnnh.mcbilingual.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PinyinTokensTest {
    @Test
    void appleUsesContinuousTonelessPinyinAndInitials() {
        PinyinTokens.Reading reading = PinyinTokens.from("苹果").orElseThrow();

        assertEquals("pingguo", reading.full());
        assertEquals("pg", reading.initials());
        assertFalse(reading.full().chars().anyMatch(Character::isDigit));
        assertFalse(reading.full().contains(" "));
    }

    @Test
    void readingsAreAlreadyLowerCase() {
        PinyinTokens.Reading reading = PinyinTokens.from("苹果").orElseThrow();

        assertEquals(reading.full(), reading.full().toLowerCase());
        assertEquals(reading.initials(), reading.initials().toLowerCase());
    }

    @Test
    void latinTextProducesNoReading() {
        assertTrue(PinyinTokens.from("TNT").isEmpty());
        assertTrue(PinyinTokens.from("Apple").isEmpty());
    }

    @Test
    void latinCharactersStayOutOfTheReading() {
        PinyinTokens.Reading reading = PinyinTokens.from("苹果TNT").orElseThrow();

        assertEquals("pingguo", reading.full());
        assertEquals("pg", reading.initials());
    }

    @Test
    void blankTextProducesNoReading() {
        assertTrue(PinyinTokens.from(null).isEmpty());
        assertTrue(PinyinTokens.from("").isEmpty());
        assertTrue(PinyinTokens.from("   ").isEmpty());
    }

    @Test
    void polyphoneUsesTheFirstCharacterReadingWithoutWordCorrection() {
        assertEquals("xing", PinyinTokens.from("行").orElseThrow().full());
        assertEquals("yinxing", PinyinTokens.from("银行").orElseThrow().full());
        assertEquals("xingzou", PinyinTokens.from("行走").orElseThrow().full());
    }
}
