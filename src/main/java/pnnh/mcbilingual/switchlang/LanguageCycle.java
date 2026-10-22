package pnnh.mcbilingual.switchlang;

import pnnh.mcbilingual.language.LanguageCodes;

/**
 * Next client language. Only {@code zh_cn} and {@code en_us} are produced.
 * Anything else, including empty, becomes {@code zh_cn} on the next press.
 */
public final class LanguageCycle {
    private LanguageCycle() {
    }

    public static String next(String current) {
        if (LanguageCodes.isChinese(current)) {
            return LanguageCodes.EN_US;
        }
        if (LanguageCodes.isEnglish(current)) {
            return LanguageCodes.ZH_CN;
        }
        return LanguageCodes.ZH_CN;
    }
}
