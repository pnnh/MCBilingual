package pnnh.mcbilingual.tooltip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import pnnh.mcbilingual.language.LanguageCodes;

class SecondaryNamePolicyTest {
    private static final String KEY = "item.minecraft.apple";

    @Test
    void chinesePrimaryAddsEnglishWhenDifferent() {
        List<SecondaryNameLine> lines = SecondaryNamePolicy.lines("苹果", LanguageCodes.ZH_CN, KEY, "苹果", "Apple");

        assertEquals(List.of(new SecondaryNameLine(LanguageCodes.EN_US, "Apple")), lines);
    }

    @Test
    void englishPrimaryAddsChineseWhenDifferent() {
        List<SecondaryNameLine> lines = SecondaryNamePolicy.lines("Apple", LanguageCodes.EN_US, KEY, "苹果", "Apple");

        assertEquals(List.of(new SecondaryNameLine(LanguageCodes.ZH_CN, "苹果")), lines);
    }

    @Test
    void sameVisibleTextAddsNothing() {
        List<SecondaryNameLine> lines = SecondaryNamePolicy.lines("TNT", LanguageCodes.EN_US, KEY, "TNT", "TNT");

        assertTrue(lines.isEmpty());
    }

    @Test
    void caseAndOuterSpaceDoNotCountAsDifferent() {
        List<SecondaryNameLine> lines = SecondaryNamePolicy.lines(" Apple ", LanguageCodes.ZH_CN, KEY, "苹果", "apple");

        assertTrue(lines.isEmpty());
    }

    @Test
    void missingTranslationAndRawKeyAreOmitted() {
        assertTrue(SecondaryNamePolicy.lines("Apple", LanguageCodes.EN_US, KEY, null, "Apple").isEmpty());
        assertTrue(SecondaryNamePolicy.lines("Apple", LanguageCodes.EN_US, KEY, "  ", "Apple").isEmpty());
        assertTrue(SecondaryNamePolicy.lines("Apple", LanguageCodes.EN_US, KEY, KEY, "Apple").isEmpty());
    }

    @Test
    void unresolvedFormatPlaceholdersAreOmitted() {
        assertTrue(SecondaryNamePolicy.lines(
                "Cave Spider Spawner",
                LanguageCodes.EN_US,
                "jade.spawner",
                "%s刷怪笼",
                "%s Spawner").isEmpty());
    }

    @Test
    void languageOutsideThePairShowsBothDistinctNames() {
        List<SecondaryNameLine> lines = SecondaryNamePolicy.lines("りんご", "ja_jp", KEY, "苹果", "Apple");

        assertEquals(
                List.of(
                        new SecondaryNameLine(LanguageCodes.ZH_CN, "苹果"),
                        new SecondaryNameLine(LanguageCodes.EN_US, "Apple")),
                lines);
    }

    @Test
    void languageOutsideThePairDropsTheNameThatMatchesTheTitle() {
        List<SecondaryNameLine> lines = SecondaryNamePolicy.lines("Apple", "ja_jp", KEY, "苹果", "Apple");

        assertEquals(List.of(new SecondaryNameLine(LanguageCodes.ZH_CN, "苹果")), lines);
    }

    @Test
    void twentyIdenticalOrMissingNamesAddNoLines() {
        for (int i = 0; i < 20; i++) {
            String key = "item.test." + i;
            assertTrue(SecondaryNamePolicy.lines("Same", LanguageCodes.ZH_CN, key, "Same", "Same").isEmpty());
            assertTrue(SecondaryNamePolicy.lines("Same", LanguageCodes.EN_US, key, key, "Same").isEmpty());
        }
    }
}
