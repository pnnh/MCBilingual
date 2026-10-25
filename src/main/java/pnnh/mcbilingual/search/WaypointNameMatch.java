package pnnh.mcbilingual.search;

import java.util.Locale;
import java.util.Optional;

/**
 * Matches a JourneyMap (or similar) waypoint name against a search query using
 * the literal name and, when the name has Han characters, toneless pinyin.
 */
public final class WaypointNameMatch {
    private WaypointNameMatch() {
    }

    /**
     * @return {@code true} when {@code query} is blank, when {@code name} contains
     *         the query (case-insensitive), or when pinyin full / initials contain it
     */
    public static boolean matches(String query, String name) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String needle = query.strip().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            return true;
        }
        String haystack = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (haystack.contains(needle)) {
            return true;
        }
        Optional<PinyinTokens.Reading> reading = PinyinTokens.from(name);
        if (reading.isEmpty()) {
            return false;
        }
        PinyinTokens.Reading tokens = reading.get();
        return tokens.full().contains(needle) || tokens.initials().contains(needle);
    }
}
