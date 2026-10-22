package pnnh.mcbilingual.switchlang;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pnnh.mcbilingual.language.LanguageCodes;

class LanguageCycleTest {
    @Test
    void chineseGoesToEnglishAndBack() {
        assertEquals(LanguageCodes.EN_US, LanguageCycle.next(LanguageCodes.ZH_CN));
        assertEquals(LanguageCodes.ZH_CN, LanguageCycle.next(LanguageCodes.EN_US));
    }

    @Test
    void otherCodesEnterChinese() {
        assertEquals(LanguageCodes.ZH_CN, LanguageCycle.next("ja_jp"));
        assertEquals(LanguageCodes.ZH_CN, LanguageCycle.next(""));
        assertEquals(LanguageCodes.ZH_CN, LanguageCycle.next("fr_fr"));
        assertEquals(LanguageCodes.ZH_CN, LanguageCycle.next(null));
    }

    @Test
    void tenPressesStayInsideThePair() {
        String code = LanguageCodes.ZH_CN;
        for (int i = 0; i < 10; i++) {
            code = LanguageCycle.next(code);
            if (i % 2 == 0) {
                assertEquals(LanguageCodes.EN_US, code);
            } else {
                assertEquals(LanguageCodes.ZH_CN, code);
            }
        }
    }

    @Test
    void aThirdLanguageIsNotRemembered() {
        String code = LanguageCycle.next("ja_jp");
        assertEquals(LanguageCodes.ZH_CN, code);
        assertEquals(LanguageCodes.EN_US, LanguageCycle.next(code));
        assertEquals(LanguageCodes.ZH_CN, LanguageCycle.next(LanguageCycle.next(code)));
    }
}
