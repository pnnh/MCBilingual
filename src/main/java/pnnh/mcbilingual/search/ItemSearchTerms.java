package pnnh.mcbilingual.search;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Lowercase strings JEI can match for one item. A missing Chinese name adds no pinyin.
 */
public final class ItemSearchTerms {
    private ItemSearchTerms() {
    }

    public static List<String> queries(String chineseName, String englishName) {
        Set<String> terms = new LinkedHashSet<>();
        add(terms, chineseName);
        add(terms, englishName);
        if (chineseName != null && !chineseName.isBlank()) {
            PinyinTokens.from(chineseName).ifPresent(reading -> {
                add(terms, reading.full());
                add(terms, reading.initials());
            });
        }
        return List.copyOf(terms);
    }

    private static void add(Set<String> terms, String value) {
        if (value == null) {
            return;
        }
        String folded = value.strip().toLowerCase(Locale.ROOT);
        if (!folded.isEmpty()) {
            terms.add(folded);
        }
    }
}
