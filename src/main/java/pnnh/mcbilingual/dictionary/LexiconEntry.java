package pnnh.mcbilingual.dictionary;

import java.util.Objects;

/**
 * One learner-dictionary row. Empty optional fields are "".
 */
public record LexiconEntry(
        String headword,
        String phonetic,
        String pos,
        String exchange,
        String translation) {
    public LexiconEntry {
        Objects.requireNonNull(headword, "headword");
        phonetic = phonetic == null ? "" : phonetic;
        pos = pos == null ? "" : pos;
        exchange = exchange == null ? "" : exchange;
        translation = translation == null ? "" : translation;
    }

    public boolean hasChineseGloss() {
        if (translation.isBlank()) {
            return false;
        }
        for (int i = 0; i < translation.length(); i++) {
            char c = translation.charAt(i);
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }
}
