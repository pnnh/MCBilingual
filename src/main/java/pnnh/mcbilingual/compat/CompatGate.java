package pnnh.mcbilingual.compat;

import java.util.Collection;
import java.util.Map;

import net.minecraft.client.Minecraft;

/**
 * Decides which features run for the current list.
 * The overloads that take plain values do not read the mod list or the config file.
 */
public final class CompatGate {
    private CompatGate() {
    }

    public static boolean dedicatedEnabled(CompatMode mode, boolean failed, boolean rival) {
        return mode == CompatMode.FULL && !failed && !rival;
    }

    public static boolean sharedBlocked(
            Map<String, CompatMode> modes,
            Collection<String> ownerIds,
            boolean rival) {
        if (rival) {
            return true;
        }
        if (ownerIds == null) {
            return false;
        }
        for (String ownerId : ownerIds) {
            if (CompatList.modeOf(modes, ownerId) == CompatMode.OFF) {
                return true;
            }
        }
        return false;
    }

    public static boolean dedicatedEnabled(String modId) {
        try {
            return dedicatedEnabled(
                    CompatConfig.mode(modId),
                    IntegrationGuard.failed(modId),
                    RivalMods.isBlocked());
        } catch (Exception | LinkageError exception) {
            return false;
        }
    }

    public static boolean sharedBlocked() {
        try {
            return sharedBlocked(CompatConfig.snapshot(), UiOwner.owners(currentScreen()), RivalMods.isBlocked());
        } catch (Exception | LinkageError exception) {
            return false;
        }
    }

    private static Class<?> currentScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.screen == null) {
            return null;
        }
        return minecraft.screen.getClass();
    }
}
