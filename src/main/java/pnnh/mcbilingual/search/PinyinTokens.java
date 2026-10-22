package pnnh.mcbilingual.search;

import java.util.Locale;
import java.util.Optional;

import com.github.promeg.pinyinhelper.Pinyin;

/**
 * Toneless pinyin for a Chinese name. Uses the library's first reading of each
 * character and does not apply a word dictionary.
 */
public final class PinyinTokens {
    private PinyinTokens() {
    }

    public record Reading(String full, String initials) {
    }

    /**
     * @return empty when {@code text} has no Han characters
     */
    public static Optional<Reading> from(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        StringBuilder full = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (!Pinyin.isChinese(character)) {
                continue;
            }
            String syllable = Pinyin.toPinyin(character).toLowerCase(Locale.ROOT);
            if (syllable.isBlank()) {
                continue;
            }
            full.append(syllable);
            initials.append(syllable.charAt(0));
        }
        if (full.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Reading(full.toString(), initials.toString()));
    }
}
