package pnnh.mcbilingual.language;

/**
 * The only language pair this stage understands.
 */
public final class LanguageCodes {
    public static final String ZH_CN = "zh_cn";
    public static final String EN_US = "en_us";

    private LanguageCodes() {
    }

    public static boolean isChinese(String code) {
        return ZH_CN.equalsIgnoreCase(code);
    }

    public static boolean isEnglish(String code) {
        return EN_US.equalsIgnoreCase(code);
    }
}
