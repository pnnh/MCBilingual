package pnnh.mcbilingual.search;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WaypointNameMatchTest {
    @Test
    void blankQueryAlwaysMatches() {
        assertTrue(WaypointNameMatch.matches(null, "制图塔"));
        assertTrue(WaypointNameMatch.matches("", "制图塔"));
        assertTrue(WaypointNameMatch.matches("   ", "制图塔"));
    }

    @Test
    void chineseSubstringMatches() {
        assertTrue(WaypointNameMatch.matches("制图", "制图塔"));
    }

    @Test
    void tonelessFullPinyinMatches() {
        // TinyPinyin first readings: 制图塔 → zhituta
        assertTrue(WaypointNameMatch.matches("zhitu", "制图塔"));
        assertTrue(WaypointNameMatch.matches("zhituta", "制图塔"));
    }

    @Test
    void initialsMatch() {
        assertTrue(WaypointNameMatch.matches("ztt", "制图塔"));
        assertTrue(WaypointNameMatch.matches("zt", "制图塔"));
    }

    @Test
    void englishSubstringIsCaseInsensitive() {
        assertTrue(WaypointNameMatch.matches("home", "My Home Base"));
        assertTrue(WaypointNameMatch.matches("HOME", "My Home Base"));
    }

    @Test
    void latinOnlyNameHasNoPinyinFallback() {
        assertFalse(WaypointNameMatch.matches("abc", "Home"));
        assertTrue(WaypointNameMatch.matches("ome", "Home"));
    }

    @Test
    void unrelatedQueryDoesNotMatch() {
        assertFalse(WaypointNameMatch.matches("xyz", "制图塔"));
        assertFalse(WaypointNameMatch.matches("abc", "制图塔"));
    }
}
