package pnnh.mcbilingual.freeze;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import pnnh.mcbilingual.MCBilingual;
import pnnh.mcbilingual.compat.RivalMods;
import pnnh.mcbilingual.switchlang.SwitchKeyHandler;
import pnnh.mcbilingual.switchlang.mixin.KeyboardHandlerAccessor;

/**
 * Unbound control uses F3+K. A bound key replaces that chord. Chat input ignores both.
 */
public final class FreezeKeyHandler {
    public static final KeyMapping FREEZE_UI = new KeyMapping(
            "key.mcbilingual.freeze_ui",
            KeyConflictContext.UNIVERSAL,
            InputConstants.UNKNOWN,
            SwitchKeyHandler.CATEGORY);

    private FreezeKeyHandler() {
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(FREEZE_UI);
    }

    public static boolean acceptsKey(int key, int scanCode, int action) {
        if (action != GLFW.GLFW_PRESS || !acceptingInput()) {
            return false;
        }
        if (FREEZE_UI.isUnbound()) {
            return key == GLFW.GLFW_KEY_K && f3Held();
        }
        return FREEZE_UI.matches(key, scanCode);
    }

    public static boolean acceptsMouse(int button, int action) {
        if (action != GLFW.GLFW_PRESS || FREEZE_UI.isUnbound() || !acceptingInput()) {
            return false;
        }
        return FREEZE_UI.matchesMouse(button);
    }

    public static void handleKey(int key, int scanCode, int action) {
        if (acceptsKey(key, scanCode, action)) {
            FreezeSession.get().requestToggle();
        }
    }

    public static void handleMouse(int button, int action) {
        if (acceptsMouse(button, action)) {
            FreezeSession.get().requestToggle();
        }
    }

    public static void claimDebugKey() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.keyboardHandler != null && f3Held()) {
            ((KeyboardHandlerAccessor) minecraft.keyboardHandler).mcbilingual$setHandledDebugKey(true);
        }
    }

    @EventBusSubscriber(modid = MCBilingual.MOD_ID, value = Dist.CLIENT)
    public static final class Events {
        private Events() {
        }

        @SubscribeEvent
        public static void onKey(InputEvent.Key event) {
            if (RivalMods.isBlocked() || event.getAction() != GLFW.GLFW_PRESS) {
                return;
            }
            if (FreezeSession.get().active()) {
                return;
            }
            if (acceptsKey(event.getKey(), event.getScanCode(), event.getAction())) {
                claimDebugKey();
                FreezeSession.get().requestToggle();
            }
        }

        @SubscribeEvent
        public static void onMouse(InputEvent.MouseButton.Pre event) {
            if (RivalMods.isBlocked() || event.getAction() != GLFW.GLFW_PRESS || FREEZE_UI.isUnbound()) {
                return;
            }
            if (FreezeSession.get().active()) {
                return;
            }
            if (acceptsMouse(event.getButton(), event.getAction())) {
                FreezeSession.get().requestToggle();
            }
        }
    }

    private static boolean acceptingInput() {
        if (RivalMods.isBlocked()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getOverlay() != null) {
            return false;
        }
        return !(minecraft.screen instanceof ChatScreen);
    }

    private static boolean f3Held() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return false;
        }
        return InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_F3);
    }
}
