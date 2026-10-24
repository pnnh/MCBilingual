package pnnh.mcbilingual.translate;

import java.util.Locale;
import java.util.Set;

/**
 * Picks the single punctuated sentence that contains a character offset.
 */
public final class SentenceBounds {
    private static final Set<String> ABBREVIATIONS = Set.of(
            "mr", "mrs", "ms", "dr", "prof", "sr", "jr", "vs", "etc", "e.g", "i.e", "st", "ave");

    private SentenceBounds() {
    }

    /**
     * @param text full visible string
     * @param offset character index into {@code text} (clamped)
     * @return the sentence containing the offset, trimmed; empty if text blank
     */
    public static String sentenceAt(String text, int offset) {
        if (text == null || text.isBlank()) {
            return "";
        }
        int len = text.length();
        int at = Math.max(0, Math.min(offset, len - 1));
        int start = 0;
        for (int i = 0; i < at; i++) {
            if (isSentenceEnd(text, i)) {
                start = i + 1;
                while (start < len && Character.isWhitespace(text.charAt(start))) {
                    start++;
                }
            }
        }
        int end = len;
        for (int i = at; i < len; i++) {
            if (isSentenceEnd(text, i)) {
                end = i + 1;
                break;
            }
        }
        return text.substring(start, end).strip();
    }

    private static boolean isSentenceEnd(String text, int index) {
        char c = text.charAt(index);
        if (c == '。' || c == '！' || c == '？' || c == '…' || c == '!' || c == '?') {
            return true;
        }
        if (c != '.') {
            return false;
        }
        if (index > 0 && Character.isDigit(text.charAt(index - 1))
                && index + 1 < text.length() && Character.isDigit(text.charAt(index + 1))) {
            return false;
        }
        int wordStart = index;
        while (wordStart > 0 && Character.isLetter(text.charAt(wordStart - 1))) {
            wordStart--;
        }
        if (wordStart < index) {
            String token = text.substring(wordStart, index).toLowerCase(Locale.ROOT);
            if (ABBREVIATIONS.contains(token)) {
                return false;
            }
        }
        return true;
    }
}
