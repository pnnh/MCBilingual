package pnnh.mcbilingual.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CompatGateTest {
    @Test
    void fullWithoutFailureOrRivalRunsDedicatedCode() {
        assertTrue(CompatGate.dedicatedEnabled(CompatMode.FULL, false, false));
    }

    @Test
    void genericAndOffSkipDedicatedCode() {
        assertFalse(CompatGate.dedicatedEnabled(CompatMode.GENERIC, false, false));
        assertFalse(CompatGate.dedicatedEnabled(CompatMode.OFF, false, false));
    }

    @Test
    void unlistedIdIsFull() {
        assertTrue(CompatGate.dedicatedEnabled(CompatList.modeOf(Map.of(), "jei"), false, false));
    }

    @Test
    void failedFullSkipsDedicatedCodeAndLeavesSharedPathOpen() {
        Map<String, CompatMode> modes = Map.of("jei", CompatMode.FULL);

        assertFalse(CompatGate.dedicatedEnabled(CompatMode.FULL, true, false));
        assertFalse(CompatGate.sharedBlocked(modes, List.of("jei"), false));
    }

    @Test
    void genericOwnerDoesNotBlockSharedPath() {
        Map<String, CompatMode> modes = Map.of("jei", CompatMode.GENERIC);

        assertFalse(CompatGate.sharedBlocked(modes, List.of("jei"), false));
    }

    @Test
    void offOwnerBlocksSharedPath() {
        Map<String, CompatMode> modes = Map.of("jei", CompatMode.OFF);

        assertTrue(CompatGate.sharedBlocked(modes, List.of("jei"), false));
    }

    @Test
    void rivalDisablesDedicatedCodeAndBlocksSharedPath() {
        assertFalse(CompatGate.dedicatedEnabled(CompatMode.FULL, false, true));
        assertTrue(CompatGate.sharedBlocked(Map.of(), List.of(), true));
    }

    @Test
    void oneOffIdDoesNotChangeAnotherDedicatedResult() {
        Map<String, CompatMode> modes = Map.of("jei", CompatMode.OFF, "ae2", CompatMode.FULL);

        assertFalse(CompatGate.dedicatedEnabled(CompatList.modeOf(modes, "jei"), false, false));
        assertTrue(CompatGate.dedicatedEnabled(CompatList.modeOf(modes, "ae2"), false, false));
        assertTrue(CompatGate.sharedBlocked(modes, List.of("jei"), false));
        assertFalse(CompatGate.sharedBlocked(modes, List.of("ae2"), false));
    }
}
