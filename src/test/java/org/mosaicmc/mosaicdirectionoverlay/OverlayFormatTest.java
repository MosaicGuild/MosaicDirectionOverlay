package org.mosaicmc.mosaicdirectionoverlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OverlayFormatTest {

    @Test
    void formatsPositionFacingAndAngles() {
        assertEquals("XYZ 12.3 / 64.0 / -45.7  NORTH  (yaw 183.2 pitch -5.1)",
                OverlayFormat.format(12.345, 64.0, -45.678, "NORTH", 183.24f, -5.06f));
    }

    @Test
    void roundsAtOneDecimal() {
        assertEquals("XYZ 1.0 / 2.0 / 3.0  EAST  (yaw 90.0 pitch 0.0)",
                OverlayFormat.format(1.04, 2.04, 3.04, "EAST", 90.04f, 0.04f));
        // 0.25 is exact in binary, so half-up is deterministic here.
        assertEquals("XYZ 0.3 / 0.0 / 0.0  EAST  (yaw 0.0 pitch 0.0)",
                OverlayFormat.format(0.25, 0, 0, "EAST", 0, 0));
    }

    @Test
    void blankFacingFallsBackToPlaceholder() {
        assertEquals("XYZ 0.0 / 0.0 / 0.0  ?  (yaw 0.0 pitch 0.0)",
                OverlayFormat.format(0, 0, 0, "   ", 0, 0));
        assertEquals("XYZ 0.0 / 0.0 / 0.0  ?  (yaw 0.0 pitch 0.0)",
                OverlayFormat.format(0, 0, 0, null, 0, 0));
    }

    @Test
    void trimsFacing() {
        assertEquals("XYZ 1.0 / 2.0 / 3.0  SOUTH  (yaw 0.0 pitch 0.0)",
                OverlayFormat.format(1, 2, 3, "  SOUTH ", 0, 0));
    }

    @Test
    void updatesEveryFifthTick() {
        assertFalse(OverlayFormat.shouldUpdate(1));
        assertFalse(OverlayFormat.shouldUpdate(4));
        assertTrue(OverlayFormat.shouldUpdate(5));
        assertFalse(OverlayFormat.shouldUpdate(6));
        assertTrue(OverlayFormat.shouldUpdate(10));
        assertTrue(OverlayFormat.shouldUpdate(20));
    }
}
