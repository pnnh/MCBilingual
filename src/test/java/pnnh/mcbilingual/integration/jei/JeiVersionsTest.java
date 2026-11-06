package pnnh.mcbilingual.integration.jei;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class JeiVersionsTest {
    @Test
    void betterMcBuildIsLegacy() {
        assertArrayEquals(new int[] {19, 27, 0, 343}, JeiVersions.parse("19.27.0.343"));
        assertEquals(JeiVersions.Line.LEGACY, JeiVersions.classify("19.27.0.343"));
    }

    @Test
    void lastReleaseBeforeTheConstructorChangeIsLegacy() {
        assertEquals(JeiVersions.Line.LEGACY, JeiVersions.classify("19.55.0.999"));
        assertEquals(JeiVersions.Line.LEGACY, JeiVersions.classify("19.55.0"));
    }

    @Test
    void filterConfigConstructorStartsAt19_56() {
        assertEquals(JeiVersions.Line.FILTER_CONFIG, JeiVersions.classify("19.56.0"));
        assertEquals(JeiVersions.Line.FILTER_CONFIG, JeiVersions.classify("19.56.0.438"));
        assertEquals(JeiVersions.Line.FILTER_CONFIG, JeiVersions.classify("19.57.0.446"));
        assertEquals(JeiVersions.Line.FILTER_CONFIG, JeiVersions.classify("19.70.0.469"));
    }

    @Test
    void otherMajorsAndGarbageAreUnknown() {
        assertEquals(JeiVersions.Line.UNKNOWN, JeiVersions.classify("20.1.0"));
        assertEquals(JeiVersions.Line.UNKNOWN, JeiVersions.classify("26.1.0"));
        assertEquals(JeiVersions.Line.UNKNOWN, JeiVersions.classify("19.56.0-beta"));
        assertEquals(JeiVersions.Line.UNKNOWN, JeiVersions.classify("v19.27.0"));
        assertEquals(JeiVersions.Line.UNKNOWN, JeiVersions.classify(""));
        assertEquals(JeiVersions.Line.UNKNOWN, JeiVersions.classify(null));
        assertNull(JeiVersions.parse("19.beta"));
    }
}
