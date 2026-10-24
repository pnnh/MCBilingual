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
import pnnh.mcbilingual.switchlang.mixin.KeyboardHandlerAccessor;

/**
 * Unbound control uses F3+K (works in chat; the chord is consumed so K is not typed).
 * A bound key replaces that chord.
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

    /**
     * Escape ends an active freeze without closing the underlying screen.
     * It never starts a freeze.
     */
    public static boolean acceptsEscape(int key, int action) {
        return action == GLFW.GLFW_PRESS
                && key == GLFW.GLFW_KEY_ESCAPE
                && FreezeSession.get().active()
                && !RivalMods.isBlocked();
    }

    public static boolean acceptsMouse(int button, int action) {
        if (action != GLFW.GLFW_PRESS || FREEZE_UI.isUnbound() || !acceptingInput()) {
            return false;
        }
        return FREEZE_UI.matchesMouse(button);
    }

    public static void handleKey(int key, int scanCode, int action) {
        if (acceptsEscape(key, action) || acceptsKey(key, scanCode, action)) {
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

    /**
     * While F3 is held, chat must not receive the freeze letter as typed text.
     */
    public static boolean shouldSuppressChatChar(int codePoint) {
        if (RivalMods.isBlocked() || !f3Held() || !FREEZE_UI.isUnbound()) {
            return false;
        }
        return codePoint == 'k' || codePoint == 'K';
    }

    @EventBusSubscriber(modid = MCBilingual.MOD_ID, value = Dist.CLIENT)
    public static final class Events {
        private Events() {
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
        // ChatScreen is allowed: freeze can target chat UGC, and F3+K must not type.
        return minecraft != null && minecraft.getOverlay() == null;
    }

    private static boolean f3Held() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return false;
        }
        return InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_F3);
    }
}
