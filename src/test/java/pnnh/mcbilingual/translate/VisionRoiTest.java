package pnnh.mcbilingual.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import pnnh.mcbilingual.translate.VisionRoi.Rect;

class VisionRoiTest {
    @Test
    void clampsToFrameEdges() {
        Rect rect = VisionRoi.clampCentered(0, 0, 100, 80, 320);
        assertEquals(0, rect.x());
        assertEquals(0, rect.y());
        assertEquals(100, rect.width());
        assertEquals(80, rect.height());
    }

    @Test
    void centersWhenRoomAllows() {
        Rect rect = VisionRoi.clampCentered(50, 40, 100, 80, 20);
        assertEquals(40, rect.x());
        assertEquals(30, rect.y());
        assertEquals(20, rect.width());
        assertEquals(20, rect.height());
    }

    @Test
    void sameGenerationAndRoiShareKey() {
        Rect a = VisionRoi.clampCentered(40, 40, 200, 200, 32);
        Rect b = VisionRoi.clampCentered(41, 41, 200, 200, 32);
        assertEquals(VisionRoi.cacheKey(3L, a), VisionRoi.cacheKey(3L, b));
    }

    @Test
    void differentGenerationsDiffer() {
        Rect rect = new Rect(8, 8, 32, 32);
        assertNotEquals(VisionRoi.cacheKey(1L, rect), VisionRoi.cacheKey(2L, rect));
    }
}
