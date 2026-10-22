package pnnh.mcbilingual.tooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pnnh.mcbilingual.language.LanguageCodes;

/**
 * Chooses which extra name lines to show. This class does not read game state.
 */
public final class SecondaryNamePolicy {
    private SecondaryNamePolicy() {
    }

    /**
     * @param title            text the player already sees as the title
     * @param currentLanguage  active game language code, or anything outside the pair
     * @param translationKey   language-file key; a resolved value equal to this key is missing
     * @param chinese          zh_cn text, or null when that language has no entry
     * @param english          en_us text, or null when that language has no entry
     */
    public static List<SecondaryNameLine> lines(
            String title,
            String currentLanguage,
            String translationKey,
            String chinese,
            String english) {
        String normalizedTitle = normalize(title);
        List<SecondaryNameLine> lines = new ArrayList<>(2);
        if (LanguageCodes.isChinese(currentLanguage)) {
            add(lines, LanguageCodes.EN_US, english, normalizedTitle, translationKey);
        } else if (LanguageCodes.isEnglish(currentLanguage)) {
            add(lines, LanguageCodes.ZH_CN, chinese, normalizedTitle, translationKey);
        } else {
            add(lines, LanguageCodes.ZH_CN, chinese, normalizedTitle, translationKey);
            add(lines, LanguageCodes.EN_US, english, normalizedTitle, translationKey);
        }
        return List.copyOf(lines);
    }

    private static void add(
            List<SecondaryNameLine> lines,
            String language,
            String text,
            String normalizedTitle,
            String translationKey) {
        if (text == null) {
            return;
        }
        String trimmed = text.strip();
        if (trimmed.isEmpty() || trimmed.equals(translationKey)) {
            return;
        }
        if (normalize(trimmed).equals(normalizedTitle)) {
            return;
        }
        for (SecondaryNameLine existing : lines) {
            if (existing.language().equals(language)) {
                return;
            }
        }
        lines.add(new SecondaryNameLine(language, trimmed));
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
