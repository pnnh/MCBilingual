package pnnh.mcbilingual.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class RivalModsTest {
    @Test
    void eitherRivalBlocks() {
        assertEquals(List.of("polyglottooltip"), RivalMods.findPresent(List.of("jei", "polyglottooltip")));
        assertEquals(List.of("languagereload"), RivalMods.findPresent(List.of("languagereload")));
    }

    @Test
    void bothRivalsKeepStableOrder() {
        assertEquals(
                List.of("polyglottooltip", "languagereload"),
                RivalMods.findPresent(List.of("languagereload", "polyglottooltip", "jei")));
    }

    @Test
    void translationPackIsNotARival() {
        assertTrue(RivalMods.findPresent(List.of("i18nupdatemod", "jade", "ae2", "minecolonies")).isEmpty());
    }

    @Test
    void emptyLoadListStandsUp() {
        assertTrue(RivalMods.findPresent(List.of()).isEmpty());
        assertTrue(RivalMods.findPresent(null).isEmpty());
    }
}
