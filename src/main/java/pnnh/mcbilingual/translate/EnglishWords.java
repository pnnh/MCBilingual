package pnnh.mcbilingual.translate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

/**
 * Splits an English visible string into words and maps cursor X into a word.
 * Widths come from the caller (e.g. Font::width) so tests stay Minecraft-free.
 */
public final class EnglishWords {
    private EnglishWords() {
    }

    public record WordSpan(String word, int startIndex, int endIndex, int pixelStart, int pixelEnd) {
        public boolean containsPixel(int pixelX) {
            return pixelX >= pixelStart && pixelX < pixelEnd;
        }
    }

    public static List<WordSpan> tokenize(String text, ToIntFunction<String> widthOf) {
        List<WordSpan> words = new ArrayList<>();
        if (text == null || text.isEmpty() || widthOf == null) {
            return words;
        }
        int i = 0;
        int pixel = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (isWordChar(c)) {
                int start = i;
                while (i < text.length() && isWordChar(text.charAt(i))) {
                    i++;
                }
                String word = text.substring(start, i);
                int w = Math.max(0, widthOf.applyAsInt(word));
                words.add(new WordSpan(word, start, i, pixel, pixel + w));
                pixel += w;
            } else {
                String gap = text.substring(i, i + 1);
                pixel += Math.max(0, widthOf.applyAsInt(gap));
                i++;
            }
        }
        return words;
    }

    public static Optional<WordSpan> atPixel(String text, int pixelX, ToIntFunction<String> widthOf) {
        for (WordSpan span : tokenize(text, widthOf)) {
            if (span.containsPixel(pixelX)) {
                return Optional.of(span);
            }
        }
        return Optional.empty();
    }

    public static Optional<WordSpan> atCharIndex(String text, int charIndex, ToIntFunction<String> widthOf) {
        List<WordSpan> words = tokenize(text, widthOf);
        for (WordSpan span : words) {
            if (charIndex >= span.startIndex() && charIndex < span.endIndex()) {
                return Optional.of(span);
            }
        }
        return Optional.empty();
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '\'' || c == '-';
    }
}
