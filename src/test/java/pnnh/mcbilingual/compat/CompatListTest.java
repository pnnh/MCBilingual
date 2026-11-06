package pnnh.mcbilingual.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class CompatListTest {
    @Test
    void emptyListLeavesUnlistedIdsFull() {
        CompatList.Parsed parsed = CompatList.parse(List.of());

        assertEquals(CompatMode.FULL, CompatList.modeOf(parsed.modes(), "jei"));
        assertTrue(parsed.warnings().isEmpty());
    }

    @Test
    void modIdIgnoresCaseAndSurroundingSpace() {
        CompatList.Parsed parsed = CompatList.parse(List.of(" JEI = Off "));

        assertEquals(CompatMode.OFF, CompatList.modeOf(parsed.modes(), "jei"));
        assertTrue(parsed.warnings().isEmpty());
    }

    @Test
    void invalidModeRunsAsFullAndWarnsOnce() {
        CompatList.Parsed parsed = CompatList.parse(List.of("jei=Sometimes"));

        assertEquals(CompatMode.FULL, CompatList.modeOf(parsed.modes(), "jei"));
        assertEquals(1, parsed.warnings().size());
        assertEquals(CompatList.Kind.INVALID, parsed.warnings().getFirst().kind());
        assertEquals("jei", parsed.warnings().getFirst().modId());
    }

    @Test
    void laterDuplicateWinsAndWarnsOnce() {
        CompatList.Parsed parsed = CompatList.parse(List.of("jei=Generic", "jei=Off"));

        assertEquals(CompatMode.OFF, CompatList.modeOf(parsed.modes(), "jei"));
        assertEquals(1, parsed.warnings().size());
        assertEquals(CompatList.Kind.DUPLICATE, parsed.warnings().getFirst().kind());
    }

    @Test
    void blankIdIsDropped() {
        CompatList.Parsed parsed = CompatList.parse(List.of("=Off", "   =Full", "   "));

        assertTrue(parsed.modes().isEmpty());
        assertTrue(parsed.warnings().isEmpty());
    }

    @Test
    void unlistedIdStaysFullBesideAListedOne() {
        CompatList.Parsed parsed = CompatList.parse(List.of("ae2=Full"));

        assertEquals(CompatMode.FULL, CompatList.modeOf(parsed.modes(), "ae2"));
        assertEquals(CompatMode.FULL, CompatList.modeOf(parsed.modes(), "jei"));
        assertTrue(parsed.warnings().isEmpty());
    }
}
