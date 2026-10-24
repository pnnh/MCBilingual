package pnnh.mcbilingual.freeze;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import pnnh.mcbilingual.MCBilingual;
import pnnh.mcbilingual.compat.RivalMods;
import pnnh.mcbilingual.switchlang.SwitchKeyHandler;

/**
 * While frozen, toggles word / unit mode. Unbound default is L (not F3+J/K).
 */
public final class LookupModeKeyHandler {
    public static final KeyMapping TOGGLE_MODE = new KeyMapping(
            "key.mcbilingual.freeze_lookup_mode",
            KeyConflictContext.UNIVERSAL,
            InputConstants.UNKNOWN,
            SwitchKeyHandler.CATEGORY);

    private LookupModeKeyHandler() {
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_MODE);
    }

    public static boolean acceptsKey(int key, int scanCode, int action) {
        if (action != GLFW.GLFW_PRESS || RivalMods.isBlocked() || !FreezeSession.get().active()) {
            return false;
        }
        if (TOGGLE_MODE.isUnbound()) {
            return key == GLFW.GLFW_KEY_L;
        }
        return TOGGLE_MODE.matches(key, scanCode);
    }

    public static void handleKey(int key, int scanCode, int action) {
        if (acceptsKey(key, scanCode, action)) {
            FreezeSession.get().toggleLookupMode();
        }
    }

    @EventBusSubscriber(modid = MCBilingual.MOD_ID, value = Dist.CLIENT)
    public static final class Events {
        private Events() {
        }

        @SubscribeEvent
        public static void onKey(InputEvent.Key event) {
            handleKey(event.getKey(), event.getScanCode(), event.getAction());
        }

        @SubscribeEvent
        public static void onMouse(InputEvent.MouseButton.Pre event) {
            if (event.getAction() != GLFW.GLFW_PRESS || !FreezeSession.get().active()) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null || minecraft.getWindow().getScreenWidth() <= 0) {
                return;
            }
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();
            int mouseX = (int) (minecraft.mouseHandler.xpos() * screenWidth / minecraft.getWindow().getScreenWidth());
            int mouseY = (int) (minecraft.mouseHandler.ypos() * screenHeight / minecraft.getWindow().getScreenHeight());
            if (LookupModeHud.click(mouseX, mouseY)) {
                event.setCanceled(true);
            }
        }
    }
}
